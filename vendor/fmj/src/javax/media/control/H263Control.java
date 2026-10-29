package javax.media.control;

import javax.media.Control;

/**
 * Standard JMF class -- see <a href="http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/control/H263Control.html" target="_blank">this class in the JMF Javadoc</a>.
 * Complete.
 * @author Ken Larson
 *
 */
public interface H263Control extends Control
{
	public boolean isUnrestrictedVectorSupported();
	public boolean setUnrestrictedVector(boolean newUnrestrictedVectorMode);
	public boolean getUnrestrictedVector();
	public boolean isArithmeticCodingSupported();
	public boolean setArithmeticCoding(boolean newArithmeticCodingMode);
	public boolean getArithmeticCoding();
	public boolean isAdvancedPredictionSupported();
	public boolean setAdvancedPrediction(boolean newAdvancedPredictionMode);
	public boolean getAdvancedPrediction();
	public boolean isPBFramesSupported();
	public boolean setPBFrames(boolean newPBFramesMode);
	public boolean getPBFrames();
	public boolean isErrorCompensationSupported();
	public boolean setErrorCompensation(boolean newtErrorCompensationMode);
	public boolean getErrorCompensation();
	public int getHRD_B();
	public int getBppMaxKb();
}
