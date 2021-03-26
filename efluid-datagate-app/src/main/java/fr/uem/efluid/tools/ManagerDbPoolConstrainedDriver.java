package fr.uem.efluid.tools;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * <p>
 * Default model for async start of completable
 * </p>
 *
 * @author elecomte
 * @version 2
 * @since v0.0.8
 */
public class ManagerDbPoolConstrainedDriver implements AsyncDriver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagerDbPoolConstrainedDriver.class);

    private final Map<AsyncSourceProcess, CompletableFuture<?>> survey = new ConcurrentHashMap<>();

    private final Map<AsyncSourceProcess, List<Future<?>>> steps = new ConcurrentHashMap<>();

    private Set<AsyncSourceProcess> runnings = ConcurrentHashMap.newKeySet();

    private HikariDataSource dataSource;

    /**
     * @param dataSource Pooled datasource for step orchestration
     */
    public ManagerDbPoolConstrainedDriver(HikariDataSource dataSource, long timeoutMs, long checkIntervalMs) {
        super();
        this.dataSource = dataSource;
        // Init survey on timeout
        startTimeoutSurvey(timeoutMs, checkIntervalMs);
    }

    /**
     * @param process
     * @param action
     * @see AsyncDriver#start(AsyncSourceProcess,
     * Consumer)
     */
    @Override
    public <T extends AsyncSourceProcess> void start(T process, Consumer<T> action) {
        LOGGER.info("Starting a new async process {}", process.getIdentifier());
        this.survey.put(process, CompletableFuture.runAsync(() -> action.accept(process)));
    }

    @Override
    public void dropFromSurvey(AsyncSourceProcess process) {
        LOGGER.info("Remove async process {} from survey", process.getIdentifier());
        this.steps.remove(process);
        this.survey.remove(process);
    }

    @Override
    public Collection<AsyncSourceProcess> listCurrentInSurvey() {
        return this.survey.keySet();
    }

    @Override
    public void kill(UUID identifier) {

        Optional<AsyncSourceProcess> process = this.survey.keySet().stream()
                .filter(p -> p.getIdentifier().equals(identifier))
                .findFirst();

        if (process.isPresent()) {
            AsyncSourceProcess proc = process.get();
            List<Future<?>> foundSteps = this.steps.get(proc);

            // Cancel steps
            if (foundSteps != null) {
                foundSteps.forEach(f -> f.cancel(true));
            }

            LOGGER.info("Killing process {} from survey. Get {} identified steps",
                    identifier, foundSteps != null ? foundSteps.size() : 0);

            // Cancel process
            this.survey.get(proc).cancel(true);

            // And remove them
            dropFromSurvey(proc);
        } else {
            LOGGER.warn("Cannot kill process {} from asyncDriver : process not found", identifier);
        }
    }

    /**
     * @param callables
     * @param current
     * @return
     * @throws InterruptedException
     * @see AsyncDriver#processSteps(List,
     * AsyncSourceProcess)
     */
    @Override
    public void processSteps(List<Callable<?>> callables, final AsyncSourceProcess current)
            throws InterruptedException {

        // Submit for completion (in same execution)
        List<Call> calls = callables.stream()
                .map(c -> new Call(current, c))
                .collect(Collectors.toList());

        LOGGER.info("Async steps for process {} started", current.getDescription());

        // Run until completion of all
        while (calls.stream().anyMatch(c -> !c.isCompleted())) {

            // Must still be on survey and available Con in pool
            if (this.survey.containsKey(current)
                    && this.dataSource.getHikariPoolMXBean().getIdleConnections() > 1) {
                calls.stream()
                        .filter(c -> !c.isCalled())
                        .findFirst()
                        .map(Call::start)
                        .ifPresent(c -> this.steps.getOrDefault(current, this.steps.putIfAbsent(current, new ArrayList<>())).add(c));
            }

            Thread.sleep(200);
        }

        LOGGER.info("Async steps for process {} are completed", current.getDescription());
    }

    private void startTimeoutSurvey(final long timeoutMs, final long checkIntervalMs) {

        LOGGER.debug("Begin timeout survey with timeout {}", timeoutMs);

        // Timeout survey is a fully autonomous thread
        new Thread(() -> {
            boolean repeat = true;
            while (repeat) {
                try {
                    ManagerDbPoolConstrainedDriver.this.survey.forEach((p, fc) -> {

                        // If process is expired
                        if (p.getCreatedTime().plus(timeoutMs, ChronoUnit.MILLIS).isBefore(LocalDateTime.now())) {
                            LOGGER.warn("Found Async process {} \"{}\" which go over timeout of {} milliseconds." +
                                    " Kill it with associated steps", p.getIdentifier(), p.getDescription(), timeoutMs);
                            kill(p.getIdentifier());
                        }
                    });
                    Thread.sleep(checkIntervalMs);
                } catch (InterruptedException i) {
                    LOGGER.debug("Interrupted survey, complete", i);
                    repeat = false;
                } catch (Throwable t) {
                    LOGGER.warn("Unprocessed error, try again", t);
                }
            }
            LOGGER.debug("Completed timeout survey");
        }, "AsyncDriver-Timeout-Survey").start();
    }

    private static class Call implements Runnable {

        private final AsyncSourceProcess process;
        private final Callable<?> callable;

        private Exception gathered;
        private CompletableFuture<?> future;

        private Call(AsyncSourceProcess process, Callable<?> callable) {
            this.process = process;
            this.callable = callable;
        }

        public boolean isCalled() {
            return this.future != null;
        }

        public boolean isCompleted() {
            return this.future!=null &&this.future.isDone();
        }

        @Override
        public void run() {
            try {
                this.callable.call();
            } catch (Exception e) {
                this.gathered = e;
            }
        }

        public CompletableFuture<?> start() {
            this.future = CompletableFuture.runAsync(this);
            return this.future;
        }
    }
}
