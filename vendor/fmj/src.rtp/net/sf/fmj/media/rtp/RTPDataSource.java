/*
 * @(#)RTPDataSource.java
 * Created: 27-Oct-2005
 * Version: 1-1-alpha3
 * Copyright (c) 2005-2006, University of Manchester All rights reserved. 
 * Andrew G D Rowley
 * Christian Vincenot <sipcom@cyberspace7.net>
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

import javax.media.Format;
import javax.media.Time;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.rtp.ReceptionStats;

/**
 * Represents an RTP Data Source
 * @author Andrew G D Rowley
 * @author Christian Vincenot
 * @version 1-1-alpha3
 */
public class RTPDataSource extends PushBufferDataSource {
    
    // The RTP data stream for this data source
    private PushBufferStream stream = null;
    
    // True if the data source is connected
    private boolean connected = false;
    
    // True if the data source is started
    private boolean started = false;
    
    /**
     * Creates a new RTPDataSource
     * @param ssrc the SSRC of this datasource
     * @param format The format of the data
     */
    public RTPDataSource(long ssrc, Format format) {
        if (format instanceof VideoFormat) {
            stream = new RTPVideoDataStream(ssrc, format);
        } else if (format instanceof AudioFormat) {
            stream = new RTPAudioDataStream(ssrc, format);
        }
    }

    /**
     * Returns the stream associated with this datasource.
     * @return the stream associated with this datasource
     */
    @Override
	public PushBufferStream[] getStreams() {
        return new PushBufferStream[]{stream};
    }

    /**
     * Returns the content type of this datasource.
     * @return the content type of this datasource ("raw" if connected, null else)
     */
    @Override
	public String getContentType() {
        if (!connected) {
            return null;
        }
        return "raw";
    }

    /**
     * Connect the datasource
     */
    @Override
	public void connect() {
        connected = true;
    }

    /**
     * Disconnect the datasource
     */
    @Override
	public void disconnect() {
        if (started) {
            stop();
        }
        connected = false;
    }

    /**
     * Start using this datasource. The data will start being sent after this call.
     */
    @Override
	public void start()  {
        started = true;
    }

    /**
     * Stop using this datasource. The data won't be sent anymore after this call.
     */
    @Override
	public void stop()
	{
        started = false;
    }

    /**
     * Returns the control associated with the supplied content-type 
     * of this datasource's stream.
     * @param arg0 the content-type
     * @return the control associated with the supplied content-type 
     * of this datasource's stream
     */
    @Override
	public Object getControl(String arg0) {
        return stream.getControl(arg0);
    }

    /**
     * Returns all the controls of this datasource's stream.
     * @return all the controls of this datasource's stream
     */
    @Override
	public Object[] getControls() {
        return stream.getControls();
    }

    /**
     * Returns the duration of this datasource. DUMMY
     * @return always returns DURATION_UNBOUNDED
     */
    @Override
	public Time getDuration() {
        return DURATION_UNBOUNDED;
    }
    
    /**
     * Handles an RTP data packet
     * @param header The RTP header
     * @param data The RTP data
     * @param offset The offset in the data
     * @param length The length of the data
     */
    public void handleRTPPacket(RTPHeader header, byte[] data, int offset,
            int length) {
        if (stream instanceof RTPDataStream) {
            ((RTPDataStream) stream).addPacket(header, data, offset, length);
        }
    }
    
    /**
     * Returns this datasource's current jitter value.
     * @return this datasource's current jitter value
     */
    public long getJitter() {
        if (stream instanceof RTPDataStream) {
            return ((RTPDataStream) stream).getJitter();
        }
        return 0;
    }
    
    /**
     * Returns the reception statistics of the source.
     * @return the reception statistics of the source
     */
    public ReceptionStats getSourceReceptionStats() {
        if (stream instanceof RTPDataStream) {
            return ((RTPDataStream) stream).getReceptionStats();
        }
        return null;
    }

}
