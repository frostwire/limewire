package net.sf.fmj.filtergraph;

import javax.media.Format;

/** 
 * An input Pin.
 * @author Ken Larson 
 */
public class InputPin extends Pin
{

	public InputPin(Node owner, int pinNumber, int track)
	{
		super(TYPE_INPUT, owner, pinNumber, track);
	}

	public InputPin(Node owner)
	{
		super(TYPE_INPUT, owner);
	}
	
	public Format setPlugInInputFormat(Format format)
	{
		Format result = getOwnerNode().setPlugInInputFormat(this, format);
		if (result == null)
			return result;
		setFormat(result);
		return result;
	}


}
