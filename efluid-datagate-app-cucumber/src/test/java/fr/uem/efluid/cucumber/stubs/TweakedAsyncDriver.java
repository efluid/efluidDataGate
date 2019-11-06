package fr.uem.efluid.cucumber.stubs;

import fr.uem.efluid.tools.FutureAsyncDriver;
import fr.uem.efluid.utils.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
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
     * @param ex
     */
    public void setForcedError(ApplicationException ex) {
        this.forcedError = ex;
    }

    /**
     * @param source
     * @param action
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
     * @param callables
     * @param current
     * @return
     * @throws InterruptedException
     * @see fr.uem.efluid.tools.FutureAsyncDriver#processSteps(java.util.List,
     * AsyncSourceProcess)
     */
    @Override
    public <T> List<T> processSteps(List<Callable<T>> callables, AsyncSourceProcess current) throws InterruptedException {

        // Replace steps by a fixed locked / error one
        if (this.lockedRun || this.forcedError != null) {
            super.processSteps(Arrays.asList(tweakedCallable()), current);
        }

        // Or standard run
        return super.processSteps(callables, current);
    }

    /**
     * @return
     */
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
