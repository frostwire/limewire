package net.sf.fmj.filtergraph.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.media.Format;
import javax.media.PlugInManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.fmj.filtergraph.Pin;
import net.sf.fmj.utility.FormatArgUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.lti.utils.StringUtils;

/**
 * Converts {@link FilterGraphModel} to/from XML.
 * @author Ken Larson
 *
 */
public class FilterGraphModelIO 
{

	private FilterGraphModelIO()
	{
		super();
	}
	
	
	public static void write(FilterGraphModel g, OutputStream os) throws IOException
	{
		
		os.write("<?xml version='1.0' encoding='utf-8'?>\n".getBytes());
		os.write("<FilterGraph version=\"1.0\">\n".getBytes());
		write(g.getRoot(), Pin.NO_TRACK, Pin.NO_TRACK, null, os, 1);
		
		os.write("</FilterGraph>\n".getBytes());

	}
	
	private static String tabs(int i)
	{
		final StringBuilder b = new StringBuilder();
		for (int j = 0; j < i; ++j)
			b.append('\t');
		return b.toString();
	}
	
	private static void write(String s, OutputStream os) throws IOException
	{
		os.write(s.getBytes());
	}
	private static void writeln(String s, OutputStream os, int depth) throws IOException
	{
		write(tabs(depth) + s + "\n", os);
	}
	
	private static void write(NodeModel n, int sourceTrack, int destTrack, Format destFormat, OutputStream os, int depth) throws IOException
	{
		final String sourceTrackAttr;
		if (sourceTrack != Pin.NO_TRACK)
			sourceTrackAttr = " sourceTrack=\"" + sourceTrack + "\"";
		else
			sourceTrackAttr = "";
		final String destTrackAttr;
		if (destTrack != Pin.NO_TRACK)
			destTrackAttr = " destTrack=\"" + destTrack + "\"";
		else
			destTrackAttr = "";
		final String destTrackFormatAttr;
		if (destFormat != null)
			destTrackFormatAttr = " destFormat=\"" + StringUtils.replaceSpecialXMLChars(FormatArgUtils.toString(destFormat)) + "\"";
		else
			destTrackFormatAttr = "";
		
		final String nodeName = getNodeName(n.getPlugInType());
		
		final boolean oneLiner = n.getNumDestLinks() == 0;
		
		writeln("<" + nodeName + " class=\"" + 
				n.getPlugInClassName() + "\"" + 
				sourceTrackAttr + 
				destTrackAttr + 
				destTrackFormatAttr + 
				(oneLiner ? "/>" : ">"), 
				os, depth);
		
		
		for (int i = 0; i < n.getNumDestLinks(); ++i)
		{
			final LinkModel link = n.getDestLink(i);
			
			write(	link.getDestNode(), 
					link.getSourceTrack(), 
					link.getDestTrack(), 
					link.getDestFormat(), os, depth + 1);
			
			
		}
		
		if (!oneLiner)
			writeln("</" + nodeName + ">", os, depth);
	
	}
	
	public static final FilterGraphModel read(InputStream is) throws IOException, ParserConfigurationException, SAXException, FilterGraphModelIOException
	{
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			
		//Using factory get an instance of document builder
		final DocumentBuilder db = dbf.newDocumentBuilder();
		
		//parse using builder to get DOM representation of the XML file
		final Document dom = db.parse(is);
		
		return parseFilterGraph(dom);
		
	}
	
	private static final FilterGraphModel parseFilterGraph(Document dom) throws FilterGraphModelIOException
	{
		final Element filterGraphElt = dom.getDocumentElement();
		if (!filterGraphElt.getNodeName().equals("FilterGraph"))
			throw new FilterGraphModelIOException("Expected root element to be FilterGraph");
		
		final String version = filterGraphElt.getAttribute("version");
		if (version == null || !version.equals("1.0"))
			throw new FilterGraphModelIOException("Expected version 1.0: " + version);
		
		final List<Element> childElements = getChildElements(filterGraphElt);
		if (childElements.size() != 1)
			throw new FilterGraphModelIOException("Expected one and only one child of FilterGraph element");
				
		return new FilterGraphModel(parseRootNode(childElements.get(0)));
		
	}
	
