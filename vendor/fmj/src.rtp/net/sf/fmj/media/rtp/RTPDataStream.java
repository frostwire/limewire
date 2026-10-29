/*
 * @(#)RTPDataStream.java
 * Created: 01-Dec-2005
 * Version: 1-1-alpha3
 * Copyright (c) 2005-2006, University of Manchester All rights reserved. 
 *
 * Modified by Christian Vincenot <sipcom@cyberspace7.net>
 * Conforming to RFC-3550
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

import java.awt.Component;
import java.io.IOException;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.control.BitRateControl;
import javax.media.control.BufferControl;
import javax.media.format.VideoFormat;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PushBufferStream;
import javax.media.rtp.GlobalReceptionStats;
import javax.media.rtp.RTPControl;
import javax.media.rtp.ReceptionStats;

/**
 * A generic RTP Data Stream
 * @author Andrew G D Rowley
 * @author Christian Vincenot
 * @version 1-1-alpha3
 */
public abstract class RTPDataStream implements PushBufferStream, RTPControl {
    
    // The format of the data
    protected Format format = null;
    
    // The clock rate of the RTP clock for the format
    protected double clockRate = 90000;
    protected int packetSize = -1;
    
    // The transfer Handler
    protected BufferTransferHandler handler = null;
    
    // The ssrc of the stream
    protected long ssrc = 0;
    
    // The threshold of the buffer before any playout is done in ns
    protected long threshold;
    
    // The controls of the stream
    protected Object[] controls = new Object[] {this, new RTPDataStreamBufferControl(), new RTPDataStreamRateControl()};
    
    // The reception statistics
    protected RTPReceptionStats receptionStats = new RTPReceptionStats();
    
    // The inter-arrival jitter of the packets
    protected long jitter = 0;
    
    // the jitter buffer
    protected RTPBuffer itsRTPBuffer;
    protected int rtpbufferlen = 3;
    protected long rtpbufferlenMS = -1;
    
    // The last packet delay time
    private long lastDelay = -1;
    
    // The time of arrival of the last rtp packet
    private long lastRTPReceiveTime = -1;
    
    // The rtp timestamp of the last rtp packet
    private long lastRTPTimestamp = -1;
    
    // mgodehardt: for the BitRateControl, it will count the bytes processed
    protected long lastTimestamp = -1;
    protected int bytesProcessed;
    protected int bitsPerSecond;

    /**
     * Creates a new RTPDataStream
     * @param ssrc the SSRC associated with this datastream
     * @param format The format of the stream
     */
    public RTPDataStream(long ssrc, Format format)
    {
        this.ssrc = ssrc;
        this.format = format;
        
        itsRTPBuffer = new RTPBuffer(rtpbufferlen, receptionStats);
    }

    /**
     * Add an RTP packet to this datastream.
     * @param header the RTP header
     * @param data the data
     * @param offset the offset
     * @param length the length
     */
    protected abstract void addPacket(RTPHeader header, byte[] data, 
            int offset, int length);
    
    /**
     * Returns the format carried by this datastream.
     * @return the format of this datastream
     */
    public Format getFormat() {
        return format;
    }

    /**
     * Sets the handler responsible for treating the incoming data.
     * @param transferHandler the object handling the data
     */
    public void setTransferHandler(BufferTransferHandler transferHandler) {
        this.handler = transferHandler;
    }

    /**
     * Returns the content descriptor of this stream.
     * @return the content descriptor of this stream - the RAW_RTP content descriptor
     */
    public ContentDescriptor getContentDescriptor() {
        return new ContentDescriptor(ContentDescriptor.RAW_RTP);
    }

    /**
     * Returns the content's length.
     * @return the content's length
     */
    public long getContentLength() {
        return LENGTH_UNKNOWN;
    }

    /**
     * Indicates if we're reached the end of stream.
     * @return FALSE
     */
    public boolean endOfStream() {
        return false;
    }

    /**
     * Returns all the possible controls of this datastream.
     * @return all the possible controls of this datastream
     */
    public Object[] getControls() {
        return controls;
    }

    /**
     * Returns the control specific to this content type.
     * @param controlType the control type
     * @return the control (this stream for RTPControl, else null)
     */
    public Object getControl(String controlType) {
        if (controlType.equals("javax.media.rtp.RTPControl")) {
            return this;
        }
        return null;
    }

    /**
     * Reads the data available into the buffer.
     * @param buffer the buffer in which to put the read data
     * @throws java.io.IOException I/O Exception
     */
    public abstract void read(Buffer buffer) throws IOException;

