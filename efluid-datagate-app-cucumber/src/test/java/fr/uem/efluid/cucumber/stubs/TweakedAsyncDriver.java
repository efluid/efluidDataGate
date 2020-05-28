package fr.uem.efluid.cucumber.stubs;

import fr.uem.efluid.tools.FutureAsyncDriver;
import fr.uem.efluid.utils.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * <p>
 * A "tweakable" FutureAsyncDriver for testing fixed behavior in asynchronous processes
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
public class TweakedAsyncDriver extends FutureAsyncDriver {

    private static final Logger LOGGER = LoggerFactory.getLogger(TweakedAsyncDriver.class);

    private boolean lockedRun;

    private ApplicationException forcedError;

    /**
     *
     */
    public TweakedAsyncDriver() {
        super(4, 5000, 200); // Fixed pool
    }

    /**
     *
     */
    public void reset() {
        this.lockedRun = false;
        this.forcedError = null;
    }

    /**
     *
     */
    public void setLocked() {
        this.lockedRun = true;
    }

    /**
     * Apply a forced error on active driver. All callables will fail with this error. Can be used for testing on abort / clean / rollback processes
     *
     * @param ex specified Exception
     */
    public void setForcedError(ApplicationException ex) {
        this.forcedError = ex;
    }

    /**
     * @param source processor source for <tt>Callable</tt> to run
     * @param action consumer on specified processor for actions to process
     * @see fr.uem.efluid.tools.AsyncDriver#start(AsyncSourceProcess,
     * java.util.function.Consumer)
     */
    @Override
    public <T extends AsyncSourceProcess> void start(T source, Consumer<T> action) {
        // Add a delay to allow testing (else to fast to check running state)
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                LOGGER.debug("Interrupted delay in test");
            }
            action.accept(source);
        });
    }

    /**
     * @param callables <tt>Callable</tt> actions to process
     * @param current   used source for actions
     * @return initialized running processes as a List
     * @throws InterruptedException if killed / failed
     * @see fr.uem.efluid.tools.FutureAsyncDriver#processSteps(java.util.List,
     * AsyncSourceProcess)
     */
    @Override
    public void processSteps(List<Callable<?>> callables, final AsyncSourceProcess current)
            throws InterruptedException {

        // Replace steps by a fixed locked / error one
        if (this.lockedRun || this.forcedError != null) {
            super.processSteps(Collections.singletonList(tweakedCallable()), current);
        } else {
            // Or standard run
            super.processSteps(callables, current);
        }
    }

    private <T> Callable<T> tweakedCallable() {

        // Locked run
        if (this.lockedRun) {
            LOGGER.info("Using a locked async start : Callable never complete");
            return () -> {
                while (this.lockedRun) {
                    Thread.sleep(100);
                }
                throw new InterruptedException("Complete tweaked callable");
            };
        }

        // Failure
        return () -> {
            LOGGER.info("Using a fixed error : Callable always fail");
            Thread.sleep(50);
            throw this.forcedError;
        };
    }
}
