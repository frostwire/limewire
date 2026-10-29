package net.sf.fmj.media;

import java.util.ArrayList;
import java.util.List;

import javax.media.CaptureDeviceInfo;
import javax.media.control.FormatControl;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullBufferDataSource;

/**
 * Merges multiple {@link PullBufferDataSource} that implement {@link CaptureDevice}.
 * @author Ken Larson
 *
 */
public class MergingCaptureDevicePullBufferDataSource extends MergingPullBufferDataSource implements CaptureDevice
{

	public MergingCaptureDevicePullBufferDataSource(List<PullBufferDataSource> sources)
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
