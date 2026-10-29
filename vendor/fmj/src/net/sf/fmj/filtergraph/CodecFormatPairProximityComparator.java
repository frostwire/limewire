package net.sf.fmj.filtergraph;

import java.util.Comparator;

import javax.media.Format;

/**
 * Comparator for CodecFormatPair, based on Formats, using FormatProximityComparator.
 * @author Ken Larson
 *
 */
class CodecFormatPairProximityComparator implements Comparator<CodecFormatPair>
{
	private final FormatProximityComparator comparator;

	public CodecFormatPairProximityComparator(final Format target)
	{
		super();
		this.comparator = new FormatProximityComparator(target);
	}
	
	public int compare(CodecFormatPair a, CodecFormatPair b)
	{
		if (a == null && b == null)
			return 0;
		if (a == null)	// then a < b, return -1
			return -1;
		if (b == null)
			return 1;	// a > b
		
		return comparator.compare(a.getFormat(), b.getFormat());
	}
	
	
}
