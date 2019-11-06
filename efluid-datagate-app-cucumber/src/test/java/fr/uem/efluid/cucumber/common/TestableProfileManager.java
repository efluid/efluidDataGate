package fr.uem.efluid.cucumber.common;

import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileHelper;
import org.pac4j.core.profile.ProfileManager;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * <p>
 * The default security validation is session based with PAC4J. To allow to "force set" an
 * authentication for a test, this profile manager can provide a fixe profile for every
 * request, and simulate this way a valid authentication
 * </p>
 * <p>
 * As the <tt>ProfileManager</tt> is never cached, and always provided by a function
 * factory for security logic, it's easy to force set the manager by simply setting a new
 * factory into PAC4J global security config
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
public class TestableProfileManager extends ProfileManager<CommonProfile> {

    private final String fixedProfileId;

    /**
     * @param context
     */
    public TestableProfileManager(WebContext context, String fixedProfileId) {
        super(context);
        this.fixedProfileId = fixedProfileId;
    }

    /**
     * Retrieve all user profiles.
     *
     * @param readFromSession if the user profiles must be read from session
     * @return the user profiles
     */
    @Override
    public List<CommonProfile> getAll(final boolean readFromSession) {

        // Prefixed profile set - forced for testing
        if (this.fixedProfileId != null) {

            CommonProfile prof = new CommonProfile();
            prof.setId(this.fixedProfileId);
            prof.addAttribute(Pac4jConstants.USERNAME, this.fixedProfileId);
            return Collections.singletonList(prof);
        }

        // Else default behavior (allows to test auth)
        final LinkedHashMap<String, CommonProfile> profiles = retrieveAll(readFromSession);
        return ProfileHelper.flatIntoAProfileList(profiles);
    }
}
