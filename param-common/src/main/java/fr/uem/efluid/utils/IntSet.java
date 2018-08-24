package fr.uem.efluid.utils;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * Custom set-like support for int primitive with some optimized processes. Optimized for
 * single int management and contains check
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class IntSet {

	private Access access = new SoloAccess();

	public void add(int value) {

		// Solo init done, need to be Multi
		if (!this.access.canAdd()) {
			this.access = new MultiAccess(this.access);
		}

		this.access.add(value);
	}

	public boolean contains(int value) {
		return this.access.contains(value);
	}

	private static interface Access {
		void add(int value);

		boolean contains(int value);

		boolean canAdd();

		int getInternal();
	}

	private static class SoloAccess implements Access {

		private int solo = Integer.MIN_VALUE;

		/**
		 * @param solo
		 */
		public SoloAccess() {
			super();
		}

		/**
		 * @param value
		 * @see fr.uem.efluid.utils.IntSet.Access#add(int)
		 */
		@Override
		public void add(int value) {
			this.solo = value;
		}

		/**
		 * @param value
		 * @return
		 * @see fr.uem.efluid.utils.IntSet.Access#contains(int)
		 */
		@Override
		public boolean contains(int value) {
			return this.solo == value;
		}

		/**
		 * @return
		 */
		@Override
		public int getInternal() {
			return this.solo;
		}

		/**
		 * @return
		 * @see fr.uem.efluid.utils.IntSet.Access#canAdd()
		 */
		@Override
		public boolean canAdd() {
			return this.solo != Integer.MIN_VALUE;
		}

	}

	private static class MultiAccess implements Access {

		private final Set<Integer> multi;

		public MultiAccess(Access solo) {
			this.multi = new HashSet<>();
			this.multi.add(Integer.valueOf(solo.getInternal()));
		}

		/**
		 * @param value
		 * @see fr.uem.efluid.utils.IntSet.Access#add(int)
		 */
		@Override
		public void add(int value) {
			this.multi.add(Integer.valueOf(value));
		}

		/**
		 * @param value
		 * @return
		 * @see fr.uem.efluid.utils.IntSet.Access#contains(int)
		 */
		@Override
		public boolean contains(int value) {
			return this.multi.contains(Integer.valueOf(value));
		}

		/**
		 * @return
		 * @see fr.uem.efluid.utils.IntSet.Access#canAdd()
		 */
		@Override
		public boolean canAdd() {
			return true;
		}

		/**
		 * @return
		 * @see fr.uem.efluid.utils.IntSet.Access#getInternal()
		 */
		@Override
		public int getInternal() {
			return 0;
		}

	}
}
