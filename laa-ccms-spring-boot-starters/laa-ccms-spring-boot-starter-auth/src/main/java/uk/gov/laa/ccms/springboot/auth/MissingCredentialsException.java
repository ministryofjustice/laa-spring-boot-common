package uk.gov.laa.ccms.springboot.auth;


import org.springframework.security.core.AuthenticationException;

public class MissingCredentialsException extends AuthenticationException {

    /**
     * Creates an instance of {@Code MissingCredentialsException} with a message and original cause.
     *
     * @param msg a message providing information about the exception
     * @param cause the original cause of this exception
     */
    public MissingCredentialsException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Creates an instance of {@Code MissingCredentialsException} with a message.
     *
     * @param msg a message providing information about the exception
     */
    public MissingCredentialsException(String msg) {
        super(msg);
    }

}
