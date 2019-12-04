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
public class DictionaryApiClient implements DictionaryApi {

    private final String uri;
    private final String token;
    private final RestTemplate template;

    /**
     *
     */
    public DictionaryApiClient(String uri, String token) {
        this.uri = uri;
        this.token = token;
        this.template = new RestTemplate();
        RestApi.configureMessageConverters(this.template);
    }

    /**
     * @param file <tt>MultipartFile</tt> to process
     * @return package export to upload
     * @throws ApplicationException on received error for DataGate instance
     */
    @Override
    public CreatedDictionaryView uploadDictionaryPackage(MultipartFile file) throws ApplicationException {

        try {
            MultiValueMap<String, Object> data = new LinkedMultiValueMap<>();
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getName();
                }
            };
            data.add("file", resource);

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(data, RestApi.FIXED_UPLOAD_HEADERS);
            return this.template.postForObject(this.uri + "/upload?token=" + this.token, request, CreatedDictionaryView.class);

        } catch (Exception e) {
            throw new ApplicationException(ErrorType.WRONG_CLIENT_CALL, "Cannot process call to /updoad", e);
        }
    }

    /**
     * @param versionName init version
     * @throws ApplicationException on received error for DataGate instance
     */
    @Override
    public void setVersion(String versionName) throws ApplicationException {
        try {
            this.template.postForEntity(
                    this.uri + "/version/" + versionName + "?token=" + this.token,
                    new HttpEntity<>(null),
                    String.class);
        } catch (Exception e) {
            throw new ApplicationException(ErrorType.WRONG_CLIENT_CALL, "Cannot process call to POST /version", e);
        }
    }

    /**
     * @return last active version
     * @throws ApplicationException on received error for DataGate instance
     */
    @Override
    public VersionView getLastVersion() throws ApplicationException {
        try {
            return this.template.getForEntity(
                    new URI(this.uri + "/version/?token=" + this.token),
                    VersionView.class).getBody();
        } catch (Exception e) {
            throw new ApplicationException(ErrorType.WRONG_CLIENT_CALL, "Cannot process call to GET /version", e);
        }
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
