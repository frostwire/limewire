package org.limewire.plugin.daap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.plugin.daap.settings.DaapSettings;


import de.kapsi.net.daap.DaapAuthenticator;
import de.kapsi.net.daap.DaapUtil;

/**
 * Implements the DaapAuthenticator
 */
class DaapAuthenticatorImpl implements DaapAuthenticator {
    
    private static final Log LOG = LogFactory.getLog(DaapAuthenticatorImpl.class);
    
    /**
     * Returns true if username and password are correct.<p>
     * Note: iTunes does not support usernames (i.e. it's
     * don't care)!
     */
    public byte[] getPassword(String username, Object scheme) {
        String password = DaapSettings.DAAP_PASSWORD.getValue();
        if (password.startsWith("MD5/")) {
            password = password.substring(4);
        }
        return DaapUtil.parseHexString(password);
    }
    
    public boolean authenticate(String username, String password, String uri, String nonce) {
        
        if (uri == null && nonce == null) {
            // BASIC
            return DaapSettings.DAAP_PASSWORD.equals(password);
        } else if (uri != null && nonce != null) {
            // DIGEST
            String ha1 = DaapSettings.DAAP_PASSWORD.getValue();
            if (ha1.startsWith("MD5/")) {
                ha1 = ha1.substring(4);
            }
            String ha2 = DaapUtil.calculateHA2(uri);
            String digest = DaapUtil.digest(ha1, ha2, nonce);
            return digest.equalsIgnoreCase(password);
        } else {
            if (LOG.isErrorEnabled()) {
                LOG.error("Unknown scheme!");
            }
        }
        
        return false;
    }
}