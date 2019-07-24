package fr.uem.efluid.tools;

import fr.uem.efluid.utils.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static fr.uem.efluid.utils.ErrorType.PREPARATION_BIZ_FAILURE;

/**
 * <p>
 * Default model for async start of completable
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
public class FutureAsyncDriver implements AsyncDriver {

    private static final Logger LOGGER = LoggerFactory.getLogger(FutureAsyncDriver.class);

    private final ExecutorService executor;

    private final long timeoutMs;

    private final Map<AsyncSourceProcess, CompletableFuture<?>> survey = new ConcurrentHashMap<>();

    private final Map<AsyncSourceProcess, List<Future<?>>> steps = new ConcurrentHashMap<>();

    /**
     * @param poolSize for step processes
     */
    public FutureAsyncDriver(int poolSize, long timeoutMs, long checkIntervalMs) {
        super();
        this.executor = Executors.newFixedThreadPool(poolSize);
        this.timeoutMs = timeoutMs;

        // Init survey on timeout
        startTimeoutSurvey(timeoutMs, checkIntervalMs);
    }

    /**
     * @param process
     * @param action
     * @see fr.uem.efluid.tools.AsyncDriver#start(AsyncSourceProcess,
     * java.util.function.Consumer)
     */
    @Override
    public <T extends AsyncSourceProcess> void start(T process, Consumer<T> action) {
        LOGGER.info("Starting a new async process {}", process.getIdentifier());
        this.survey.put(process, CompletableFuture.runAsync(() -> action.accept(process), this.executor));
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
     * @see fr.uem.efluid.tools.AsyncDriver#processSteps(java.util.List,
     * AsyncSourceProcess)
     */
    @Override
    public <T> List<T> processSteps(List<Callable<T>> callables, final AsyncSourceProcess current)
            throws InterruptedException {

        // Submit for completion (in same execution)
        List<Future<T>> futures = callables.stream().map(this.executor::submit).collect(Collectors.toList());

        // Add them to process
        futures.forEach(f -> this.steps.getOrDefault(current, this.steps.putIfAbsent(current, new ArrayList<>())).add(f));

        LOGGER.info("Async steps for process {} started", current.getDescription());

        // Wait for completion (in current process thread)
        while (!futures.stream().allMatch(Future::isDone)) {
            Thread.sleep(200);
        }

        LOGGER.info("Async steps for process {} are completed. Extract result", current.getDescription());

        // Get result
        List<T> results = futures.stream()
                .map(c -> gatherResult(c, current))
                .sorted()
                .collect(Collectors.toList());

        LOGGER.info("Async steps {} results for process {} are available", results.size(), current.getDescription());

        return results;
    }

    private void startTimeoutSurvey(final long timeoutMs, final long checkIntervalMs) {

        LOGGER.debug("Begin timeout survey with timeout {}", timeoutMs);

        // Timeout survey is a fully autonomous thread
        new Thread(() -> {
            boolean repeat = true;
            while (repeat) {
                try {
                    FutureAsyncDriver.this.survey.forEach((p, fc) -> {

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

    /**
     * <p>
     * Join future execution and gather exception if any
     * </p>
     *
     * @param future
     * @return
     */
    private static <T> T gatherResult(Future<T> future, final AsyncSourceProcess current) {

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error will processing diff", e);

            // If already identified, keep going on 1st identified error
            if (current.hasSourceFailure()) {
                throw current.getSourceFailure();
            }
            return current.fail(new ApplicationException(PREPARATION_BIZ_FAILURE, "Aborted on exception ", e));
        }
    }

}