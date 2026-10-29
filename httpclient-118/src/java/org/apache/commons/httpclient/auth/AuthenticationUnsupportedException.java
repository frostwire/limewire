

package org.apache.commons.httpclient.auth;

/**
 * Signals an unsupported authentication scheme.
 */
public class AuthenticationUnsupportedException extends RuntimeException {

    /**
     * 
     */
    public AuthenticationUnsupportedException() {
        super();
    }

    /**
     * 
     */
    public AuthenticationUnsupportedException(String message) {
        super(message);
    }
}
