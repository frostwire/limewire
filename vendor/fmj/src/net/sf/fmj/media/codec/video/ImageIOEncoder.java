package net.sf.fmj.media.codec.video;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.media.Buffer;
import javax.media.Codec;
import javax.media.Format;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;

import net.sf.fmj.media.AbstractCodec;
import net.sf.fmj.media.util.BufferToImage;

/**
 * Base class for image encoder Codecs which use ImageIO.
 * @author Ken Larson
 *
 */
public abstract class ImageIOEncoder extends AbstractCodec implements Codec
{
	private final String formatName;
	
	public ImageIOEncoder(String formatName)
	{
		super();
		this.formatName = formatName;
		
		if (!ImageIO.getImageWritersByFormatName(formatName).hasNext())
		{	throw new RuntimeException("No ImageIO writer found for " + formatName);
		}
	}
	
	private final Format[] supportedInputFormats = new Format[] {
			new RGBFormat(null, -1, Format.byteArray, -1.0f, -1, -1, -1, -1),
			new RGBFormat(null, -1, Format.intArray, -1.0f, -1, -1, -1, -1),
		};

	@Override
	public Format[] getSupportedInputFormats()
	{
		return supportedInputFormats;
	}
	
	private BufferToImage bufferToImage;
	
	@Override
	public Format setInputFormat(Format format)
	{
		final VideoFormat videoFormat = (VideoFormat) format;
		if (videoFormat.getSize() == null)
			return null;	// must set a size.
		// TODO: check VideoFormat and compatibility
		bufferToImage = new BufferToImage((VideoFormat) format);
		return super.setInputFormat(format);
	}
	
	@Override
	public int process(Buffer input, Buffer output) 
	{
        if (!checkInputBuffer(input))
        {
            return BUFFER_PROCESSED_FAILED;
        }

        if (isEOM(input))
        {
            propagateEOM(output);	// TODO: what about data? can there be any?
            return BUFFER_PROCESSED_OK;
        }
        
		final BufferedImage image = (BufferedImage) bufferToImage.createImage(input);
		
		try
		{
			// TODO: this is very inefficient - it allocates a new byte array (or more) every time
			
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			if (!ImageIO.write(image, formatName, os))
			{	throw new RuntimeException("No ImageIO writer found for " + formatName);
			}
			os.close();
			
			final byte[] ba = os.toByteArray();
			output.setData(ba);
			output.setOffset(0);
			output.setLength(ba.length);
			return BUFFER_PROCESSED_OK;

		}
		catch (IOException e)
		{	output.setDiscard(true);
			output.setLength(0);
			return BUFFER_PROCESSED_FAILED;
		}		
		
		
	}


}


