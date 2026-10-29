package net.sf.fmj.filtergraph;

import java.io.IOException;
import java.io.OutputStream;

import net.sf.fmj.filtergraph.model.FilterGraphModel;
import net.sf.fmj.filtergraph.model.FilterGraphModelIO;

/**
 * Converts {@link FilterGraph} to/from XML.
 * @author Ken Larson
 *
 */
public final class FilterGraphIO 
{

	private FilterGraphIO()
	{
		super();
	}
	
	
	public static void write(FilterGraph g, OutputStream os) throws IOException
	{
		final FilterGraphModel m = FilterGraphModelTranslator.toModel(g);
		FilterGraphModelIO.write(m, os);
	}
	
	
}
