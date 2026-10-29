package net.sf.fmj.media.codec.video.lossless;

import javax.media.Format;
import javax.media.format.VideoFormat;

import net.sf.fmj.media.codec.video.ImageIOEncoder;
import net.sf.fmj.media.format.PNGFormat;

/**
 * PNG encoder Codec.
 * @author Ken Larson
 *
 */
public class PNGEncoder extends ImageIOEncoder
{
	public PNGEncoder()
	{
		super("PNG");
	}

	private final Format[] supportedOutputFormats = new Format[] {
			new PNGFormat(),
		};

	@Override
	public Format[] getSupportedOutputFormats(Format input)
	{
		if (input == null)
			return supportedOutputFormats;
		final VideoFormat inputCast = (VideoFormat) input;
		final Format[] result = new Format[] {
				new PNGFormat(inputCast.getSize(), -1, Format.byteArray, inputCast.getFrameRate())};
	
		return result;
	}
}
