package net.sf.fmj.filtergraph;

import javax.media.Buffer;
import javax.media.Format;

/**
 * A Pin is an input or output of a filter graph node.
 * @author Ken Larson
 *
 */
public abstract class Pin
{
	// TODO: types are redundant with InputPin and OutputPin subclasses
	public static final int TYPE_INPUT = 0;
	public static final int TYPE_OUTPUT = 1;
	
	public static final int NO_TRACK = -1;
	private final int type;
	private final Node owner;
	private int pinNumber;
	private int track;	// only for demux output and mux input, NO_TRACK otherwise.
	private Format format;
	private Buffer buffer;
	
	// TODO: the filter graph processing really only uses certain items, like the Buffer, from the input pin.
	// perhaps the Buffer should be part of the Link.  Also, the Format basically ends up being the same
	// for both the input and output pins that are linked, so this is redundant.
	// TODO: clarify the difference between setting the format and setting the plug in format.
	
	/**
	 * Constructor when there is only 1 pin of the given type (input, output).
	 */
	protected Pin(int type, Node owner)
	{	super();
		this.type = type;
		if (owner == null)
			throw new NullPointerException();
		this.owner = owner;
		this.pinNumber = 0;
		this.track = NO_TRACK;
	}
	
	/**
	 * Constructor for pin with track index (used for demux output pins and mux input pins).
	 */
	protected Pin(int type, Node owner, int pinNumber, int track)
	{
		super();
		this.type = type;
		if (owner == null)
			throw new NullPointerException();
		this.owner = owner;
		this.track = track;
	}

	public int getType()
	{	
		return type;
	}
	
	public int getTrack()
	{
		return track;
	}
	
	public int getPinNumber()
	{	
		return pinNumber;
	}
	

	public void setTrack(int track)
	{
		this.track = track;
	}

	public Format getFormat()
	{
		return format;
	}

	public void setFormat(Format format)
	{
		this.format = format;
	}
	

	public Buffer getBuffer()
	{
		return buffer;
	}

	public void setBuffer(Buffer buffer)
	{
		this.buffer = buffer;
	}
	
	public Node getOwnerNode()
	{	
		return owner;
	}
}