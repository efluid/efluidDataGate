package fr.uem.efluid.tools;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import fr.uem.efluid.utils.ApplicationException;

/**
 * <p>
 * Entry point for async process start and step run. Specified as a component for easy
 * upgrade in test context
 * </p>
 * <p>
 * Mostly a basic facade to async features used in multi-step processes, for easy update
 * in some contexts (testing ...) or for heavy configuration changes
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public interface AsyncDriver {

	/**
	 * @param runnable
	 */
	<T extends SourceErrorAware> void start(T source, Consumer<T> action);

	/**
	 * <p>
	 * Run all steps (as <tt>Callable</tt>) for completing a <tt>SourceErrorAware</tt> and
	 * provides results
	 * </p>
	 * <p>
	 * Process steps concurrently using driver config
	 * </p>
	 * 
	 * @param stepCallables
	 * @param source
	 * @return
	 * @throws InterruptedException
	 */
	public <T> List<T> processSteps(List<Callable<T>> stepCallables, final SourceErrorAware source)
			throws InterruptedException;

	/**
	 * <p>
	 * For shared error management in source -&gt; step process
	 * </p>
	 * 
	 * @author elecomte
	 * @since v0.0.8
	 * @version 1
	 */
	public static interface SourceErrorAware {

		boolean hasSourceFailure();

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
