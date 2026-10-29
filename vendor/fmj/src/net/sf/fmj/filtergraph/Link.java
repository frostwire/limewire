package net.sf.fmj.filtergraph;


/**
 * A link between two nodes, from the output pin of one node to the input pin of another.
 * @author Ken Larson
 *
 */
public class Link
{
	private final OutputPin sourcePin;// may not be null
	private final InputPin destPin; // may not be null

	public Link(OutputPin sourcePin, InputPin destPin)
	{
		super();
		if (destPin == null)
			throw new NullPointerException();
		if (destPin.getOwnerNode() == null)
			throw new NullPointerException();
		this.destPin = destPin;

		if (sourcePin == null)
			throw new NullPointerException();
		if (sourcePin.getOwnerNode() == null)
			throw new NullPointerException();
		this.sourcePin = sourcePin;

	}
	

	public Node getDestNode()
	{
		return destPin.getOwnerNode();
	}

	public Node getSourceNode()
	{
		return sourcePin.getOwnerNode();
	}

	public InputPin getDestPin()
	{	return destPin;
	}
	
	public OutputPin getSourcePin()
	{	return sourcePin;
	}
	
}
