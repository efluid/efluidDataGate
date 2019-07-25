package fr.uem.efluid.services.types;


/**
 * <p>
 * DTO for version diff
 * </p>
 *
 * @author elecomte
 * @since v2.0.0
 * @version 1
 */
public class VersionCompare {

    private final VersionData one;

    private final VersionData two;

    public VersionCompare(VersionData one, VersionData two) {
        this.one = one;
        this.two = two;
    }

    public VersionData getOne() {
        return one;
    }

    public VersionData getTwo() {
        return two;
    }
}
