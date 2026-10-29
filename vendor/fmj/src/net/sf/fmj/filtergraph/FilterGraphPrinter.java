package net.sf.fmj.filtergraph;

import java.util.logging.Logger;

import net.sf.fmj.utility.LoggerSingleton;

/**
 * Prints filter graphs (to a logger).
 * @author Ken Larson
 *
 */
class FilterGraphPrinter
{
	private static final Logger logger = LoggerSingleton.logger;

	private static String tabs(int i)
	{
		StringBuffer b = new StringBuffer();
		while (i-- >0)
			b.append('\t');
		return b.toString();
	}

	public static void print(Node n, int tabs)
	{
		String trackStr = "";	

		n.print(logger, tabs(tabs) + trackStr);
		
		for (int j = 0; j < n.getNumDestLinks(); ++j)
		{	final Link linkChild = n.getDestLink(j);
			if (linkChild != null)
				print(linkChild, tabs + 1);
		}
	}
	public static void print(Link link, int tabs)
	{
		final Node n = link.getDestNode();
		
		String trackStr = "";	

		// TODO: print out tracks properly
		if (link.getDestPin() != null && link.getDestPin().getTrack() >= 0)
			trackStr = "[Track " + link.getDestPin().getTrack() + " of] ";
		n.print(logger, tabs(tabs) + trackStr);
		
		
		for (int j = 0; j < n.getNumDestLinks(); ++j)
		{	final Link linkChild = n.getDestLink(j);
			if (linkChild != null)
				print(linkChild, tabs + 1);
		}
	
	}
}
