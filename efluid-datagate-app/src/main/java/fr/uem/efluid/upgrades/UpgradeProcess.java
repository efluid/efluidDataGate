package fr.uem.efluid.upgrades;

import fr.uem.efluid.utils.ApplicationException;

/**
 * An application upgrade process, to handle complex updates on data when a flyway script is not enough
 *
 * @author elecomte
 * @version 1
 * @since v2.0.19
 */
public interface UpgradeProcess extends Comparable<UpgradeProcess> {

    boolean repeat();

    int index();

    String name();

    void runUpgrade() throws ApplicationException;

    @Override
    default int compareTo(UpgradeProcess o) {
        return Integer.compare(index(), o.index());
    }
}
