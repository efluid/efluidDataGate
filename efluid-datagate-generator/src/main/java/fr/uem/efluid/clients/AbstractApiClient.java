package fr.uem.efluid.clients;

import fr.uem.efluid.rest.RestApi;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;

/**
 * Basic API Call tooling with token support
 */
public abstract class AbstractApiClient {

    private final String uri;
    private final String token;
    private final RestTemplate template;

    /**
     * @param uri   associated entry point
     * @param token user technical token
     */
    public AbstractApiClient(String uri, String token) {
        this.uri = uri;
        this.token = token;
        this.template = new RestTemplate();
        RestApi.configureMessageConverters(this.template);
    }

    protected <T> T postFile(String path, MultipartFile file, Class<T> returnType) throws ApplicationException {
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
            return post(path, request, returnType);
        } catch (IOException e) {
            throw new ApplicationException(ErrorType.WRONG_CLIENT_CALL, "Cannot process call to POST file onto " + path, e);
        }
    }

    protected <T> T post(String path, Object postObject, Class<T> returnType) throws ApplicationException {
        try {
            return this.template.postForEntity(this.uri + path + (path.contains("?") ? "&" : "?") + "token=" + this.token,
                    postObject, returnType).getBody();
        } catch (Exception e) {
            throw new ApplicationException(ErrorType.WRONG_CLIENT_CALL, "Cannot process call to POST " + path, e);
        }
    }

    protected void put(String path) throws ApplicationException {
        try {
            this.template.put(
                    this.uri + path + "?token=" + this.token,
                    new HttpEntity<>(null));
        } catch (Exception e) {
            throw new ApplicationException(ErrorType.WRONG_CLIENT_CALL, "Cannot process call to PUT " + path, e);
        }
    }


    protected <T> T get(String path, Class<T> type) throws ApplicationException {
        try {
            return this.template.getForEntity(
                    new URI(this.uri + path + "?token=" + this.token),
                    type).getBody();
        } catch (Exception e) {
            throw new ApplicationException(ErrorType.WRONG_CLIENT_CALL, "Cannot process call to GET " + path, e);
        }
    }
}
