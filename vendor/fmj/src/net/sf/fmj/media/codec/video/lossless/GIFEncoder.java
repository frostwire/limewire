package net.sf.fmj.media.codec.video.lossless;

import javax.media.Format;
import javax.media.format.VideoFormat;

import net.sf.fmj.media.codec.video.ImageIOEncoder;
import net.sf.fmj.media.format.GIFFormat;

/**
 * GIF encoder Codec.
 * Won't work prior to Java 1.6 due to lack of GIF encoding ability in ImageIO.
 * @author Ken Larson
 *
 */
public class GIFEncoder extends ImageIOEncoder
{
	public GIFEncoder()
	{
		super("GIF");
	}

	private final Format[] supportedOutputFormats = new Format[] {
			new GIFFormat(),
		};

	@Override
	public Format[] getSupportedOutputFormats(Format input)
	{
		if (input == null)
			return supportedOutputFormats;
		final VideoFormat inputCast = (VideoFormat) input;
		final Format[] result = new Format[] {
				new GIFFormat(inputCast.getSize(), -1, Format.byteArray, inputCast.getFrameRate())};
	
		return result;
	}
}
