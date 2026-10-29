/*
 * @(#)RTPSendStream.java
 * Created: 29-Oct-2005
 * Version: 1-1-alpha3
 * Copyright (c) 2005-2006, University of Manchester All rights reserved. 
 * 
 * Andrew G D Rowley
 * Christian Vincenot <sipcom@cyberspace7.net>
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
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.Buffer;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.protocol.PushDataSource;
import javax.media.protocol.PushSourceStream;
import javax.media.protocol.SourceTransferHandler;
import javax.media.rtp.OutputDataStream;
import javax.media.rtp.Participant;
import javax.media.rtp.SendStream;
import javax.media.rtp.TransmissionStats;
import javax.media.rtp.rtcp.SenderReport;
import javax.media.rtp.rtcp.SourceDescription;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;

import net.sf.fmj.utility.LoggerSingleton;


/**
 * Represnts an RTP sending stream
 * @author Andrew G D Rowley
 * @author Christian Vincenot
 * @version 1-1-alpha3
 */
public class RTPSendStream implements SendStream, 
        SourceTransferHandler, BufferTransferHandler {

	private static final Logger logger = LoggerSingleton.logger;

    // The ssrc of the stream
    private long ssrc = 0;
    
    // The data source being sent
    private DataSource dataSource = null;
    
    // The rtp output stream
    private OutputDataStream rtpDataStream = null;
    
    // The source description objects
    private HashMap sourceDescriptions = new HashMap();
    
    // The size of the sdes items
    private int sdesSize = 0;
    
    // The index of the stream to use from the data source
    private int index = 0;
    
    // The rtp format being sent
    private int format = 0;
    
    // The clock rate of the format
    private double clockRate = 90000;
    
    // RTPSessionManager
    private RTPSessionMgr rtpMgr;
    
    // True if the source has been started
    private boolean started = false;
    
    // The local participant
    private RTPLocalParticipant participant = null;
    
    // The buffer used to send the data
    private byte[] buffer = new byte[0];
    
    // The last sequence number
    /* The sequence number must be truly random und unpredictable to avoid 
     * TCP-like ISN attacks, that's why we use the output from our 
     * SSRCGenerator class (which uses SecureRandom to generate a 
     * cryptographically strong pseudo-random number).
     */
    private int lastSequence = SSRCGenerator.nextSecuredRandomShort();
    
    // The last time at which a value was sent
    private long lastSendTime = -1;
    private long initialSendTime = -1;
    
    // The last timestamp sent
    /* Conforming to the RFC, the initial value must be truly random like the 
     * sequence number 
     */
    private long lastTimestamp;
    private long initialTimestamp;
    private long lastBufferTimestamp;
    private long initialBufferTimestamp;
    
    // The first byte of the rtp header
    private byte header0 = (byte) 0x80;
    
    // The transmission statistics
    private RTPTransmissionStats stats = new RTPTransmissionStats();
    
    /**
     * Creates a new RTPSendStream
     * @param index the index of the stream in the datasource's stream table
     * @param participant the participant at the other end of this stream
     * @param format the format used in this stream
     * @param clockRate the encoding clockrate
     * @param rtpMgr the RTP session manager
     * @param ssrc The ssrc of the stream
     * @param dataSource The datasource of the stream
     * @param rtpDataStream The rtp output
     */
    public RTPSendStream(long ssrc, DataSource dataSource, 
            OutputDataStream rtpDataStream, int index, 
            RTPLocalParticipant participant, int format, double clockRate, RTPSessionMgr rtpMgr) {
        this.ssrc = ssrc;
        if (dataSource instanceof PushDataSource)
        	logger.warning("RTPSendStream initialized with PushDataSource");
        this.dataSource = dataSource;
        this.rtpDataStream = rtpDataStream;
        this.index = index;
        this.participant = participant;
        this.format = format;
        this.clockRate = clockRate;
        this.rtpMgr = rtpMgr;
        
        // rtcp packets use in the "rtp timestamp" the same initial value as rtp packets ( see RFC 3550 Page 37 )
        initialTimestamp = SSRCGenerator.nextSecuredRandomInt();
        lastTimestamp = initialTimestamp;
        
        addSourceDescription(
                new SourceDescription(SourceDescription.SOURCE_DESC_CNAME, 
                        participant.getCNAME(), 1, false));
        addSourceDescription(
                new SourceDescription(SourceDescription.SOURCE_DESC_NAME, 
                        participant.getCNAME(), 1, false));
    }
    
    /**
     * Adds a source description to this send stream
     * @param sdes The description to add
     */
    public void addSourceDescription(SourceDescription sdes) {
        SourceDescription oldSdes = 
            (SourceDescription) sourceDescriptions.get(
                    new Integer(sdes.getType()));
        if (oldSdes != null) {
            sdesSize -= oldSdes.getDescription().length();
            sdesSize -= 2;
        }
        sourceDescriptions.put(new Integer(sdes.getType()), sdes);
        sdesSize += 2;
        sdesSize += sdes.getDescription().length();
    }

    /**
     * Add an RTP Source Description (SDES) refering to this stream.
     * @param sourceDesc an RTP Source Description (SDES) refering to this stream
     */
    public void setSourceDescription(SourceDescription[] sourceDesc) {
        for (int i = 0; i < sourceDesc.length; i++) {
            addSourceDescription(sourceDesc[i]);
        }
    }

    /**
     * Closes this RTP stream.
     */
    public void close() {
        logger.fine("RTPSendStream closing");
        if (started) {
            try
			{
				stop();
			} catch (IOException e)
			{
				logger.log(Level.WARNING, "" + e, e);
			}
        }
    }


    /**
     * Stops this RTP stream.
     * @throws java.io.IOException I/O Exception
     */
    public void stop() throws IOException
    {
    	if (started)
    	{
            // we must call stop on the datasource, see http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/rtp/SendStream.html#start()
            dataSource.stop();	
    	}
        started = false;
    }

    /**
     * Starts this RTP stream.
     * @throws java.io.IOException I/O Exception
     */
    public void start() throws IOException
    {
        if (!started) {
            if (dataSource instanceof PushBufferDataSource) {
                logger.fine("RTPSendStream started with PushBufferDataSource");
                PushBufferStream[] streams = 
                    ((PushBufferDataSource) dataSource).getStreams();
                streams[index].setTransferHandler(this);
                
            } else if (dataSource instanceof PushDataSource) {
            	logger.warning("RTPSendStream started with PushDataSource");
            	PushSourceStream[] streams = 
                    ((PushDataSource) dataSource).getStreams();
                streams[index].setTransferHandler(this);
            } else throw new IOException("Unknown datasource: " + dataSource);
            
            // we must call start on the datasource, see http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/rtp/SendStream.html#start()
            dataSource.start();	
            
            // mgodehardt: missing started=true
            started = true;
        }
    }

    /**
     * Set the birate of this stream's samples. DUMMY.
     * @param bitRate the bitrate used
     * @return -1
     */
    public int setBitRate(int bitRate) {
        return -1;
    }

    /**
     * Returns the datasource transmission stats.
     * @return the datasource transmission stats
     */
    public TransmissionStats getSourceTransmissionStats() {
        return stats;
    }

    /**
     * Returns the participant associated with this RTP stream.
     * @return the participant associated with this RTP stream
     */
    public Participant getParticipant() {
        return participant;
    }

    /**
     * Returns the last RTCP SR. DUMMY.
     * @return null
     */
    public SenderReport getSenderReport() {
        return null;
    }

    /**
     * Returns the SSRC used in this stream.
     * @return the SSRC of this stream
     */
    public long getSSRC() {
        return ssrc;
    }

    /**
     * Returns the source sending data to this stream.
     * @return the source sending data to this stream
     */
    public DataSource getDataSource() {
        return dataSource;
    }
    
    private void writeHeaderToBuffer(boolean marker, long timestamp) {
        // Write the marker bit and packet type
        buffer[0] = header0;
        buffer[1] = (byte) (format & 0xFF);
        if (marker) {
            buffer[1] |= 0x80;
        }
        buffer[2] = (byte) ((lastSequence >> 8) & 0xFF);
        buffer[3] = (byte) (lastSequence & 0xFF);
        buffer[4] = (byte) ((timestamp >> 24) & 0xFF);
        buffer[5] = (byte) ((timestamp >> 16) & 0xFF);
        buffer[6] = (byte) ((timestamp >> 8) & 0xFF);
        buffer[7] = (byte) (timestamp & 0xFF);
        buffer[8] = (byte) ((ssrc >> 24) & 0xFF);
        buffer[9] = (byte) ((ssrc >> 16) & 0xFF);
        buffer[10] = (byte) ((ssrc >> 8) & 0xFF);
        buffer[11] = (byte) (ssrc & 0xFF);

        lastSequence++;
        if (lastSequence > RTPHeader.MAX_SEQUENCE) {
            lastSequence = 0;
        }
    }

    /**
     * Method used to transfer data to the RTP stream.
     * @param stream stream used to read data from
     */
    public void transferData(PushSourceStream stream) {
        logger.warning("RTP PUSH SOURCE STREAM !!");
//        try
//        {         
//            Thread.sleep(300);
//        } catch (InterruptedException ex)
//        {
//            ex.printStackTrace();
//        }
        
        if (!stream.endOfStream()) {
            int length = 0;
            int size = stream.getMinimumTransferSize();
            if (buffer.length < size + RTPHeader.SIZE) {
                buffer = new byte[size + RTPHeader.SIZE];
            }
            try {
                length = stream.read(buffer, RTPHeader.SIZE, 
                        buffer.length - RTPHeader.SIZE);
                if (length > 0) {
                    long time = System.currentTimeMillis();
                    if (lastSendTime != -1) {
                        /* Chris: see explanations below */
                        lastTimestamp += 
                            ((time - lastSendTime) * clockRate) / 1000;
                    }
                    lastSendTime = time;
                    // TODO: this timestamp calculation will be wrong for video, where all packets of a single frame should have
                    // the same timestamp.  Also, the RTP marker flag will never be set, and for video, it needs to be set for the
                    // last packet of a frame.
                    // See the other call to writeHeaderToBuffer, in the transferDatat(PushBufferStream).
                    // really, we should probably not accept a PushSourceStream, as it does not give us access to the
                    // timestamps, or the RTP marker flag.
                    // kenlars99 8/21/07
                    writeHeaderToBuffer(false, lastTimestamp);
                    rtpDataStream.write(buffer, 0, length + RTPHeader.SIZE);
                }
            } catch (IOException e) {
            	logger.log(Level.WARNING, "" + e, e);
            }
            
            stats.addPDUTransmitted();
            RTPHeader header=null;
            try
            {
                header = new RTPHeader(buffer, 0, RTPHeader.SIZE);
                //System.out.println("PADDING : "+header.getPadding()+" ["+(int)buffer[buffer.length-1]+"]");
                int len = length - (header.getPadding() > 0 ? (int)buffer[buffer.length-1]:0);
                stats.addBytesTransmitted(len);
            } catch (IOException ex)
            {
            	// TODO: why don't we log this?
                //ex.printStackTrace();
            }
            
            rtpMgr.RTPPacketSent(lastSendTime, length + RTPHeader.SIZE);
        }
    }
    
    private static final int MAX_PUSHBUFFER_DATA_SIZE = 2048;	// TODO: this is just an arbitrary number.  Ideally, we can just 
    															// get the data directly from the stream without using our buffer,
    private int lop = 0;
    
    /**
     * Method used to transfer data to the RTP stream.
     * @param stream stream to read data from
     */
    public void transferData(PushBufferStream stream) {
        
        if (!stream.endOfStream()) {
                
                // According to the API, if the caller sets the
				// data in the buffer, the stream should not allocate it.
				// See http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/protocol/PushBufferStream.html
				// The original implementation here assumed this.  But in JMF there are some badly behaving PushBufferStreams,
                // namely those in RTPSyncBufferMux, which give us a new buffer.  So we'll check for that below, and
                // copy it back to buffer.  kenlars99 7/13/07.
            Buffer recvBuffer = new Buffer();
            try {
                // Make sure that buffer is big enough.  kenlars99 6/3/07.
                if (buffer.length < MAX_PUSHBUFFER_DATA_SIZE + RTPHeader.SIZE) {
                    buffer = new byte[MAX_PUSHBUFFER_DATA_SIZE + RTPHeader.SIZE];
                }
                
                // According to the API, if the caller sets the
				// data in the buffer, the stream should not allocate it.
				// See http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/protocol/PushBufferStream.html
				// The original implementation here assumed this.  But in JMF there are some badly behaving PushBufferStreams,
                // namely those in RTPSyncBufferMux, which give us a new buffer.  So we'll check for that below, and
                // copy it back to buffer.  kenlars99 7/13/07.
                recvBuffer.setData(buffer);
                recvBuffer.setOffset(RTPHeader.SIZE);
                recvBuffer.setLength(buffer.length - RTPHeader.SIZE);
                stream.read(recvBuffer);
                if (recvBuffer.getLength() > 0)
                {
                    
                    // copy the data back into buffer, if the stream put it in a different byte array. kenlars99 7/13/07.
                    if (recvBuffer.getData() != buffer)
                    	System.arraycopy(recvBuffer.getData(), recvBuffer.getOffset(), buffer, RTPHeader.SIZE, recvBuffer.getLength());

                    /* We set the marker flag if we're at the end of a video frame */
                    boolean marker = (recvBuffer.getFlags() & Buffer.FLAG_RTP_MARKER) != 0;
                    //printFlags(recvBuffer);
                    //System.out.println("MARKER : "+recvBuffer.getFlags()+" / "+ recvBuffer.FLAG_RTP_MARKER +" => "+marker);
                    //System.out.println("TIME "+recvBuffer.getTimeStamp());

                    long rtpTimestamp = 0;

                    // mgodehardt: the timestamp value in the rtp packet is dependent on the payload type, for ulaw
                    // its easy 160 bytes in a packet means, add 160 to the timestamp, see below

                    // TODO: add other formats, at the moment only ULAW works
                    if ( (stream.getFormat().getEncoding() == AudioFormat.ULAW_RTP) || (stream.getFormat().getEncoding() == AudioFormat.GSM_RTP) )
                    {
                        rtpTimestamp = lastTimestamp;
                    }
                    else if ( stream.getFormat().getEncoding() == VideoFormat.JPEG_RTP )
                    {
                        rtpTimestamp = lastTimestamp;

                        if ( 0 == lastBufferTimestamp )
                        {
                            lastBufferTimestamp = System.nanoTime() / 1000000L;
                        }
                    }

                    // RFC 3550 Page 13, The timestamp reflects the sampling instant of the first octet in the buffer
                    writeHeaderToBuffer(marker, rtpTimestamp);

                    // copy the data back into buffer, if the stream put it in a different byte array. kenlars99 7/13/07.
                    if (recvBuffer.getData() != buffer)
                    {
                        System.arraycopy(recvBuffer.getData(), recvBuffer.getOffset(), buffer, RTPHeader.SIZE, recvBuffer.getLength());
                    }
                    
                    ///long encodeTime = (System.nanoTime() - recvBuffer.getTimeStamp()) / 1000000L;
                    ///System.out.println("### " + (recvBuffer.getLength() + RTPHeader.SIZE) + " " + marker + " rtpTimestamp=" + rtpTimestamp + " encodeTime=" + encodeTime);
                    rtpDataStream.write(buffer, 0, recvBuffer.getLength() + RTPHeader.SIZE);
                    
                    // TODO: add other formats
                    if ( stream.getFormat().getEncoding() == AudioFormat.ULAW_RTP )
                    {
                        // RFC 3550 Page 13, fixed rate audio should increment timestamp by one for each sampling period
                        // we use 8000Hz for ULAW/PCMU
                        lastTimestamp += recvBuffer.getLength();
                    }
                    else if ( stream.getFormat().getEncoding() == AudioFormat.GSM_RTP )
                    {
                        // RFC 3550 Page 13, fixed rate audio should increment timestamp by one for each sampling period
                        // we use 8000Hz for GSM 6.10
                        lastTimestamp += (recvBuffer.getLength() * 8000) / 1650;
                    }
                    else if ( stream.getFormat().getEncoding() == VideoFormat.JPEG_RTP )
                    {
                        if ( (recvBuffer.getFlags() & Buffer.FLAG_RTP_MARKER) > 0 )
                        {
                            long currentTime = (System.nanoTime() / 1000000L);
                        
                            // we use 90000Hz for JPEG ( see RFC 3551 )
                            long diffTime = currentTime - lastBufferTimestamp;
                            lastTimestamp += (diffTime * 90);
                            
                            lastBufferTimestamp = currentTime;
                        }
                    }

                    // must be done here, lastTimestamp and lastSendTime must have a relationship
                    lastSendTime = System.currentTimeMillis() - RTCPSenderInfo.MSB_1_BASE_TIME;
                    if ( initialSendTime == -1 )
                    {
                        initialSendTime = lastSendTime;
                    }
                }
            } catch (IOException e) {
            	logger.log(Level.WARNING, "" + e, e);
            }
            
            stats.addPDUTransmitted();
            RTPHeader header=null;
            try
            {
                header = new RTPHeader(buffer, 0, RTPHeader.SIZE);
                //System.out.println("PADDING for "+header.getSsrc()+" : "+header.getPadding()+" ["+(int)buffer[buffer.length-1]+"]");
                int len = recvBuffer.getLength() - (header.getPadding() > 0 ? (int)buffer[buffer.length-1]:0);
                //System.out.println("ADDING LEN "+len);
                stats.addBytesTransmitted(len);
            } catch (IOException ex)
            {
                //ex.printStackTrace();
            	// TODO: why don't we log this?
            }
            
            rtpMgr.RTPPacketSent(lastSendTime, recvBuffer.getLength()+RTPHeader.SIZE);
        }
    }
    
    private static void printFlags(Buffer buf) {
        StringBuffer out = new StringBuffer("FLAGS :");
        if ((buf.getFlags() & Buffer.FLAG_BUF_OVERFLOWN) != 0) out.append("OVERFLOWN ");
        if ((buf.getFlags() & Buffer.FLAG_BUF_UNDERFLOWN) != 0) out.append("UNDERFLOWN ");
        if ((buf.getFlags() & Buffer.FLAG_DISCARD) != 0) out.append("DISCARDED ");
        if ((buf.getFlags() & Buffer.FLAG_EOM) != 0) out.append("EOM ");
        if ((buf.getFlags() & Buffer.FLAG_FLUSH) != 0) out.append("FLUSH ");
        if ((buf.getFlags() & Buffer.FLAG_KEY_FRAME) != 0) out.append("KEY_FRAME ");
        if ((buf.getFlags() & Buffer.FLAG_LIVE_DATA) != 0) out.append("LIVE_DATA ");
        if ((buf.getFlags() & Buffer.FLAG_NO_DROP) != 0) out.append("NO_DROP ");
        if ((buf.getFlags() & Buffer.FLAG_NO_SYNC) != 0) out.append("NO_SYNC ");
        if ((buf.getFlags() & Buffer.FLAG_NO_WAIT) != 0) out.append("NO_WAIT ");
        if ((buf.getFlags() & Buffer.FLAG_RELATIVE_TIME) != 0) out.append("RELATIVE_TIME ");
        if ((buf.getFlags() & Buffer.FLAG_RTP_MARKER) != 0) out.append("MARKER ");
        if ((buf.getFlags() & Buffer.FLAG_RTP_TIME) != 0) out.append("RTP_TIME ");
        if ((buf.getFlags() & Buffer.FLAG_SID) != 0) out.append("SID ");
        if ((buf.getFlags() & Buffer.FLAG_SILENCE) != 0) out.append("SILENCE ");
        if ((buf.getFlags() & Buffer.FLAG_SYSTEM_MARKER) != 0) out.append("SYSMARKER ");
        if ((buf.getFlags() & Buffer.FLAG_SYSTEM_TIME) != 0) out.append("SYSTIME ");
        System.out.println(out);
    }
    
    /**
     * Returns the source description for this source
     * @return The source description objects
     */
    public Vector getSourceDescription() {
        return new Vector(sourceDescriptions.values());
    }
    
    /**
     * Returns the number of bytes of sdes that this participant requires
     * @return the number of bytes of sdes that this participant requires
     */
    public int getSdesSize() {
        return sdesSize;
    }
    
    /**
     * Returns the last time a packet was sent.
     * @return the last time a packet was sent
     */
    public long getLastSendTime() {
        return lastSendTime;
    }

    /**
     * Returns the last time a packet was sent.
     * @return the last time a packet was sent
     */
    public long getInitialSendTime() {
        return initialSendTime;
    }
    
    /**
     * Returns the last timestamp of a packet sent.
     * @return the last timestamp of a packet sent
     */
    public long getLastTimestamp() {
        return lastTimestamp;
    }

    /**
     * Returns the initial timestamp base.
     * @return the initial timestamp base
     */
    public long getInitialTimestamp() {
        return initialTimestamp;
    }

    /**
     * Returns the clockRate used for sampling.
     *@return the clockRate used by this stream
     */
    public double getClockRate()
    {
        return clockRate;
    }

}
