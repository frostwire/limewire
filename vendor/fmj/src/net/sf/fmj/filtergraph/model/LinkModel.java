package net.sf.fmj.filtergraph.model;

import javax.media.Format;

public class LinkModel {

	private final int sourceTrack;
	private final int destTrack;
	private final Format destFormat;
	private final NodeModel destNode;

	public LinkModel(final int sourceTrack, final int destTrack, final Format destFormat, final NodeModel destNode) {
		super();
		this.sourceTrack = sourceTrack;
		this.destTrack = destTrack;
		this.destFormat = destFormat;
		this.destNode = destNode;
	}

	public Format getDestFormat() {
		return destFormat;
	}

	public NodeModel getDestNode() {
		return destNode;
	}

	public int getDestTrack() {
		return destTrack;
	}

	public int getSourceTrack() {
		return sourceTrack;
	}
	
}
