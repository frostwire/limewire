/**
 * @author : Paul Taylor
 * <p/>
 * Version @version:$Id: InvalidAudioFrameException.java,v 1.1 2008/02/06 21:03:24 meverett Exp $
 * Date :${DATE}
 * <p/>
 * Jaikoz Copyright Copyright (C) 2003 -2005 JThink Ltd
 */
package org.jaudiotagger.audio.exceptions;

/**
 * Thrown if portion of file thought to be an AudioFrame is found to not be.
 */
public class InvalidAudioFrameException extends Exception
{
    public InvalidAudioFrameException(String message)
    {
        super(message);
    }
}
