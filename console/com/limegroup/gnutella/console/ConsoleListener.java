package com.limegroup.gnutella.console;

/**
 * an Entity listening for input arriving on various consoles  
 */
public interface ConsoleListener {
	
	/**
	 * @param line a command the user entered
	 * @param which the console the command arrived on 
	 * (and where the output *should* be sent)
	 */
	public void inputArrived(CharacterConsole which, String line);
	
	
	public void sessionStarted(CharacterConsole which);
	public void sessionEnded(CharacterConsole which);
	
	/**
	 * @return if the listener is ready for the next command/more input
	 */
	public boolean isReady();
}