    /**
     * Add the specified format as corresponding to the specified payload.
     * DOES NOTHING
     * @param fmt format
     * @param payload payload type
     */
    public void addFormat(Format fmt, int payload) {
        // Does Nothing
    }

    /**
     * Returns the reception stats.
     * @return the reception stats
     */
    public ReceptionStats getReceptionStats() {
        return receptionStats;
    }

    /**
     * Returns the global stats.
     * @return the global stats
     */
    public GlobalReceptionStats getGlobalStats() {
        return null;
    }

    /**
     * Returns the list of formats supported.
     * @return the list of formats supported
     */
    public Format[] getFormatList() {
        return new Format[0];
    }

    /**
     * Returns the format corresponding to this payload type.
     * @param payload the payload type
     * @return the format corresponding to this payload type
     */
    public Format getFormat(int payload) {
        return format;
    }

    /**
     * Returns the control component.
     * NULL
     * @return null
     */
    public Component getControlComponent() {
        return null;
    }
    
    /**
     * Performs jitter calculations
     * (Conforming to RFC-3550 6.4.1)
     * @param rtpTimestamp the RTP timestamp present in the packet
     */
    protected void calculateJitter(long rtpTimestamp)
    {
        // mogdehardt: interarrival jitter is calculated ( RFC 3550 Page 38)
        // jitter in a receiver report is represented as a unsigned value, measured in sampling units !

        // short explanation of jitter
        // when a sender send two packets 50ms apart and the receiver gets them 60ms apart, jittter is 10ms

        // we need a high precision timer ( we are measuring only relative time )
        long now = (System.nanoTime() / 1000000L);
        
        if ( (lastRTPTimestamp != -1) && (lastRTPReceiveTime != -1) )
        {
            // ULAW/PCMU has 8000 sampling units per second
            
            // diff between packets send ( in sampling units )
            double rtpDiffTimestamp = rtpTimestamp - lastRTPTimestamp;
            
            // diff between packets received (calculated in sampling units)
            double rtpDiffReceiveTime = ((double)(now - lastRTPReceiveTime) * clockRate) / 1000.0d;
            
            long delta = Math.abs(Math.round(rtpDiffReceiveTime - rtpDiffTimestamp));
            ///System.out.println("### delta=" + delta + " jitter=" + jitter);
            
            jitter = jitter + Math.round((double)(delta - jitter) / 16.0d);
        }

        lastRTPTimestamp = rtpTimestamp;
        lastRTPReceiveTime = now;
    }
    
    /**
     * Returns the current jitter calculated.
     * @return the current jitter calculated
     */
    public long getJitter()
    {
        return jitter;
    }

    public long getJitterBufferSize()
    {
        return itsRTPBuffer.getMaxItems();
    }

    public long getJitterBufferItemCount()
    {
        return itsRTPBuffer.getItemCount();
    }
    
    private class RTPDataStreamBufferControl implements BufferControl
    {
        public long getBufferLength()
        {
            return rtpbufferlenMS;
        }
        
        public long setBufferLength(long time)
        {
            rtpbufferlenMS = time;
            
            if ( format instanceof VideoFormat )
            {
                rtpbufferlen = (int)Math.round((double)time / 50.0d);
            }
            else
            {
                int bytesToBuffer = (int)(rtpbufferlenMS * (clockRate / 1000));
                
                rtpbufferlen = bytesToBuffer / packetSize;
                if ( rtpbufferlen < 1 )
                {
                    rtpbufferlen = 1;
                }
            }
            
            itsRTPBuffer.resize(rtpbufferlen);
            
            return rtpbufferlenMS;
        }
        
        public long getMinimumThreshold()
        {
            return -1;
        }
        
        public long setMinimumThreshold(long time)
        {
            return -1;
        }
        
        public void setEnabledThreshold(boolean b)
        {
        }
        
        public boolean getEnabledThreshold()
        {
            return false;
        }
        
        public java.awt.Component getControlComponent()
        {
            return null;
        }
    }

    // mgodehardt: returns the current thruput, for e.g. if this drops below 64000 bps with ULAW RTP, then
    // the receiver will have have hipcups in the stream, this is a read only control
    private class RTPDataStreamRateControl implements BitRateControl
    {
        public int getBitRate()
        {
            return bitsPerSecond;
        }
        
        public int setBitRate(int bitrate)
        {
            return -1;
        }
        
        public int getMinSupportedBitRate()
        {
            return -1;
        }
        
        public int getMaxSupportedBitRate()
        {
            return -1;
        }
        
        public java.awt.Component getControlComponent()
        {
            return null;
        }
    }
}
