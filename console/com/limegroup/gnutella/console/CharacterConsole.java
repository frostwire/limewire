package com.limegroup.gnutella.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * interface that represents a character console device
 * such as a command prompt, IM interface, etc.
 */
public interface CharacterConsole {
	public PrintStream getOutput() throws IOException;
	public BufferedReader getInput() throws IOException;
	public boolean isOpen();
	public void close();
}
