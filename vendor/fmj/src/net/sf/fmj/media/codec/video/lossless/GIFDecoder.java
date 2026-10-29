package net.sf.fmj.media.codec.video.lossless;

import javax.media.Format;

import net.sf.fmj.media.codec.video.ImageIODecoder;
import net.sf.fmj.media.format.GIFFormat;

/**
 * GIF decoder Codec.
 * @author Ken Larson
 *
 */
public class GIFDecoder extends ImageIODecoder
{
	public GIFDecoder()
	{
		super("GIF");
	}
	
	private final Format[] supportedInputFormats = new Format[] {
			new GIFFormat(),
		};

	@Override
	public Format[] getSupportedInputFormats()
	{
		return supportedInputFormats;
	}
}
