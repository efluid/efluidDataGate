package fr.uem.efluid.tools;

import fr.uem.efluid.utils.ApplicationException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * <p>
 * Entry point for async process start and step run. Specified as a component for easy
 * upgrade in test context
 * </p>
 * <p>
 * Mostly a basic facade to async features used in multi-step processes, for easy update
 * in some contexts (testing ...) or for heavy configuration changes. Do not care of the result of the processed Callable
 * </p>
 * <p>
 * Provides a survey feature : can check active processes and kill
 * them if timeout extended. Allows also to kill active process,
 * and to list active one for management
 * </p>
 *
 * @author elecomte
 * @version 2
 * @since v0.0.8
 */
public interface AsyncDriver {

    /**
     * Start a process and keep it under survey. Will be killed automatically if timeout completed
     *
     * @param source
     * @param action
     * @param <T>
     */
    <T extends AsyncSourceProcess> void start(T source, Consumer<T> action);

    /**
     * Remove from survey a process (once completed)
     *
     * @param process
     */
    void dropFromSurvey(AsyncSourceProcess process);

    /**
     * Get active process under survey
     *
     * @return
     */
    Collection<AsyncSourceProcess> listCurrentInSurvey();

    /**
     * Kill a survey - under process
     *
     * @param identifier
     */
    void kill(UUID identifier);

    /**
     * <p>
     * Run all steps (as <tt>Callable</tt>) for completing a <tt>AsyncSourceProcess</tt>. Do not get / extract callable results ...
     * </p>
     * <p>
     * Process steps concurrently using driver config
     * </p>
     *
     * @param stepCallables
     * @param source
     * @throws InterruptedException
     */
    void processSteps(List<Callable<?>> stepCallables, final AsyncSourceProcess source)
            throws InterruptedException;

    /**
     * <p>
     * For shared error management in source -&gt; step process
     * </p>
     *
     * @author elecomte
     * @version 2
     * @since v0.0.8
     */
    interface AsyncSourceProcess {

        /**
         * Managed identifier of the local process
         *
         * @return
         */
        UUID getIdentifier();

        /**
         * Description for async process management listing
         *
         * @return
         */
        String getDescription();

        /**
         * @return
         */
        LocalDateTime getCreatedTime();

        int getAyncStepNbr();

        /**
         * State of failure
         *
         * @return
         */
        boolean hasSourceFailure();

        /**
         * Defails on processed content
         *
         * @return
         */
        int getPercentDone();

        /**
         * <p>
         * Mark source failed
         * </p>
         *
         * @param error
         * @return
         */
        <F> F fail(ApplicationException error);

        ApplicationException getSourceFailure();
    }
}
