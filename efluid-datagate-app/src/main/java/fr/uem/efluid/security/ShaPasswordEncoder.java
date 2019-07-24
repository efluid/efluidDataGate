package fr.uem.efluid.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.annotation.PostConstruct;

import org.pac4j.core.credentials.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import fr.uem.efluid.utils.FormatUtils;

/**
 * <p>
 * Very basic encoder support for passwords
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Component
public class ShaPasswordEncoder implements PasswordEncoder {

	private static final Logger LOGGER = LoggerFactory.getLogger(ShaPasswordEncoder.class);

	private final static String PWD_DIGEST = "SHA-256";

	/**
	 * @param password
	 * @return
	 * @see org.pac4j.core.credentials.password.PasswordEncoder#encode(java.lang.String)
	 */
	@Override
	public String encode(String password) {

		try {
			// Digest is not TS
			MessageDigest digest = MessageDigest.getInstance(PWD_DIGEST);

			// Hash + B64 (UTF8 encoded)
			return FormatUtils.encode(digest.digest(password.getBytes(FormatUtils.CONTENT_ENCODING)));
		} catch (NoSuchAlgorithmException e) {
			throw new ApplicationException(ErrorType.VALUE_SHA_UNSUP, "unsupported digest type " + PWD_DIGEST, e);
		}

	}

	/**
	 * @param plainPassword
	 * @param encodedPassword
	 * @return
	 * @see org.pac4j.core.credentials.password.PasswordEncoder#matches(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public boolean matches(String plainPassword, String encodedPassword) {
		return encode(plainPassword).equals(encodedPassword);
	}

	@PostConstruct
	public void signalLoading() {
		LOGGER.debug("[SECURITY] Load password encoder {}", this.getClass().getName());
	}
}
