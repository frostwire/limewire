/**
 * 
 */
package net.sf.fmj.filtergraph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.PlugIn;
import javax.media.ResourceUnavailableException;

import net.sf.fmj.utility.LoggerSingleton;

/**
 * Base class for all filter graph nodes.
 * @author Ken Larson
 *
 */
public abstract class Node
{
	protected static final Logger logger = LoggerSingleton.logger;
	
	/** Outgoing links. */
	private List<Link> destLinks = new ArrayList<Link>();
	
	private final InputPin[] inputPins;
	private final OutputPin[] outputPins;

	private final PlugIn plugIn;
	
	/** Subclass constructors must actually set the pins using setInputPin and setOutputPin.  
	 * this constructor only allocates the arrays. */
	public Node(final PlugIn plugIn, int numInputPins, int numOutputPins)
	{
		super();
		this.plugIn = plugIn;
		this.inputPins = new InputPin[numInputPins];
		this.outputPins = new OutputPin[numOutputPins];
	}
	
	public Link getDestLink(InputPin pin)
	{
		for (Link link : destLinks)
			if (link.getDestPin() == pin)
				return link;
		
		return null;
	}
	
	public Link getDestLink(OutputPin pin)
	{
		for (Link link : destLinks)
			if (link.getSourcePin() == pin)
				return link;
		
		return null;
	}
	
	public boolean removeDestLink(Link link)
	{
		return destLinks.remove(link);
		
	}

	
	public InputPin getInputPin(int index)
	{	if (inputPins[index].getOwnerNode() == null)
			throw new NullPointerException();
		return inputPins[index];
	}

	public InputPin getInputPinByTrack(int track)
	{	for (InputPin p : inputPins)
		{
			if (p.getTrack() == track)
			{	if (p.getOwnerNode() == null)
					throw new NullPointerException();
			
				return p;
			}
		}
		throw new IllegalArgumentException("" + track);
	}

	public OutputPin getOutputPinByTrack(int track)
	{	for (OutputPin p : outputPins)
		{
			if (p.getTrack() == track)
			{	if (p.getOwnerNode() == null)
					throw new NullPointerException();
			
				return p;
			}
		}
		throw new IllegalArgumentException("" + track);
	}

	
	public int getNumInputPins()
	{	return inputPins.length;
	}
	
	public OutputPin getOutputPin(int index)
	{	return outputPins[index];
	}
	public int getNumOutputPins()
	{	return outputPins.length;
	}

	
	protected void setInputPin(int index, InputPin pin)
	{	if (pin.getOwnerNode() == null)
			throw new NullPointerException();
		inputPins[index] = pin;
	}
	protected void setOutputPin(int index, OutputPin pin)
	{	if (pin.getOwnerNode() == null)
			throw new NullPointerException();
		outputPins[index] = pin;
	}

	public int getNumDestLinks()
	{	return destLinks.size();
	}
	
	public Link getDestLink(int i)
	{
		try 
		{
			return destLinks.get(i);
		}
		catch (IndexOutOfBoundsException e) 
		{
			logger.log(Level.WARNING, "" + e, e);
			return null; // TODO: better to just rethrow?
		}
	}
	
	public Buffer getOutputBuffer(int i)
	{
		try 
		{
			return outputPins[i].getBuffer();
		}
		catch (IndexOutOfBoundsException e) 
		{
			logger.log(Level.WARNING, "" + e, e);
			return null; // TODO: better to just rethrow?
		}
	}
	
	public void setOutputBuffer(int i, Buffer b)
	{
		outputPins[i].setBuffer(b);
	}
	
	public void addDestLink(Link n)
	{
		destLinks.add(n);
	}
	
	public abstract Node duplicate();
	
	public void open() throws ResourceUnavailableException
	{
		getPlugIn().open();
	}
	
	public final void close()
	{
		getPlugIn().close();
	}
	
	/** sourceTrackNumber only used for demux, and destTrackNumber only used for mux. */
	public abstract int process(Buffer input, int sourceTrackNumber, int destTrackNumber, int flags);
	
	/**
	 * Intended to be called by subclass implementations of duplicate, to duplicate the destinations.
	 * @param result FilterGraphNode that is to be returned by duplicate();
	 */
	protected Node propagateDuplicate(Node result)
	{
		for (int i = 0; i < getNumDestLinks(); ++i)
		{
			final Link link = getDestLink(i);
			if (link != null) 
			{
				if (link.getDestNode() == null)
					throw new NullPointerException("link.getDestNode()");
				if (link.getDestPin() == null)
					throw new NullPointerException("link.getDestPin(), link.getDestNode()=" + link.getDestNode());
				
				// TODO: this assumes that each node has max 1 input:
				result.addDestLink(new Link(result.getOutputPin(link.getSourcePin().getPinNumber()), link.getDestNode().duplicate().getInputPin(link.getDestPin().getPinNumber())));
			}
			else
			{	result.addDestLink(null);
			}
		}
		return result;
	}
	
	public void print(Logger logger, String prefix)
	{
		final String inputFormatStr = getInputFormat() == null ? "" : (getInputFormat().getClass().getSimpleName() + " [" + getInputFormat() + "]");
		logger.info(prefix + inputFormatStr);
		logger.info(prefix + getPlugIn().getClass().getName());
	}
	
	public abstract Format getInputFormat();
	
	public final PlugIn getPlugIn()
	{	return plugIn;
	}

	public void start() throws IOException
	{	// do nothing by default
	}
	
	public void stop()
	{	// do nothing by default
	}
	
	// node implements the actual setting of input/output formats on the pins:
	abstract Format setPlugInInputFormat(InputPin pin, Format format);
	abstract Format setPlugInOutputFormat(OutputPin pin, Format format);
	
	public abstract int getPlugInType();
	
}