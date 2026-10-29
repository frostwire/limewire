package net.sf.fmj.media;

import java.util.logging.Logger;

import javax.media.Format;
import javax.media.Multiplexer;
import javax.media.protocol.ContentDescriptor;

import net.sf.fmj.utility.LoggerSingleton;

/**
 * Abstract base class to implement Multiplexer.
 * @author Ken Larson
 *
 */
public abstract class AbstractMultiplexer extends AbstractPlugIn implements Multiplexer
{
	private static final Logger logger = LoggerSingleton.logger;
	
	protected ContentDescriptor outputContentDescriptor;
	public ContentDescriptor setContentDescriptor(ContentDescriptor outputContentDescriptor)
	{
		this.outputContentDescriptor = outputContentDescriptor;
		return outputContentDescriptor;
	}
	
	protected Format[] inputFormats;
	
	public Format setInputFormat(Format format, int trackID)
	{
		if (trackID >= numTracks)
		{	logger.warning("Rejecting input format for track number out of range: " + trackID + ": " + format);
			return null;
		}
		boolean match = false;
		for (Format supported : getSupportedInputFormats())
		{	if (supported.matches(format))
				match = true;
		}
		if (!match)
		{	logger.fine("Rejecting unsupported input format for track " + trackID + ": " + format);
			return null;
		}
		
		// TODO: we want to take the most specific matching input format, and
		// make the most specific format we can.
		
		logger.finer("setInputFormat " + format + " " + trackID);
		if (inputFormats != null)	// TODO: should we save this somewhere and apply once inputFormats is not null?
			inputFormats[trackID] = format;
		
		return format;
	}

	protected int numTracks;
	public int setNumTracks(int numTracks)
	{
		logger.finer("setNumTracks " + numTracks);
		inputFormats = new Format[numTracks];

		this.numTracks = numTracks;
		return numTracks;
	}
}
