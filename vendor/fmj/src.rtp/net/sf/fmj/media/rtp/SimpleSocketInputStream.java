/*
 * @(#)SocketInputStream.java
 * Created: 26-Oct-2005
 * Version: TODO
 * Copyright (c) 2005-2006, University of Manchester All rights reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials 
 * provided with the distribution. Neither the name of the University of 
 * Manchester nor the names of its contributors may be used to endorse or 
 * promote products derived from this software without specific prior written
 * permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package net.sf.fmj.media.rtp;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PushSourceStream;
import javax.media.protocol.SourceTransferHandler;

import net.sf.fmj.media.AsyncSourceTransferHandlerNotifier;
import net.sf.fmj.utility.LoggerSingleton;

import com.lti.utils.synchronization.CloseableThread;
import com.lti.utils.synchronization.ProducerConsumerQueue;

/**
 * A DatagramSocket Input Stream Adapter
 * @author Andrew G D Rowley
 * @author Ken Larson
 * @version 1-1-alpha3
 * TODO: changes to this class by kenlars99 basically make it similar to SocketInputStream, which is not currently used.
 * Perhaps it is better to put SimpleSocketInputStream back the way it was, move this code to SocketInputStream, and
 * use SocketInputStream instead of SimpleSocketInputStream.
 */
public class SimpleSocketInputStream extends CloseableThread implements PushSourceStream {
    
	private static final Logger logger = LoggerSingleton.logger;

    // The datagram socket
    private DatagramSocket socket = null;
    
    private boolean eos = false;
    
    private static final int QUEUE_SIZE = 100;
    private final ProducerConsumerQueue q = new ProducerConsumerQueue(QUEUE_SIZE); // holds either DatagramPacket or IOException. 
    /**
     * Creates a new SocketInputStream
     * @param socket The socket to handle
     */
    public SimpleSocketInputStream(DatagramSocket socket) {
        this.socket = socket;
    }

    /**
     * Read a datagram packet and write it into the provided buffer.
     * @param buffer the buffer to write the packets to
     * @param offset offset after which we can write
     * @param length total length
     * @return the packet length or 0 if a problem occured
     */
     // kenlars99: this now follows the contract for PushSourceStream.read.
     // read is supposed to be non-blocking, and we call the transfer handler
     // only when data is actually there.  This code used to call the transfer handler continuously, and block in read.
   public int read(byte[] buffer, int offset, int length) throws IOException {
    	
    	if (eos)
    		return -1;

    	final Object o;
    	try
		{
    		synchronized (q)
    		{
    	    	if (q.isEmpty())
    	    		return 0;
    			o = q.get();
    		}
		} catch (InterruptedException e)
		{
			throw new InterruptedIOException();
		}
    	
    	if (o instanceof DatagramPacket)
    	{
    		final DatagramPacket packet = (DatagramPacket) o;
    		if (length < packet.getLength())
    			throw new IllegalArgumentException("Length must be at least " + packet.getLength());
    		System.arraycopy(packet.getData(), packet.getOffset(), buffer, offset, packet.getLength());
    		logger.finer("packet length: " + packet.getLength());
    		return packet.getLength();
    	}
    	else if (o instanceof IOException)
    	{	throw (IOException) o;
    	}
    	else
    	{	throw new RuntimeException("Unknown object in queue: " + o);
    	}
    	
    }

    /**
     * Returns the minimum transfer size.
     * @return 2048 by default, since we've got no way to retrieve the MTU
     */
    public int getMinimumTransferSize() {
        // There is currently no way to get the MTU in Java, so return a 
        // suitable large value
        return 2048;
    }

    private final AsyncSourceTransferHandlerNotifier asyncBufferTransferHandlerNotifier = new AsyncSourceTransferHandlerNotifier(this);
    /**
     * Sets the transfer handler for this stream.
     * @param transferHandler the transfer handler
     */
    public void setTransferHandler(SourceTransferHandler transferHandler)
    {
        asyncBufferTransferHandlerNotifier.setTransferHandler(transferHandler);
    }
    public void notifyTransferHandlerAsync() throws InterruptedException
    {
		asyncBufferTransferHandlerNotifier.notifyTransferHandlerAsync();
    }
	
    /**
     * Returns the content descriptor. DUMMY.
     * Needed by JMF.
     * @return null
     */
    public ContentDescriptor getContentDescriptor() {
        return null;
    }

    /**
     * Returns the content's length.
     * Needed by JMF, always returns LENGTH_UNKNOWN in our case.
     * @return LENGTH_UNKNOWN
     */
    public long getContentLength() {
        return LENGTH_UNKNOWN;
    }

    /**
     * Have we reached the end of stream?
     * @return true if we're finished with this stream
     */
    public boolean endOfStream() {
        return eos;
    }

    /**
     * DUMMY.
     * @return EMPTY ARRAY
     */
    public Object[] getControls() {
        return new Object[0];
    }

    /**
     * DUMMY.
     * @param controlType UNUSED.
     * @return null
     */
    public Object getControl(String controlType) {
        return null;
    }
    
    /**
     * Walk...
     */
    @Override
	public void run() {
    	
    	try
    	{
	    	while (!isClosing())
	    	{
	    		// TODO: avoid memory allocatioin for each packet:
	    		final byte[] data = new byte[getMinimumTransferSize()];
	    		final DatagramPacket packet = new DatagramPacket(data, data.length);
	            try
				{
					socket.receive(packet);
					
					synchronized (q)
					{
						if (q.isFull())
						{	logger.fine("Dropping buffered UDP packet");
							q.get();	// drop packet
						}
						q.put(packet);
					}
					
					notifyTransferHandlerAsync();
		            
				} catch (IOException e)
				{
					logger.log(Level.FINE, "" + e, e);
					synchronized (q)
					{
						if (q.isFull())
						{	logger.fine("Dropping buffered UDP packet");
							q.get(); // drop packet	
						}
						q.put(e);
					}
					eos = true;
					break;
				}
	            
	            
	    	}
    	}
    	catch (InterruptedException e)
    	{
    	}
    	finally
    	{	
    		setClosed();
    	}

    }
    
    /**
     * Stops the socket
     */
    public void kill() {
    	asyncBufferTransferHandlerNotifier.dispose();
    	close();
        socket.close();
        
    }
}
