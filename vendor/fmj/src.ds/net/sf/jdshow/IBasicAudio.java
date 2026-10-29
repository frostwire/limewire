package net.sf.jdshow;

public class IBasicAudio extends IUnknown{

	public IBasicAudio(long ptr) {
		super(ptr);
	}
	
	/**
	 * Volume ranges from -10,000 to 0.  The bottom half of the range is essentially muted.
	 */
	public native int put_Volume(long volume);
	
	public native int get_Volume(long[] volume);
	
	static native GUID Init_IID(GUID guid);
}
