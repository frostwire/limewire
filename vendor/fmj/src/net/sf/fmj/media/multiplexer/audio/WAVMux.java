package net.sf.fmj.media.multiplexer.audio;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.protocol.FileTypeDescriptor;
import javax.sound.sampled.AudioFileFormat;

import net.sf.fmj.media.codec.JavaSoundCodec;
import net.sf.fmj.utility.IOUtils;
import net.sf.fmj.utility.LoggerSingleton;


/**
 * WAV audio multiplexer.
 * @author Ken Larson
 *
 */
public class WAVMux extends JavaSoundMux
{
	private static final Logger logger = LoggerSingleton.logger;

	private static final boolean USE_JAVASOUND = true;
	
	public WAVMux()
	{
		super(new FileTypeDescriptor(FileTypeDescriptor.WAVE), AudioFileFormat.Type.WAVE);
	}
	
	@Override
	public Format setInputFormat(Format format, int trackID)
	{
		final AudioFormat af = (AudioFormat) format;
		if (af.getSampleSizeInBits() == 8 && af.getSigned() == AudioFormat.SIGNED)
			return null;	// 8-bit is always unsigned for Wav.
		
		if (af.getSampleSizeInBits() == 16 && af.getSigned() == AudioFormat.UNSIGNED)
			return null;	// 16-bit is always signed for Wav.

		return super.setInputFormat(format, trackID);
	}

	@Override
	protected void write(InputStream in, OutputStream out, javax.sound.sampled.AudioFormat javaSoundFormat) throws IOException
	{
		if (USE_JAVASOUND)
		{	super.write(in, out, javaSoundFormat);
			
		}
		else
		{	// alternative to JavaSound - not necessary.
			try
			{
				byte[] header = JavaSoundCodec.createWavHeader(javaSoundFormat);	// TODO: no length
				if (header == null)
					throw new IOException("Unable to create wav header");
				
				//System.out.println("WAVMux Header: " + header.length);
				out.write(header);
				
				IOUtils.copyStream(in, out);
				
				// TODO: go back and write header.
			}
			catch (InterruptedIOException e)
			{
				logger.log(Level.FINE, "" + e, e);
				throw e;
			}
			catch (IOException e)
			{
				logger.log(Level.WARNING, "" + e, e);
				throw e;
			}
		}
	}
	

}
