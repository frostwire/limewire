package org.limewire.plugin.daap;

import java.io.FileInputStream;
import java.io.IOException;

import com.limegroup.gnutella.FileDesc;

import de.kapsi.net.daap.DaapStreamSource;
import de.kapsi.net.daap.Song;

/**
 * Handles the audio stream
 */
class DaapStreamSourceImpl implements DaapStreamSource {
    
    public Object getSource(Song song) throws IOException {
        FileDesc fileDesc = (FileDesc)song.getAttachment();

        if (fileDesc != null) {
            return new FileInputStream(fileDesc.getFile());
        }
        
        return null;
    }
}