package net.sf.fmj.filtergraph.model;

/**
 * Exception thrown while parsing in FilterGraphModelIO.
 * @author Ken Larson
 *
 */
public class FilterGraphModelIOException extends Exception
{

	public FilterGraphModelIOException() {
		super();
	}

	public FilterGraphModelIOException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}


	public FilterGraphModelIOException(String arg0) {
		super(arg0);
	}


	public FilterGraphModelIOException(Throwable arg0) {
		super(arg0);
	}

}
