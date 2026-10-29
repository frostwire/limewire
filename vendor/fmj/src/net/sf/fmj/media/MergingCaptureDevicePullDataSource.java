package net.sf.fmj.media;

import java.util.ArrayList;
import java.util.List;

import javax.media.CaptureDeviceInfo;
import javax.media.control.FormatControl;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullDataSource;

/**
 * Merges multiple {@link PullDataSource} that implement {@link CaptureDevice}.
 * @author Ken Larson
 *
 */
public class MergingCaptureDevicePullDataSource extends MergingPullDataSource implements CaptureDevice
{

	public MergingCaptureDevicePullDataSource(List<PullDataSource> sources)
	{
		super(sources);
		for (DataSource source : sources)
		{	if (!(source instanceof CaptureDevice))
				throw new IllegalArgumentException();
		}
	}

	public CaptureDeviceInfo getCaptureDeviceInfo()
	{
		throw new UnsupportedOperationException(); // TODO
	}

	public FormatControl[] getFormatControls()
	{
		final List<FormatControl> formatControls = new ArrayList<FormatControl>();
		for (DataSource source : sources)
		{	for (FormatControl formatControl : ((CaptureDevice) source).getFormatControls())
				formatControls.add(formatControl);
		}
		return formatControls.toArray(new FormatControl[0]);
	}
}
