package net.sf.fmj.filtergraph;

import javax.media.Format;

/**
 * An output Pin.
 * @author Ken Larson
 *
 */
public class OutputPin extends Pin
{
	public OutputPin(Node owner, int pinNumber, int track)
	{
		super(TYPE_OUTPUT, owner, pinNumber, track);
	}

	public OutputPin(Node owner)
	{
		super(TYPE_OUTPUT, owner);
	}

	public Format setPlugInOutputFormat(Format format)
	{
		Format result = getOwnerNode().setPlugInOutputFormat(this, format);
		if (result == null)
			return result;
		setFormat(result);
		return result;
	}
}
