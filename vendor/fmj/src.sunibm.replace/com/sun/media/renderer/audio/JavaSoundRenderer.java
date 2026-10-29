package com.sun.media.renderer.audio;

import javax.media.Format;
import javax.media.format.AudioFormat;

/**
 * 
 * Use FMJ's sound renderer instead of Sun's.
 * @author Warren Bloomer
 * @author Ken Larson
 *
 */
public class JavaSoundRenderer extends net.sf.fmj.media.renderer.audio.JavaSoundRenderer
{
	// override to return exactly the formats that JMF's does:
	
	private Format[] supportedInputFormats = new Format[] {
			new AudioFormat(AudioFormat.LINEAR, -1.0, -1, -1, -1, -1, -1, -1.0, Format.byteArray),
			new AudioFormat(AudioFormat.ULAW, -1.0, -1, -1, -1, -1, -1, -1.0, Format.byteArray),	
		};
	
	public Format[] getSupportedInputFormats() {
		return supportedInputFormats;	// JMF doesn't return all the details.
	}
}
