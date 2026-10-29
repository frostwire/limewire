package net.sf.fmj.filtergraph;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.Multiplexer;
import javax.media.PlugInManager;

/**
 * A node in a filter graph for a Multiplexer (one input track only).
 * The graph will have a MuxNode for each input to a Multiplexer, 
 * with the same Multiplexer.  
 * @author Ken Larson
 *
 */
public class MuxNode extends Node
{
	private final Multiplexer mux;

	public MuxNode(Multiplexer mux, Format inputFormat, int destTrack)
	{
		super(mux, 1, 0);	// TODO: don't have multiple mux nodes, set track #
		setInputPin(0, new InputPin(this, 0, destTrack));
		
		getInputPin(0).setFormat(inputFormat);
		this.mux = mux;
	}
	
	
	@Override
	public int getPlugInType()
	{
		return PlugInManager.MULTIPLEXER;
	}
	
	@Override
	Format setPlugInInputFormat(InputPin pin, Format format)
	{
		return mux.setInputFormat(format, pin.getTrack());
	}
	
	@Override
	Format setPlugInOutputFormat(OutputPin pin, Format format)
	{	throw new UnsupportedOperationException();
	}


	
	@Override
	public Node duplicate()
	{
		return propagateDuplicate(new MuxNode(getMultiplexer(), getInputFormat(), getInputPin(0).getTrack()));
		
	}

	@Override
	public Format getInputFormat()
	{
		return getInputPin(0).getFormat();
	}

	public Multiplexer getMultiplexer()
	{
		return mux;
	}

	
	@Override
	public int process(final Buffer input, final int sourceTrackNumber, final int destTrackNumber, final int flags)
	{
		if (input.getFormat() == null)
			input.setFormat(getInputFormat());	// TODO: is this right?  JMF appears to set the format in between demux track read adnd mux process.
		
		final int processResult = getMultiplexer().process(input, destTrackNumber);	// TODO: check return code
		// TODO: set buffer1 format if it is not set?
		if (processResult != Multiplexer.BUFFER_PROCESSED_OK)
		{
			if (processResult == Multiplexer.OUTPUT_BUFFER_NOT_FILLED ||
				processResult == Multiplexer.INPUT_BUFFER_NOT_CONSUMED)
				logger.finer("Multiplexer process result: " + processResult);	// this is common, so don't pollute the log.
			else
				logger.warning("Multiplexer process result: " + processResult);
				
			// TODO: set discard in buffer or any other flags?
			// TODO: check any other buffer flags?
		}
		
		return processResult;

	}
	
	

	@Override
	public void addDestLink(Link n)
	{
		throw new UnsupportedOperationException();
	}
}