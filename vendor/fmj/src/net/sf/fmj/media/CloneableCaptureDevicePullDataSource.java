package net.sf.fmj.media;

import javax.media.CaptureDeviceInfo;
import javax.media.control.FormatControl;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.PullDataSource;

/**
 * Cloneable {@link PullDataSource} that also implements {@link CaptureDevice}.
 * Note: created clones do not implement CaptureDevice.  This is consistent with JMF.
 * @author Ken Larson
 *
 */
public class CloneableCaptureDevicePullDataSource extends CloneablePullDataSource implements CaptureDevice
{
	private final CaptureDevice captureDevice;
	
	public CloneableCaptureDevicePullDataSource(PullDataSource source)
	{
		super(source);
		this.captureDevice = (CaptureDevice) source;
	}

	public synchronized CaptureDeviceInfo getCaptureDeviceInfo()
	{
		return captureDevice.getCaptureDeviceInfo();
	}

	public synchronized FormatControl[] getFormatControls()
	{
		return captureDevice.getFormatControls();
	}
}
