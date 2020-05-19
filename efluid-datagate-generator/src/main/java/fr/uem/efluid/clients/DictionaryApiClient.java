package fr.uem.efluid.clients;

import fr.uem.efluid.rest.RestApi;
import fr.uem.efluid.rest.v1.DictionaryApi;
import fr.uem.efluid.rest.v1.model.CreatedDictionaryView;
import fr.uem.efluid.rest.v1.model.VersionView;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

/**
 * <p>
 * Basic client using RestTemplate
 * </p>
 *
 * @author elecomte
 * @version 2
 * @since v0.0.1
 */
public class DictionaryApiClient extends AbstractApiClient implements DictionaryApi {

    /**
     * @param uri   associated entry point
     * @param token user technical token
     */
    public DictionaryApiClient(String uri, String token) {
        super(uri, token);
    }

    /**
     * @param file <tt>MultipartFile</tt> to process
     * @return package export to upload
     * @throws ApplicationException on received error for DataGate instance
     */
    @Override
    public CreatedDictionaryView uploadDictionaryPackage(MultipartFile file) throws ApplicationException {
       return postFile( "/dictionary/upload" , file, CreatedDictionaryView.class);
    }

    /**
     * @param versionName init version
     * @throws ApplicationException on received error for DataGate instance
     */
    @Override
    public void setVersion(String versionName) throws ApplicationException {
        put("/dictionary/version/" + versionName);
    }

    /**
     * @return last active version
     * @throws ApplicationException on received error for DataGate instance
     */
    @Override
    public VersionView getLastVersion() throws ApplicationException {
        return get("/dictionary/version/", VersionView.class);
    }

    /**
     * @return existing versions
     * @throws ApplicationException on received error for DataGate instance
     */
    @Override
    public List<VersionView> getVersions() throws ApplicationException {
        throw new ApplicationException(ErrorType.WRONG_CLIENT_CALL, "get versions is Not implemented");
    }

}
