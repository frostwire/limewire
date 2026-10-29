package net.sf.fmj.filtergraph.model;

import java.util.ArrayList;
import java.util.List;

public class NodeModel {

	private final int plugInType;
	private final String plugInClassName;
	
	private final List<LinkModel> destLinks = new ArrayList<LinkModel>();

	public NodeModel(final int plugInType, final String plugInClassName) {
		super();
		this.plugInType = plugInType;
		this.plugInClassName = plugInClassName;
	}

	public String getPlugInClassName() {
		return plugInClassName;
	}

	public int getPlugInType() {
		return plugInType;
	}

	public int getNumDestLinks()
	{	return destLinks.size();
	}
	
	public LinkModel getDestLink(int i)
	{
		return destLinks.get(i);
	}
	
	public void addDestLink(LinkModel link)
	{
		destLinks.add(link);
	}
}
