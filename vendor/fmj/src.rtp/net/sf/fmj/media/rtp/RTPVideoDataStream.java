/*
 * @(#)RTPDataStream.java
 * Created: 27-Oct-2005
 * Version: 1-1-alpha3
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

import javax.media.Buffer;
import javax.media.Format;
import javax.media.protocol.PushBufferStream;

/**
 * Represents a stream sent via RTP
 * 
 * @author Andrew G D Rowley
 * @author Christian Vincenot
 * @version 1-1-alpha3
 */
public class RTPVideoDataStream extends RTPDataStream implements PushBufferStream
{
    private double durationMultiplier;

    /**
     * Creates a new RTPDataStream
     * @param ssrc the ssrc of this RTP stream
     * @param format The format of the data
     */
    public RTPVideoDataStream(long ssrc, Format format)
    {
        super(ssrc, format);
        
        clockRate = 90000; // hardwired setting see RFC3551
        
        durationMultiplier = 1000.0d / clockRate;
    }
    
    /**
     * Adds a packet to the buffers
     * @param header The header of the packet
     * @param data The data of the packet
     * @param offset The offset of the packet
     * @param length The length of the packet
     */
    @Override
    protected void addPacket(RTPHeader header, byte[] data, int offset, int length)
    {
        packetSize = length;
        
        int sequence = header.getSequence();
        long timestamp = header.getTimestamp();
        calculateJitter(timestamp);
        
        Buffer buffer = new Buffer();
        buffer.setHeader(header);
        buffer.setData(data);
        buffer.setOffset(offset);
        buffer.setLength(length);
        buffer.setTimeStamp(timestamp);
        buffer.setSequenceNumber(sequence);

        buffer.setDuration(Math.round((double)length * durationMultiplier * 1000000.0d));

        int flags = Buffer.FLAG_RTP_TIME | Buffer.FLAG_RELATIVE_TIME;
        if (header.getMarker() == 1)
        {
            flags |= Buffer.FLAG_RTP_MARKER;
        }
        buffer.setFlags(flags);

        if ( null != handler )
        {
            try
            {
                itsRTPBuffer.addWait(buffer);
                handler.transferData(RTPVideoDataStream.this);
            }
            catch ( InterruptedException ex )
            {
            }
        }
    }
    
    /**
     * Fill the provided buffer with the available data.
     * @param buffer the buffer to fill
     */
    @Override
    public void read(Buffer buffer)
    {
        try
        {
            Buffer data = itsRTPBuffer.removeWait();
            
            if ((data != null) && (buffer != null))
            {
                buffer.setData(data.getData());
                buffer.setOffset(data.getOffset());
                buffer.setLength(data.getLength());
                buffer.setTimeStamp(data.getTimeStamp());
                buffer.setSequenceNumber(data.getSequenceNumber());
                buffer.setFlags(data.getFlags());
                buffer.setFormat(format);
                
                // mgodehardt: will measure thruput in bits per second for the BitRateControl
                // dont know if this the right place to measure the thruput
                long currentTimestamp = System.nanoTime();
                if ( -1 == lastTimestamp )
                {
                    lastTimestamp = currentTimestamp;
                }
                
                bytesProcessed += data.getLength();
                
                if ( (currentTimestamp - lastTimestamp) > 1000000000L )
                {
                    bitsPerSecond = bytesProcessed << 3;
                    bytesProcessed = 0;
                    lastTimestamp = currentTimestamp;
                }
            }
        }
        catch ( InterruptedException ex )
        {
        }
    }
}
