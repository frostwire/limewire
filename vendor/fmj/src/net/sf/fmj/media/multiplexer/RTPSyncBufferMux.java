package net.sf.fmj.media.multiplexer;

import java.util.logging.Logger;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.protocol.ContentDescriptor;

import net.sf.fmj.media.SleepHelper;
import net.sf.fmj.utility.LoggerSingleton;


/**
 * Intended to replace Sun's RTPSyncBufferMux, which is for multiplexing RTP streams to be 
 * transmitted, and presumably synchronizing them as well.
 * Sun's RTPSyncBufferMux extends RawSynBufferMux.
 * Not sure what any of these really do, for now all we need is a RawBufferMux with the correct content descriptor.
 * TODO: implement the "sync" part of this.  This means we sleep to match the timestamps.
 * @author Ken Larson
 *
 */
public class RTPSyncBufferMux extends RawBufferMux
{
	private static final Logger logger = LoggerSingleton.logger;

	public RTPSyncBufferMux()
	{
		super(new ContentDescriptor(ContentDescriptor.RAW_RTP));
	}

	@Override
	public String getName()
	{
		return "RTP Sync Buffer Multiplexer";
	}

	@Override
	public Format setInputFormat(Format format, int trackID)
	{
		// This is what Sun's RTPSyncBufferMux does, but it calls the equivalent method on
		// com.sun.media.rtp.RTPSessionMgr, which seems to be a bit of a hack, since it
		// makes assumptions about which RTPSessionMgr is actually being used.  Should
		// be determined by RTPManager.newInstance().
		if (!net.sf.fmj.media.rtp.RTPSessionMgr.formatSupported(format))
			return null;
		// Note: it seems like we would want to fully specify the format by filling in any
		// required but unspecified fields.  But Sun's RTPSessionMgr does not appear to have any
		// methods which allow us to get the actual supported formats, so it is doubtful that JMF
		// does such a fill-in.
		// this is probably OK since all of the RTP packetizers probably set their formats to be
		// as specific as possible.
		return super.setInputFormat(format, trackID);
	}

	private final SleepHelper sleepHelper = new SleepHelper();
	
	
	@Override
	public void open() throws ResourceUnavailableException 
	{
		super.open();
		sleepHelper.reset();
	}

	@Override
	public int process(Buffer buffer, int trackID) 
	{
		final int result = super.process(buffer, trackID);

		// no sleeping please, rtp is sending in real time, if we are late we need to catch up

		return result;
	}
}
