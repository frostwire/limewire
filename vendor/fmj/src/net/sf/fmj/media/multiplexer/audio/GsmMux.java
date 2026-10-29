package net.sf.fmj.media.multiplexer.audio;

import net.sf.fmj.media.multiplexer.AbstractStreamCopyMux;

import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.FileTypeDescriptor;

/**
 * Gsm Multiplexer class, actually just provides unmodified input to the output.
 * @author Martin Harvan
 */
public class GsmMux extends AbstractStreamCopyMux {

    public GsmMux() {
        super(new ContentDescriptor(FileTypeDescriptor.GSM));
    }

    public Format[] getSupportedInputFormats() {
        return new Format[]{
                new AudioFormat(AudioFormat.GSM, 8000, 8, 1, -1, -1)
        };
    }
}
