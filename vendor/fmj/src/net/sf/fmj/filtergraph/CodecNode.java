/**
 * 
 */
package net.sf.fmj.filtergraph;

import java.util.logging.Level;

import javax.media.Buffer;
import javax.media.Codec;
import javax.media.Format;
import javax.media.PlugInManager;

import net.sf.fmj.utility.LoggingStringUtils;

/**
 * A node in a filter graph for a Codec.  dest has size of 1.
 * @author Ken Larson
 *
 */
public class CodecNode extends Node
{
	private final Codec codec;

	public CodecNode(Codec codec, Format inputFormat)
	{
		super(codec, 1, 1);
		setInputPin(0, new InputPin(this));
		setOutputPin(0, new OutputPin(this));
		getInputPin(0).setFormat(inputFormat);
		this.codec = codec;
	}
	
	
	@Override
	public int getPlugInType()
	{
		return PlugInManager.CODEC;
	}
	
	@Override
	Format setPlugInInputFormat(InputPin pin, Format format)
	{
		return codec.setInputFormat(format);
	}
	
	@Override
	Format setPlugInOutputFormat(OutputPin pin, Format format)
	{	
		return codec.setOutputFormat(format);
	}
	
	@Override
	public Node duplicate()
	{
		return propagateDuplicate(new CodecNode(getCodec(), getInputFormat()));
		
	}

	@Override
	public Format getInputFormat()
	{
		return getInputPin(0).getFormat();
	}

	public Codec getCodec()
	{
		return codec;
	}

	
	@Override
	public int process(final Buffer input, final int sourceTrackNumber, final int destTrackNumber, final int flags)
	{

		if (input.getLength() == 0 && input.getData() == null)
		{	
			logger.warning("Skipping processing of codec input buffer with length 0 and null buffer");
			// not sure why we get these, but we can, and it can cause the codec to throw an NPE.
			// perhaps we should be handling these somewhere before it gets here.
			// observed with this filter graph:
//				INFO: 	ContentDescriptor [null]
//				INFO: 	net.sf.fmj.media.parser.RawPushBufferParser
//				INFO: 		AudioFormat [dvi/rtp, 22050.0 Hz, 4-bit, Mono]
//				INFO: 		com.ibm.media.codec.audio.dvi.JavaDecoder
//				INFO: 			AudioFormat [LINEAR, 22050.0 Hz, 16-bit, Mono, LittleEndian, Signed]
//				INFO: 			com.sun.media.renderer.audio.JavaSoundRenderer
			return Codec.OUTPUT_BUFFER_NOT_FILLED;	// TODO: return BUFFER_PROCESSED_OK?
		}
		
		if (input.getFormat() == null)
			input.setFormat(getInputFormat());	// TODO: is this right?  JMF appears to set the format in between demux track read adnd codec process.
		
		// JMF re-uses the previous buffer
		if (getOutputBuffer(0) == null)
		{
			setOutputBuffer(0, new Buffer());
		}
		
		final Buffer buffer1 = getOutputBuffer(0);
		buffer1.setLength(0);
		buffer1.setTimeStamp(input.getTimeStamp());	// TODO: is this correct?
		buffer1.setFlags(input.getFlags());	// mgodehardt: this was missing, TODO: propagate EOM or anything else?
//		TODO: other fields to set/clear? these determined empirically
// 		Seems like setting the format is a good idea.  At least some codecs (like com.sun.media.codec.audio.ulaw.DePacketizer) do not 
//		appear to set the format of their output buffer.
		if (buffer1.getFormat() == null)
			buffer1.setFormat(getDestLink(0).getDestNode().getInputFormat());
//		if (buffer1.getFormat() == null)
//			System.out.println("FORMAT NULL EVEN AFTER SET!!!!");
			//			throw new NullPointerException();
		buffer1.setSequenceNumber(input.getSequenceNumber());	// TODO: is this right?  JMF appears to be setting the sequence numbers, not the codec
		// the one thing that is wierd about the sequence numbers, is that in the case of something like a packetizers, all of the packets derived from
		// a single source buffer will have the same sequence number.
		

		
		final int processResult;
		try {
			processResult = getCodec().process(input, buffer1);
		}
		catch (NullPointerException e)
		{
			logger.severe(LoggingStringUtils.bufferToStr(input));
			logger.severe(LoggingStringUtils.bufferToStr(buffer1));
			logger.log(Level.SEVERE, "" + e, e);
			throw e;
			
		}

		if (processResult != Codec.BUFFER_PROCESSED_OK)
		{
			if (processResult == Codec.OUTPUT_BUFFER_NOT_FILLED)
				logger.finer("Codec process result for " + getCodec() + ": " + processResult);	// this is common, so don't pollute the log.
			else if (processResult == Codec.INPUT_BUFFER_NOT_CONSUMED)	// we will be re-called with same buffer again.
				logger.finer("Codec process result for " + getCodec() + ": " + processResult);	// this is common, so don't pollute the log.
			else
				logger.warning("Codec process result for " + getCodec() + ": " + LoggingStringUtils.plugInResultToStr(processResult));
				
			// TODO: in the case of INPUT_BUFFER_NOT_CONSUMED, we probably need to re-call.
			// we are not doing anything with processResult, it probably needs to be returned.
			
			// TODO: set discard in buffer or any other flags?
			// TODO: check any other buffer flags?
		}
		return processResult;
		
	}



}