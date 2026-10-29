package org.limewire.plugin.daap;

import java.net.InetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.io.NetworkUtils;

import com.limegroup.gnutella.RouterService;

import de.kapsi.net.daap.DaapFilter;

/**
 * The DAAP Library should be only accessable from the LAN
 * as we can not guarantee for the required bandwidth and it
 * could be used to bypass Gnutella etc. Note: iTunes can't
 * connect to DAAP Libraries outside of the LAN but certain
 * iTunes download tools can.
 */
class DaapFilterImpl implements DaapFilter {

    private static final Log LOG = LogFactory.getLog(DaapFilterImpl.class);
    
    /**
     * Returns true if <tt>address</tt> is a private address
     */
    public boolean accept(InetAddress address) {
        
        try {
            // not private & not close, not allowed.
            if (!NetworkUtils.isVeryCloseIP(address)
                    && !NetworkUtils.isPrivateAddress(address))
                return false;
        } catch (IllegalArgumentException err) {
            LOG.error(err);
            return false;
        }

        // Is it a annoying fellow? >:-)
        return RouterService.getIpFilter().allow(address.getAddress());
    }
}