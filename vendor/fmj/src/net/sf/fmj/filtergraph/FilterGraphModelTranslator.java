package net.sf.fmj.filtergraph;

import net.sf.fmj.filtergraph.model.FilterGraphModel;
import net.sf.fmj.filtergraph.model.LinkModel;
import net.sf.fmj.filtergraph.model.NodeModel;

/**
 * Converts {@link FilterGraph} to/from {@link FilterGraphModel}.
 * @author Ken Larson
 *
 */
public final class FilterGraphModelTranslator 
{
	private FilterGraphModelTranslator()
	{
		super();
	}
	
	public static FilterGraphModel toModel(FilterGraph g)
	{
		return new FilterGraphModel(toModel(g.getRoot()));
	}
	
	private static NodeModel toModel(Node n)
	{
		if (n == null)
			return null;
		final NodeModel result = new NodeModel(n.getPlugInType(), n.getPlugIn().getClass().getName());
		
		for (int i = 0; i < n.getNumDestLinks(); ++i)
		{
			Link link = n.getDestLink(i);
			result.addDestLink(toModel(link));
		}
		return result;
		
	}
	
	private static LinkModel toModel(Link link)
	{
		return new LinkModel(
				link.getSourcePin().getTrack(), 
				link.getDestPin().getTrack(), 
				link.getDestPin().getFormat(), 
				toModel(link.getDestNode())
				);
	}
	
}