	private static final List<Element> getChildElements(Element elt)
	{
		final List<Element> result = new ArrayList<Element>();
		for (int i = 0; i < elt.getChildNodes().getLength(); ++i)
		{
			if (elt.getChildNodes().item(i) instanceof Element)
				result.add((Element) elt.getChildNodes().item(i));
		}
		return result;
	}
	
	private static final NodeModel parseRootNode(Element elt) throws FilterGraphModelIOException
	{
		final int plugInType = getPlugInType(elt.getNodeName());
		if (plugInType < 0)
			throw new FilterGraphModelIOException("Unknown plug-in node name: " + elt.getNodeName());
		final String plugInClassName = getRequiredStringAttribute(elt, "class");
	
		
		final NodeModel result = new NodeModel(plugInType, plugInClassName);

		final List<Element> childElements = getChildElements(elt);
		for (Element child : childElements)
		{
			final LinkModel link = parseLink(child);
			result.addDestLink(link);
			
		}
			
		return result;
		
	}
	
	private static final LinkModel parseLink(Element elt) throws FilterGraphModelIOException
	{
		final int plugInType = getPlugInType(elt.getNodeName());
		if (plugInType < 0)
			throw new FilterGraphModelIOException("Unknown plug-in node name: " + elt.getNodeName());
		final String plugInClassName = getRequiredStringAttribute(elt, "class");
		final int sourceTrack = getIntAttribute(elt, "sourceTrack", Pin.NO_TRACK);
		final int destTrack = getIntAttribute(elt, "destTrack", Pin.NO_TRACK);
		final String destFormatStr = getRequiredStringAttribute(elt, "destFormat");
		final Format destFormat;
		try {
			destFormat = FormatArgUtils.parse(destFormatStr);
		} catch (ParseException e) {
			throw new FilterGraphModelIOException("Invalid format: " + destFormatStr, e);
		}
		
		
		final NodeModel destNode = new NodeModel(plugInType, plugInClassName);
		
		final List<Element> childElements = getChildElements(elt);
		for (Element child : childElements)
		{
			final LinkModel link = parseLink(child);
			destNode.addDestLink(link);
			
		}
		
		final LinkModel result = new LinkModel(sourceTrack, destTrack, destFormat, destNode);
		
		return result;
		
		
	}
	
	private static final String getRequiredStringAttribute(Element elt, String attrName) throws FilterGraphModelIOException
	{
		final String result = elt.getAttribute(attrName);
		if (result == null || result.equals(""))
			throw new FilterGraphModelIOException("Missing attribute: " + attrName);
		return result;
	}
	
	private static final int getIntAttribute(Element elt, String attrName, int defaultResult) throws FilterGraphModelIOException
	{
		final String attrValue = elt.getAttribute(attrName);
		if (attrValue == null || attrValue.equals(""))
			return defaultResult;
		try
		{
			return Integer.parseInt(attrValue);
		}
		catch (NumberFormatException e)
		{	throw new FilterGraphModelIOException("Expected integer: " + attrValue, e);
		}
	}

	
	
	private static final String getNodeName(int plugInType)
	{
		switch (plugInType)
		{
			case PlugInManager.DEMULTIPLEXER: return "Demux";
			case PlugInManager.CODEC: return "Codec";
			case PlugInManager.EFFECT: return "Effect";
			case PlugInManager.RENDERER: return "Renderer";
			case PlugInManager.MULTIPLEXER: return "Mux";
			default: throw new IllegalArgumentException();
		
		}
	}
	
	/** returns -1 if invalid. */
	private static final int getPlugInType(String nodeName)
	{
		for (int i = PlugInManager.DEMULTIPLEXER; i <= PlugInManager.MULTIPLEXER; ++i)
			if (nodeName.equals(getNodeName(i)))
				return i;
		
		return -1;
	}
	
	
	// testing only.
	public static void main(String[] args) throws Exception
	{
		FilterGraphModel m = read(new FileInputStream("test.xml"));
		write(m, System.out);
	}
	
}
