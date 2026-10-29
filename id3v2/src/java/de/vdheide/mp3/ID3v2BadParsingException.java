package de.vdheide.mp3;

import java.io.PrintStream;
import java.io.PrintWriter;

public class ID3v2BadParsingException extends ID3v2Exception {
	/**
	 * Root cause.
	 */
	private final Throwable cause;

	public ID3v2BadParsingException() {
		super("Problem parsing id3v2");
		cause = null;
	}

	public ID3v2BadParsingException(String msg) {
		super(msg);
		cause = null;
	}

	public ID3v2BadParsingException(Throwable cause) {
		super(cause.getMessage());
		this.cause = cause;
	}

	public void printStackTrace() {
		super.printStackTrace();
		if (cause != null) {
			System.err.println("Parent Cause:");
			cause.printStackTrace();
		}
	}

	public void printStackTrace(PrintStream ps) {
		super.printStackTrace(ps);
		if (cause != null) {
			ps.println("Parent Cause:");
			cause.printStackTrace(ps);
		}
	}

	public void printStackTrace(PrintWriter pw) {
		super.printStackTrace(pw);
		if (cause != null) {
			pw.println("Parent Cause:");
			cause.printStackTrace(pw);
		}
	}

}
