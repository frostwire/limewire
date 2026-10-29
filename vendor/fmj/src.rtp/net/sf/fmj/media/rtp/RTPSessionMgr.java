/*
 * @(#)RTPManager.java
 * Created: 25-Oct-2005
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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Dimension;

import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.format.UnsupportedFormatException;
import javax.media.format.VideoFormat;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.rtp.EncryptionInfo;
import javax.media.rtp.GlobalReceptionStats;
import javax.media.rtp.GlobalTransmissionStats;
import javax.media.rtp.LocalParticipant;
import javax.media.rtp.OutputDataStream;
import javax.media.rtp.Participant;
import javax.media.rtp.RTPConnector;
import javax.media.rtp.RTPStream;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.RemoteListener;
import javax.media.rtp.SendStream;
import javax.media.rtp.SendStreamListener;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.SessionListener;
import javax.media.rtp.SessionManager;
import javax.media.rtp.TransmissionStats;
import javax.media.rtp.event.ActiveReceiveStreamEvent;
import javax.media.rtp.event.ByeEvent;
import javax.media.rtp.event.InactiveReceiveStreamEvent;
import javax.media.rtp.event.NewParticipantEvent;
import javax.media.rtp.event.NewReceiveStreamEvent;
import javax.media.rtp.event.NewSendStreamEvent;
import javax.media.rtp.event.ReceiveStreamEvent;
import javax.media.rtp.event.ReceiverReportEvent;
import javax.media.rtp.event.RemoteEvent;
import javax.media.rtp.event.SendStreamEvent;
import javax.media.rtp.event.SenderReportEvent;
import javax.media.rtp.event.SessionEvent;
import javax.media.rtp.event.StreamMappedEvent;
import javax.media.rtp.rtcp.SourceDescription;

import net.sf.fmj.media.datasink.rtp.RTPBonusFormatsMgr;
import net.sf.fmj.utility.FmjStartup;
import net.sf.fmj.utility.LoggerSingleton;

/**
 * Kernel of the RTP stack. This is where almost everything is done.
 * Instances of this class handle a single RTP session which is 
 * an association among a set of participants communicating with RTP.  
 * A participant may be involved in multiple RTP sessions at the same time.  
 * In a multimedia session, each medium is typically carried in a separate 
 * RTP session with its own RTCP packets (unless the the encoding itself 
 * multiplexes multiple media into a single data stream).
 * @author Andrew G D Rowley
 * @author Christian Vincenot
 * @version 1-1-alpha3
 */
public class RTPSessionMgr extends javax.media.rtp.RTPManager implements
        SessionManager {
      
    private static final Logger logger = LoggerSingleton.logger;

    // The minimum RTCP interval in ms
    private static final int MIN_RTCP_INTERVAL = 5000;
    
    // The period between consecutive participants timeout checks
    // (should happen at least once per RTCP period)
    private static final long REAPER_PERIOD = MIN_RTCP_INTERVAL - 1;
    
    // Average size of the lower layers (network & transport)
    // WARNING/TODO: a better solution should be found. Hardcoding this
    // sucks and will give poor results with IPv6
    private static final int LOWLAYERS = 20 + 8;
    
    // The map of Integer -> format recognised
    private HashMap formatMap = new HashMap();

    // A vector of receive stream listeners
    private Vector receiveStreamListeners = new Vector();

    // A vector of remote listeners
    private Vector remoteListeners = new Vector();

    // A vector of send stream listeners
    private Vector sendStreamListeners = new Vector();

    // A vector of session listeners
    private Vector sessionListeners = new Vector();

    // The local participant
    private RTPLocalParticipant localParticipant = null;

    // A map of active participants (cname -> participant)
    private HashMap activeParticipants = new HashMap();

    // A map of inactive participants (cname -> participant)
    private HashMap inactiveParticipants = new HashMap();

    // The global reception statistics
    private RTPGlobalReceptionStats globalReceptionStats =
        new RTPGlobalReceptionStats();

    // The global transmission statistics
    private RTPGlobalTransmissionStats globalTransmissionStats =
        new RTPGlobalTransmissionStats();

    // A map of receive streams (ssrc -> stream)
    private HashMap receiveStreams = new HashMap();

    // A map of send streams (ssrc -> stream)
    private HashMap sendStreams = new HashMap();

    // A map of streams that are ignored as they cannot be recognised
    private HashMap ignoredStreams = new HashMap();

    // Streams which have been mapped until now
    private HashMap unassignedStreams = new HashMap();
    
    // A map of ssrcs to cnames
    private HashMap senders = new HashMap();

    // The RTCP Receiver Bandwidth fraction
    private double rtcpReceiverBandwidthFraction = 0.0375;

    // The RTCP Sender Bandwidth fraction
    private double rtcpSenderBandwidthFraction = 0.0125;

    // The local address to use to bind sockets to
    private SessionAddress localAddress = null;

    // The RTP Connectors for targets that have been set up
    // (address -> connector)
    private final HashMap<SessionAddress, RTPConnector> targets = new HashMap<SessionAddress, RTPConnector>();

    // The RTP Handler
    private RTPHandler rtpHandler = null;

    // The RTCP Handler
    private RTCPHandler rtcpHandler = null;

    // The lock for sending events (so events are received in order)
    private Integer eventLock = new Integer(0);

    // True if the event lock has been obtained
    private boolean eventLocked = false;

    // True when the session is finished with
    private boolean done = false;

    // An RTCP timer
    private Timer rtcpTimer = new Timer("RTCP Timer", true);
    
    // The Timer Task meant to send the RTCP packets (NOTE: access to it might 
    // have to be synchronized using a non-blocking mutex system)
    private RTCPTimerTask RTCPSendTask = null;
   
    // The Grim Reaper Timer : checks the dying participants and kills those who
    // have timed out
    private Timer reaperTimer = new Timer("Reaper Timer", true);
    
    // The time at which the last rtcpPacket was sent
    private long lastRTCPSendTime = -1;
            
    // Time at which the next RTCP packet must be sent
    private long nextRTCPSendTime = -1;

    // The average size of an RTCP packet received
    // Initialized to the size of an RTCP RR packet
    private int averageRTCPSize = 60;
    
    // How many rtcp packets were sent
    private long rtcpPacketsSent = 0;
    
    // Flag set if we're about to leave the session (a BYE packet is waiting to be sent)
    private boolean sayonara = false;
    
    // Position in the round-robin (used when having to send RTCP RR packets with
    // too many report blocks to fit in a single RTCP packet)
    private int RRRoundRobin = 0;
    
    // The we_sent variable described in the RFC. Its value is true if we've recently
    // sent an RTP packet
    private boolean we_sent = false;
    
    // The number of members when the last RTCP delay was calculated
    private long pmembers=1;

    // The ssrc if we are not sending
    protected long ssrc=0;
    
    // Our SSRC generator
    private SSRCGenerator generator;

    // the socket adapter (RTPConnector) used to send and receive RTP packets
    RTPConnector socket = null;
    
    private void getEventLock() {
        synchronized (eventLock) {
            while (eventLocked) {
                try {
                    eventLock.wait();
                } catch (InterruptedException e) {
                    // Do Nothing
                }
            }
            eventLocked = true;
        }
    }

    private void releaseEventLock() {
        synchronized (eventLock) {
            eventLocked = false;
            eventLock.notifyAll();
        }
    }

    /**
     * Creates a new RTPManager
     */
    public RTPSessionMgr() {
        //new Error().printStackTrace();
        String user = "username";
        if ( !FmjStartup.isApplet )
        {
            user = System.getProperty("user.name");
        }
        
        String host = "localhost";
        if ( !FmjStartup.isApplet )
        {
            try
            {
                host = InetAddress.getLocalHost().getHostName();
            }
            catch (UnknownHostException e)
            {
                // Do Nothing
            }
        }
        
        this.localParticipant = new RTPLocalParticipant(user + "@FMJ." + host);
        this.generator = new SSRCGenerator();
        //this.ssrc = generateSSRC();
        
        addFormat(new AudioFormat(AudioFormat.ULAW_RTP, 8000, 8, 1), 0);
        addFormat(new AudioFormat(AudioFormat.GSM_RTP, 8000,
                Format.NOT_SPECIFIED, 1), 3);
        addFormat(new AudioFormat(AudioFormat.G723_RTP, 8000,
                Format.NOT_SPECIFIED, 1), 4);
        addFormat(new AudioFormat(AudioFormat.DVI_RTP, 8000, 4, 1), 5);
        addFormat(new AudioFormat(AudioFormat.MPEG_RTP), 14);
        addFormat(new AudioFormat(AudioFormat.G728_RTP, 8000.0, Format.NOT_SPECIFIED, 1), 15);
        addFormat(new AudioFormat(AudioFormat.DVI_RTP, 11025, 4, 1), 16);
        addFormat(new AudioFormat(AudioFormat.DVI_RTP, 22050, 4, 1), 17);
		addFormat(new AudioFormat(AudioFormat.G729_RTP, 8000.0, Format.NOT_SPECIFIED, 1), 18);
        addFormat(new VideoFormat(VideoFormat.JPEG_RTP), 26);
        addFormat(new VideoFormat(VideoFormat.H261_RTP), 31);
        addFormat(new VideoFormat(VideoFormat.MPEG_RTP), 32);
        addFormat(new VideoFormat(VideoFormat.H263_RTP), 34);

        addFormat(new VideoFormat(VideoFormat.H263_1998_RTP), 42);
    }
    

    
    /**
     * This is a function provided by Sun's implementation, which is not in the API as AFAIK - kenlars99.
     * This does not include formats in the RTP bonus formats mgr.
     * This is a somewhat dubious function, since the actual RTPSessionMgr used is determined by RTPManager.newInstance().
     * To the extend that Sun's code calls this function, it is assuming that its own RTPSessionMgr will be used, which
     * may be the source of some problems.
     * @param f the format which we want to test
     * @return true if the format is supported
     * 
     * Note: the format list in JMF is static, and in FMJ it is not.  So you can add a format once with
     * JMF, but you have to add it every time you instantiate the manager with FMJ.
     * 
     */
	public static boolean formatSupported(Format f) {
		final RTPSessionMgr mgr = new RTPSessionMgr();
		
		try
		{
			RTPBonusFormatsMgr.addBonusFormats(mgr);
			
	        final Iterator iter = mgr.formatMap.keySet().iterator();
	        while (iter.hasNext()) {
	            Integer id = (Integer) iter.next();
	            Format testFormat = (Format) mgr.formatMap.get(id);
	            if (testFormat.matches(f)) {
	               return true;
	            }
	        }
	        
			return false;
		}
		finally
		{	mgr.dispose();
		}
	}
	
	/** Another function provided by Sun's implementation, not in the spec/API. */
	public Format getFormat(int payload) {
		return (Format) formatMap.get(new Integer(payload));
	}


    /**
     * Add a format to the list of supported formats.
     * @param format format to add
     * @param payload type of payload corresponding to this format
     */
    @Override
	public void addFormat(Format format, int payload) {
        formatMap.put(new Integer(payload), format);
    }

    /**
     * Adds a listener to the list of listeners wanting to be informed when a receive stream is created.
     * @param listener the listener to add
     */
    @Override
	public void addReceiveStreamListener(ReceiveStreamListener listener) {
        receiveStreamListeners.add(listener);
    }

    /**
     * Adds a listener to the list of remote listeners.
     * @param listener listener to add
     */
    @Override
	public void addRemoteListener(RemoteListener listener) {
        remoteListeners.add(listener);
    }

    /**
     * Add this listener to the list of listeners wanting to be informed when a send stream is created.
     * @param listener the listener to add
     */
    @Override
	public void addSendStreamListener(SendStreamListener listener) {
        sendStreamListeners.add(listener);
    }

    /**
     * Add this listener to the list of session listeners.
     * @param listener listener to add
     */
    @Override
	public void addSessionListener(SessionListener listener) {
        sessionListeners.add(listener);
    }

    /**
     * Removes this listener from the list of listeners wanting to be be informed
     * of the creation of new receive streams.
     * @param listener listener to remove
     */
    @Override
	public void removeReceiveStreamListener(ReceiveStreamListener listener) {
        receiveStreamListeners.remove(listener);
    }

    /**
     * Removes this listener from the list of remote listeners.
     * @param listener listener to remove
     */
    @Override
	public void removeRemoteListener(RemoteListener listener) {
        remoteListeners.remove(listener);
    }

    /**
     * Removes this listener from the list of listeners wanting to be informed of
     * the creation of send streams.
     * @param listener listener to remove
     */
    @Override
	public void removeSendStreamListener(SendStreamListener listener) {
        sendStreamListeners.remove(listener);
    }

    /**
     * Removes this listener from the list of session listeners.
     * @param listener listener to remove
     */
    @Override
	public void removeSessionListener(SessionListener listener) {
        sessionListeners.remove(listener);
    }

    /**
     * Returns the list of active participants in this RTP session.
     * @return the list of active participants in this RTP session
     */
    @Override
	public Vector getActiveParticipants() {
        Vector participants = new Vector(activeParticipants.values());
        if (localParticipant.isActive()) {
            participants.add(localParticipant);
        }
        return participants;
    }

    /**
     * Returns the list of all participants in this RTP session.
     * @return the list of all participants in this RTP session
     */
    @Override
	public Vector getAllParticipants() {
        Vector participants = new Vector();
        participants.addAll(activeParticipants.values());
        participants.addAll(inactiveParticipants.values());
        participants.add(localParticipant);
        return participants;
    }

    /**
     * Returns the local participant.
     * @return the local participant
     */
    @Override
	public LocalParticipant getLocalParticipant() {
        return localParticipant;
    }

    /**
     * Returns the list of passive participants in this RTP session.
     * @return the list of passive participants in this RTP session
     */
    @Override
	public Vector getPassiveParticipants() {
        Vector participants = new Vector(inactiveParticipants.values());
        if (!localParticipant.isActive()) {
            participants.add(localParticipant);
        }
        return participants;
    }

    /**
     * Returns the list of remote participants in this RTP session.
     * @return the list of passive participants in this RTP session
     */
    @Override
	public Vector getRemoteParticipants() {
        Vector participants = new Vector();
        participants.addAll(activeParticipants.values());
        participants.addAll(inactiveParticipants.values());
        return participants;
    }

    /**
     * Returns the global reception statistics for this RTP session.
     * @return the global reception stats
     */
    @Override
	public GlobalReceptionStats getGlobalReceptionStats() {
        return globalReceptionStats;
    }

    /**
     * Returns the global transmission statistics for this RTP session.
     * @return the global transmission statistics for this RTP session
     */
    @Override
	public GlobalTransmissionStats getGlobalTransmissionStats() {
        return globalTransmissionStats;
    }

    /**
     * Returns the list of receive streams in this RTP session.
     * This is the list of streams used to receive data, 
     * ie connected to senders.
     * @return the list of receive streams in this RTP session
     */
    @Override
	public Vector getReceiveStreams() {
        return new Vector(receiveStreams.values());
    }

    /**
     * Returns the list of send streams in this RTP session.
     * This is the list of streams used to send data, 
     * ie connected to receivers.
     * @return the list of send streams in this RTP session
     */
    @Override
	public Vector getSendStreams() {
        return new Vector(sendStreams.values());
    }

    /**
     * Initializes and starts the RTP Session.
     * @param localAddress the local address
     * @throws java.io.IOException I/O Exception
     */
    @Override
	public void initialize(SessionAddress localAddress)
            throws IOException {
        //System.out.println("FMJ : INITIALISATION RTP !!");
        String user = "username";
        if ( !FmjStartup.isApplet )
        {
            user = System.getProperty("user.name");
        }
        initialize(new SessionAddress[] { localAddress },
                new SourceDescription[] {
                        new SourceDescription(
                                SourceDescription.SOURCE_DESC_CNAME, user
                                        + "@"
                                        + InetAddress.getLocalHost()
                                                .getHostName(), 1, false),
                        new SourceDescription(
                                SourceDescription.SOURCE_DESC_NAME, user
                                        + "@"
                                        + InetAddress.getLocalHost()
                                                .getHostName(), 3, false) },
                0.05, 0.25, null);
    }

    /**
     * Initializes and starts the RTP Session.
     * @param localAddresses the local address
     * @param sourceDescription the source description to be used when sending SDES reports
     * @param rtcpBandwidthFraction the bandwidth fraction allocated for RTCP traffic
     * @param rtcpSenderBandwidthFraction the RTCP bandwidth fraction allocated for senders
     * @param encryptionInfo Encryption information - UNUSED at the moment
     */
    @Override
	public void initialize(SessionAddress[] localAddresses,
            SourceDescription[] sourceDescription,
            double rtcpBandwidthFraction, double rtcpSenderBandwidthFraction,
            EncryptionInfo encryptionInfo) {
        
        //System.out.println("FMJ : INITIALISATION RTP !!");
        
        /* Note: the two parameters supplied here are the global bandwidth % assigned
         * to the RTCP traffic and the % of the RTCP traffic allocated for senders.
         * This is not exactly what is suggested by the RFC (S : bw % for senders, R : bw % for receivers),
         * but it's ok (if those parameters are the one really supplied to the RTP stack).
         * BUG correction: original code was: sender_bw = bw * bw , which is obviously wrong.
         */
        this.rtcpSenderBandwidthFraction = rtcpSenderBandwidthFraction
                * rtcpBandwidthFraction;
        this.rtcpReceiverBandwidthFraction = rtcpBandwidthFraction
                - this.rtcpSenderBandwidthFraction;
        
        /* BUG/STRANGE: Erases the localParticipant created in the constructor ???? */ 
        //this.localParticipant = new RTPLocalParticipant("");
        
        localParticipant.setSourceDescription(sourceDescription);
        localAddress = localAddresses[0];
        
        this.ssrc = generateSSRC();
        inactiveParticipants.put(localParticipant.getCNAME(), localParticipant);
        
        start();
    }

    /**
     * Add a network target to which we'll send our RTP packets.
     * This network target can be one receiver (unicast address)
     * or several receivers (multicast address).
     * @param remoteAddress remote address (unicast or multicast) to which we want to send our RTP packets
     * @throws java.io.IOException I/O Exception
     */
    @Override
	public void addTarget(SessionAddress remoteAddress)
            throws IOException {
                //System.out.println("FMJ : Add Target !!");
        if (socket == null) {
            socket = new RTPSocketAdapter(localAddress.getDataAddress(),
                    localAddress.getDataPort(),
                    localAddress.getTimeToLive());
            rtpHandler = new RTPHandler(this);
            rtcpHandler = new RTCPHandler(this);
            socket.getControlInputStream().setTransferHandler(rtcpHandler);
            socket.getDataInputStream().setTransferHandler(rtpHandler);
        }
        
        if ( socket instanceof RTPSocketAdapter )
        {
            ((RTPSocketAdapter)socket).addTarget(remoteAddress);
        }
        targets.put(remoteAddress, socket);
    }

    /**
     * Initializes and starts the RTP Session.
     * @param connector the RTP connector used to retrieve the control & data streams
     */
    @Override
	public void initialize(RTPConnector connector) {
                //System.out.println("FMJ : INITIALISATION RTP !!");
        try {
            ssrc = generateSSRC();
            socket = connector;
            rtpHandler = new RTPHandler(this);
            rtcpHandler = new RTCPHandler(this);
            connector.getControlInputStream().setTransferHandler(rtcpHandler);
            connector.getDataInputStream().setTransferHandler(rtpHandler);
            targets.put((SessionAddress) null, connector);
            start();
        } catch (IOException e) {
            logger.log(Level.WARNING, "" + e, e);
        }
    }

    /**
     * Remove a network target .
     * @param remoteAddress remote address (unicast or multicast) to which we want to send our RTP packets
     * @param reason reason for which we remove this target (used in the RTCP BYE packet following this call)
     */
    @Override
    public void removeTarget(SessionAddress remoteAddress, String reason)
    {
        Vector sendStreams = localParticipant.getStreams();
        if ( sendStreams != null )
        {
            // TODO: check for multicast and broadcast
            RTPSendStream sendStream = null;
            if ( sendStreams.size() > 0 )
            {
            	sendStream = (RTPSendStream)sendStreams.get(0);
            }
            sendBYE(sendStream, remoteAddress, reason);
        }

        if ( socket instanceof RTPSocketAdapter )
        {
            ((RTPSocketAdapter)socket).removeTarget(remoteAddress);
        }
        targets.remove(remoteAddress);
    }

    /**
     * Remove a network target .
     * @param reason reason for which we remove this target (used in the RTCP BYE packet following this call)
     */
    @Override
	public void removeTargets(String reason) {
        final Iterator<SessionAddress> iter = targets.keySet().iterator();
        while (iter.hasNext()) {
            final SessionAddress addr = iter.next();
            removeTarget(addr, reason);
        }
        if (socket != null)
        {
	        socket.close();     // removed all target, can close own socket also
	        socket = null;
        }
    }

    private void sendBYE(RTPSendStream sendStream, SessionAddress remoteAddress, String reason)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(bytes);

        try
        {
            // RFC 3550 Page 33, MUST NOT send a BYE when we never send a RTP or RTCP packet
            boolean fSendBYE = (rtcpPacketsSent > 0) ? true : false;
            if ( (null != sendStream) && (sendStream.getSourceTransmissionStats().getBytesTransmitted() > 0) )
            {
                fSendBYE = true;
            }

            if ( fSendBYE )
            {
                // Determine the packet type
                int packetType = RTCPPacket.PT_RR;
                int packetSize = 1;

                if ( we_sent )
                {
                    packetType = RTCPPacket.PT_SR;
                    packetSize = 6;
                }

                // Send a empty RR/SR packet ( must be always the first rtcp packet in a compound packet )
                output.writeByte(0x80);
                output.writeByte(packetType & 0xFF);
                output.writeShort(packetSize);

                if ( we_sent )
                {
                    writeSenderInfo(output, sendStream);
                }
                else
                {
                    output.writeInt((int)(ssrc & 0xFFFFFFFF));
                }

                writeSDESPacket(output);

                // calculate goodbye packet length
                int byelen = 4;
                if ( (null != reason) && (reason.length() > 0) )
                {
                    byte[] reasonBytes = reason.getBytes("UTF-8");
                    byelen += reasonBytes.length + 1;
                }

                // do we need padding to next 32-bit boundary
                int padding = 0;
                if ( (byelen % 4) != 0 )
                {
                    padding = 4 - (byelen % 4);
                    byelen += padding;
                }

                // Send a goodbye packet ( must be the last packet in a compound packet )
                output.writeByte(0x81);
                output.writeByte(RTCPPacket.PT_BYE & 0xFF);
                output.writeShort(byelen / 4);
                output.writeInt((int)(ssrc & 0xFFFFFFFF));

                // Goodbye RTCP Packet, send the reason for leaving
                if ( (null != reason) && (reason.length() > 0) )
                {
                    byte[] reasonBytes = reason.getBytes("UTF-8");
                    output.writeByte(reasonBytes.length & 0xFF);
                    output.write(reasonBytes);

                    for (int i=0; i<padding; i++)
                    {
                        output.writeByte(0);
                    }
                }

                output.close();
                bytes.close();

                byte[] data = bytes.toByteArray();
                if ( socket.getControlOutputStream() instanceof SocketOutputStream )
                {
                    SocketOutputStream out = (SocketOutputStream) socket.getControlOutputStream();
                    out.writeToRemote(data, 0, data.length, remoteAddress.getControlAddress(), remoteAddress.getControlPort());
                }
                else
                {
                    OutputDataStream outstream = socket.getControlOutputStream();
                    outstream.write(data, 0, data.length);
                }
            }
        }
        catch (IOException ex)
        {
            logger.log(Level.WARNING, "" + ex, ex);
        }
    }
    
    /**
     * Creates a send stream used to carry data from a source to some receivers over the network.
     * @param dataSource the datasource producing the data to send
     * @param streamIndex the index of the stream in the datasource's streams table
     * @return the created SendStream
     * @throws javax.media.format.UnsupportedFormatException thrown if the datasource's stream only outputs formats which aren't supported
     * @throws java.io.IOException I/O Exception thrown in case of problem with streams
     */
    @Override
	public SendStream createSendStream(DataSource dataSource, int streamIndex)
            throws UnsupportedFormatException, IOException {
        int format = -1;
        Format fmt = null;
        double clockRate = 90000;
        if (dataSource instanceof PushBufferDataSource) {
            PushBufferStream stream =
                ((PushBufferDataSource) dataSource).getStreams()[streamIndex];
            fmt = stream.getFormat();
        } else if (dataSource instanceof PullBufferDataSource) {
            PullBufferStream stream =
                ((PullBufferDataSource) dataSource).getStreams()[streamIndex];
            fmt = stream.getFormat();
        } else {
            throw new IOException("Cannot use stream sources");
        }
        //System.out.println("createSendStream : Trying to find handler for format "+ fmt);
        Iterator iter = formatMap.keySet().iterator();
        while (iter.hasNext()) {
            Integer id = (Integer) iter.next();
            Format testFormat = (Format) formatMap.get(id);
            //System.out.println("Comparing with format "+testFormat);
            if (testFormat.matches(fmt)) {
                format = id.intValue();
                //System.out.println("GOT IT!");
            }
        }
        if (format == -1) {
            throw new UnsupportedFormatException(fmt);
        }
        if (fmt instanceof AudioFormat) {
            clockRate = ((AudioFormat) fmt).getSampleRate();
        }
        OutputDataStream stream = socket.getDataOutputStream();

        // see RFC 3550 Page 8, a RTP Session has ONLY one medium per session, multiplexing is done thru different ports
        RTPSendStream RTPStream = new RTPSendStream(ssrc,
                dataSource, stream, streamIndex, localParticipant, format,
                clockRate, this);
        sendStreams.put(new Long(ssrc), RTPStream);
        
        /* Chris: Also add it to the local participant's streams?*/
        localParticipant.addStream(RTPStream);
        
        getEventLock();
        SendStreamEvent ev = new NewSendStreamEvent(this, RTPStream);
        new SendStreamNotifier(sendStreamListeners,ev);
        
        return RTPStream;
    }

    /**
     * Called when wishing to quit the RTP session.
     */
    @Override
	public void dispose() {
        //removeTargets("Quitting");
        closeSession("Quitting");
        rtcpTimer.cancel();
        reaperTimer.cancel();
        done = true;
    }

    /**
     * Get control for the specified type. DUMMY.
     * @param controlClass the control class
     * @return null
     */
    public Object getControl(String controlClass) {
        return null;
    }

    /**
     * Get the list of all controls possible. DUMMY.
     * @return an EMPTY ARRAY
     */
    public Object[] getControls() {
        return new Object[] {};
    }

    /**
     * Handles an incoming RTP packet
     * 
     * @param data The raw packet data
     * @param offset The packet offset after which we must start investigating
     * @param length The total packet length
     */
    protected void handleRTPPacket(byte[] data, int offset, int length) {
        //System.out.println("FMJ : RTP PACKET!!");
        
         /* If we're about to leave the session, return to avoid messing up with the
         * BYE sending procedure. */
        if (sayonara) return;
        
        try
        {
            // mgodehardt: we should add some RTP header sanity checks here or anywhere else ?
            
            /* Statistics & RTP size calculation */
            globalReceptionStats.addPacketRecd();
            globalReceptionStats.addBytesRecd(length);
            computeAverageRTPSize(length);
            
            RTPHeader header = new RTPHeader(data, offset, length);
            long ssrc = header.getSsrc();
            
            /* This should be considered as loops, but for the moment we just exclude the
            * packets using the same SSRC as ours (because they most probably come from us).
            * THIS MUST BE MODIFIED TO CONFORM TO THE RFC-3550.
            */
            Object test = sendStreams.get(new Long(ssrc));
            if (test != null) {
                logger.fine("Collision/Loop ??");
                return;
            }
            
            Integer packetType = (Integer) ignoredStreams.get(new Long(ssrc));
                      
            if (packetType != null) {
                if (packetType.intValue() != header.getPacketType()) {
                    ignoredStreams.remove(new Long(ssrc));
                    packetType = null;
                }
            }
            if (packetType == null) {
                RTPReceiveStream stream =
                    (RTPReceiveStream) receiveStreams.get(new Long(ssrc));
                if (stream == null) {
                    int type = header.getPacketType();
                    Format format = (Format) formatMap.get(new Integer(type));
                    if (format == null) {
                        globalReceptionStats.addUnknownType();
                        logger.warning("Unknown format identifier: "
                                + type);
                        ignoredStreams.put(new Long(ssrc), new Integer(type));
                    }
                    else
                    {
                        if ( format instanceof VideoFormat )
                        {
                            format = findVideoSize(data, offset + header.getSize(), (VideoFormat)format);
                        }
                        RTPDataSource dataSource = new RTPDataSource(ssrc, format);
                        stream = new RTPReceiveStream(dataSource, ssrc);
                        receiveStreams.put(new Long(ssrc), stream);
                        unassignedStreams.put(new Long(ssrc), stream);
                        ReceiveStreamEvent event = new NewReceiveStreamEvent(this, stream);
                        new ReceiveStreamNotifier(receiveStreamListeners, event);
                    }
                }
                if (stream != null) {
                    if (stream.checkResumeActivity()) {
                      try {
                          String cname = (String)senders.get(new Long(ssrc));
                        RTPRemoteParticipant pal =
                               (RTPRemoteParticipant) activeParticipants.get(cname);
                         /* Trigger InactiveReceiveStreamEvent */
                         getEventLock();
                         new ReceiveStreamNotifier(receiveStreamListeners,
                               new ActiveReceiveStreamEvent(this, pal, stream));
                      } catch (Exception e) {
                          e.printStackTrace();
                      }
                    } 
                    RTPDataSource dataSource =
                        (RTPDataSource) stream.getDataSource();
                    dataSource.handleRTPPacket(header,
                            data, offset + header.getSize(),
                            length - header.getSize());
                }
                
            }
        } catch (IOException e) {
            globalReceptionStats.addBadRTPkt();
        }
    }

    private VideoFormat findVideoSize(byte[] data, int offset, VideoFormat format)
    {
        if ( format.getEncoding().equalsIgnoreCase(VideoFormat.JPEG_RTP) )
        {
            int width = data[offset + 6] << 3;
            int height = data[offset + 7] << 3;
            
            return new VideoFormat(VideoFormat.JPEG_RTP, new Dimension(width, height), -1, format.getDataType(), -1);
        }
        return format;
    }
    
    /**
     * Handles an incoming RTCP packet
     * @param data The packet data
     * @param offset The packet offset
     * @param length The packet length
     */
    protected void handleRTCPPacket(byte[] data, int offset, int length) {
                        //System.out.println("FMJ : RTCP PACKET!!");
        
        //TODO : special case of a participant sending only an ignored stream
        // (will be counted as receiver for the moment but shouldn't)
        try {
            globalReceptionStats.addRTCPRecd();
            globalReceptionStats.addBytesRecd(length);
          
            RTCPHeader header = new RTCPHeader(data, offset, length);

            // Get the stream of the participant, if available
            long ssrc = header.getSsrc();
            RTPReceiveStream stream =
                (RTPReceiveStream) receiveStreams.get(new Long(ssrc));

            /* To be tried:
            if (stream == null)
                stream = (RTPReceiveStream) ignoredStreams.get(new Long(ssrc));
            */
            RTCPReport report = null;
            RemoteEvent remoteEvent = null;

            // If the packet is SR, read the sender info
            if (header.getPacketType() == RTCPPacket.PT_SR) {
                report = new RTCPSenderReport(data, offset, length);
                ((RTCPSenderReport) report).setStream(stream);
                remoteEvent = new SenderReportEvent(this,
                        (RTCPSenderReport) report);
                globalReceptionStats.addSRRecd();
            }

            // If the packet is RR, read the receiver info
            if (header.getPacketType() == RTCPPacket.PT_RR) {
                report = new RTCPReceiverReport(data, offset, length);
                remoteEvent = new ReceiverReportEvent(this,
                        (RTCPReceiverReport) report);
            }
            

            // If the report is not null
            if (report != null) {
                
                /* If we're quitting the RTP session and this packet is a BYE,
                 * see RFC-3550 6.3.7 */
                if (sayonara) {
                    if (report.isByePacket()) {
                        /* Dummy entries */
                        inactiveParticipants.put(new Long(System.currentTimeMillis()), "BYE");
                        computeAverageRTPSize(length);
                    }
                    return;
                }
            
                /* Calculate average RTP packet size if not BYE. */
                if (!report.isByePacket())
                    computeAverageRTPSize(length);
                
                String cname = report.getCName();
                if (cname == null) {
                    cname = (String) senders.get(new Long(ssrc));
                }

                if (stream != null) {
                    stream.setReport(report);
                }

                // If the cname is in the report
                if (cname != null) {

                    // Store the cname for later
                    unassignedStreams.remove(new Long(ssrc));
                    senders.put(new Long(ssrc), cname);

                    // Get the participant
                    RTPRemoteParticipant participant =
                        (RTPRemoteParticipant) activeParticipants.get(cname);
                    if (participant == null) {
                        participant = (RTPRemoteParticipant)
                                          inactiveParticipants.get(cname);
                    }

                    // If there is no participant, create one
                    if (participant == null) {
                        participant = new RTPRemoteParticipant(cname);
                        getEventLock();
                        SessionEvent event =
                            new NewParticipantEvent(this, participant);
                        new SessionNotifier(sessionListeners, event);
                        inactiveParticipants.put(cname, participant);
                    }

                    // Set the participant of the report
                    report.setParticipant(participant);
                    participant.addReport(report);

                    // If this is a bye packet, program the removal of the stream
                    if (report.isByePacket()) {
                        new RemoveStreamTimer(this, participant, stream, cname,
                                              report);
                        
                    } else {

                        // If the stream is not null, map the stream
                        if (stream != null) {
                            if (!activeParticipants.containsKey(cname)) {
                                inactiveParticipants.remove(cname);
                                activeParticipants.put(cname, participant);
                            }

                            if (stream.getParticipant() == null) {
                                participant.addStream(stream);
                                stream.setParticipant(participant);
                                getEventLock();
                                ReceiveStreamEvent event =
                                    new StreamMappedEvent(this, stream,
                                            participant);
                                new ReceiveStreamNotifier(
                                        receiveStreamListeners, event);
                            }
                        }
                    }
                }

                // Notify listeners of this packet
                getEventLock();
                new RemoteNotifier(remoteListeners, remoteEvent);
            } else {
                throw new IOException("Unknown report type: " +
                        header.getPacketType());
            }

        } catch (IOException e) {
            globalReceptionStats.addBadRTCPPkt();
        }
    }

    
    /* Conforming to RFC-3550 6.2.1 page 27 :
     * When a BYE packet is received, the stream shouldn't be deleted directly,
     * but after a delay (to avoid that remaining data packets recreate the 
     * stream). We chose this delay as 3 times the computed deterministic 
     * RTCP transmission interval for a sender. 
     */
    private class RemoveStreamTimer extends Thread {
        /* Number of delays to sleep before erasing the stream/participant */
        private final long DELAY_MULTIPLIER = 3;
        RTPRemoteParticipant participant;
        RTPReceiveStream stream;
        String cname;
        RTCPReport report;
        javax.media.rtp.RTPManager rtpmgr;
        
        public RemoveStreamTimer(javax.media.rtp.RTPManager rtpmgr,
                                 RTPRemoteParticipant participant, 
                                 RTPReceiveStream stream, 
                                 String cname,
                                 RTCPReport report) {
            this.participant = participant;
            this.stream = stream;
            this.cname = cname;
            this.report = report;
            this.rtpmgr = rtpmgr;
            //System.out.println("Programming the removal of "+participant);
            setName("RemoveStreamTimer for " + RTPSessionMgr.this);
            start(); 
        }
        
        @Override
		public void run() {
            long delay = calculateRTCPDelay(true, true);
            try
            {
                Thread.sleep(DELAY_MULTIPLIER*delay);
            } catch (InterruptedException ex)
            {
                return; // if we've been interrupted, then just exit.
            }
            
            if (stream != null) {
                Long thisSSRC =  new Long(stream.getSSRC());
                senders.remove(thisSSRC);
                participant.removeStream(stream);
                receiveStreams.remove(thisSSRC);
            }
            
            getEventLock();
            new ReceiveStreamNotifier(receiveStreamListeners,
                          new ByeEvent((SessionManager)rtpmgr, (Participant)participant, (ReceiveStream)stream,
                                        report.getByeReason(),
                                        participant.getStreams().size() == 0));
            
            if (participant.getStreams().size() == 0) {
                            activeParticipants.remove(cname);
                            /* TODO: Was originally following in the code but should it be? 
                             * Since we can't be sure if the partipant will still be a
                             * receiver or not, and considering that the RFC says that the
                             * number of participants should better be overestimated than
                             * underestimated, we add the participant to the receivers, and
                             * if it's really inactive, it will time out anyway? 
                             */
                            //inactiveParticipants.put(cname, participant);
                            
                            /* If a participant has left, we must apply the reverse reconsideration
                             * algorithm.
                             */
                            doReverseReconsideration();
            }
            
            //System.out.println(participant+" REMOVED");
        }
    }
    
     // A notifier of send stream events
    private class SendStreamNotifier extends Thread {


        // The send stream listener
        private Vector listeners = null;

        // The event
        private SendStreamEvent event = null;

        private SendStreamNotifier(Vector listeners,
                SendStreamEvent event) {
            this.listeners = listeners;
            this.event = event;
            start();
        }

        @Override
		public void run() {
            //System.out.println("FMJ : Dispatching SendStreamEvent!!");
            for (int i = 0; i < listeners.size(); i++) {
                SendStreamListener listener =
                    (SendStreamListener) listeners.get(i);
                listener.update(event);
            }
            releaseEventLock();
        }
    }
    
    // A notifier of receive stream events
    private class ReceiveStreamNotifier extends Thread {


        // The receive stream listener
        private Vector listeners = null;

        // The event
        private ReceiveStreamEvent event = null;

        private ReceiveStreamNotifier(Vector listeners,
                ReceiveStreamEvent event) {
            this.listeners = listeners;
            this.event = event;
            start();
        }

        @Override
		public void run() {
                                    //System.out.println("FMJ : RTP STREAM!!");
            for (int i = 0; i < listeners.size(); i++) {
                ReceiveStreamListener listener =
                    (ReceiveStreamListener) listeners.get(i);
                listener.update(event);
            }
            releaseEventLock();
        }
    }

    // A notifier of receive session events
    private class SessionNotifier extends Thread {


        // The session listener
        private Vector listeners = null;

        // The event
        private SessionEvent event = null;

        private SessionNotifier(Vector listeners,
                SessionEvent event) {
            this.listeners = listeners;
            this.event = event;
            start();
        }

        @Override
		public void run() {
                                            //System.out.println("FMJ : RTP SESSION!!");
            for (int i = 0; i < listeners.size(); i++) {
                SessionListener listener =
                    (SessionListener) listeners.get(i);
                listener.update(event);
            }
            releaseEventLock();
        }
    }

    // A notifier of remote events
    private class RemoteNotifier extends Thread {


        // The remote listeners
        private Vector listeners = null;

        // The event
        private RemoteEvent event = null;

        private RemoteNotifier(Vector listeners,
                RemoteEvent event) {
            this.listeners = listeners;
            this.event = event;
            start();
        }

        @Override
		public void run() {
                                            //System.out.println("FMJ : RTP NOTIFIER!!");
            for (int i = 0; i < listeners.size(); i++) {
                RemoteListener listener =
                    (RemoteListener) listeners.get(i);
                listener.update(event);
            }
            releaseEventLock();
        }
    }

    /**
     * Initializes and starts the session.
     * Wrapper for the "initialize" methods, needed by JMF.
     * @param localAddress the local address
     * @param defaultSSRC the default SSRC. UNUSED.
     * @param defaultUserDesc the default SDES
     * @param rtcp_bw_fraction the bandwidth fraction allocated for RTCP traffic
     * @param rtcp_sender_bw_fraction the RTCP bandwidth fraction allocated for senders
     * @return 0
     */
    public int initSession(SessionAddress localAddress, long defaultSSRC,
            SourceDescription[] defaultUserDesc, double rtcp_bw_fraction,
            double rtcp_sender_bw_fraction) {
        initialize(new SessionAddress[]{localAddress}, defaultUserDesc,
                rtcp_bw_fraction,
                rtcp_sender_bw_fraction, null);
        return 0;
    }

    /**
     * Initializes and starts the session.
     * Wrapper for the "initialize" methods, needed by JMF.
     * @param localAddress the local address
     * @param defaultUserDesc the default SDES
     * @param rtcp_bw_fraction the bandwidth fraction allocated for RTCP traffic
     * @param rtcp_sender_bw_fraction the RTCP bandwidth fraction allocated for senders
     * @return 0
     */
    public int initSession(SessionAddress localAddress,
            SourceDescription[] defaultUserDesc, double rtcp_bw_fraction,
            double rtcp_sender_bw_fraction) {
        initialize(new SessionAddress[]{localAddress}, defaultUserDesc,
                rtcp_bw_fraction,
                rtcp_sender_bw_fraction, null);
        return 0;
    }

    /**
     * Starts a session.
     * Wrapper for addTarget, needed by JMF.
     * @param destAddress destination address to add as target
     * @param mcastScope multicast scope. UNUSED.
     * @param encryptionInfo encryption information. UNUSED.
     * @return 0
     * @throws java.io.IOException I/O Exception
     */
    public int startSession(SessionAddress destAddress, int mcastScope,
            EncryptionInfo encryptionInfo) throws IOException {
                                //System.out.println("FMJ : START!!");
        addTarget(destAddress);
        return 0;
    }

    /**
     * Starts a session.
     * Wrapper for addTarget, needed by JMF.
     * @param localReceiverAddress local receiver address. UNUSED.
     * @param localSenderAddress local sender address. UNUSED.
     * @param remoteReceiverAddress remote receiver address to add as target
     * @param encryptionInfo encryption information. UNUSED
     * @return 0
     * @throws java.io.IOException I/O Exception
     */
    public int startSession(SessionAddress localReceiverAddress,
            SessionAddress localSenderAddress,
            SessionAddress remoteReceiverAddress,
            EncryptionInfo encryptionInfo) throws IOException {
        addTarget(remoteReceiverAddress);
        return 0;
    }

    /**
     * Returns the default SSRC in this RTP session.
     * @return the current default SSRC for this RTP session
     */
    public long getDefaultSSRC() {
        /* We had to return SSRC_UNSPEC in the past if the session wasn't initialized,
         * but this is deprecated. */
        return ssrc;
    }

    /**
     * Returns the RTPStream corresponding to this SSRC. DUMMY.
     * Needed by JMF.
     * @param filterssrc SSRC. UNUSED.
     * @return null
     */
    public RTPStream getStream(long filterssrc) {
        return null;
    }

    /**
     * Returns the multicast scope.
     * @return 127
     */
    public int getMulticastScope() {
        return 127;
    }

    /**
     * Sets the multicast scope. DUMMY.
     * Needed by JMF.
     * @param multicastScope multicast scope. UNUSED.
     */
    public void setMulticastScope(int multicastScope) {
        // Does Nothing
    }

    /**
     * Closes this RTP Session.
     * @param reason Reason why this session is closed (used in the RTCP BYE packet following this call)
     */
    public void closeSession(String reason) {
        sayonara=true;
        
        /* If the sender RTCP bandwidth is inexistant, we leave without BYE */
        if  (rtcpReceiverBandwidthFraction == 0 && !we_sent) {
            return;
        }
    
        /* We want to quit the whole session, so we must send a BYE packet.
         * But we must conform to RFC-3550 6.3.7 if we have more than 50 participants
         * to avoid flooding the RTP Session.
         */
        if (getAllParticipants().size() < 50) { 
            removeTargets(reason);
            return;
        }
        
        long now = System.currentTimeMillis();
        RTCPSendTask.cancel();
        RTCPSendTask=null;
        reaperTimer.cancel();
        reaperTimer=null;
        we_sent=false;
        lastRTCPSendTime = -1;
        activeParticipants.clear();
        inactiveParticipants.clear();
        inactiveParticipants.put(localParticipant.getCNAME(), localParticipant);
        pmembers = 0;
        averageRTCPSize = LOWLAYERS + 4 + localParticipant.getStreams().size()*4;
        long delay = calculateRTCPDelay(false, false);
        lastRTCPSendTime = now;
        
        /* We're waiting to send our final BYE */
        while (((now = System.currentTimeMillis()) < (lastRTCPSendTime + delay))) {
            nextRTCPSendTime = lastRTCPSendTime + delay;
            try
            {
                Thread.sleep(delay);
            } catch (InterruptedException ex)
            {
                ex.printStackTrace();
            }
            long temp = lastRTCPSendTime;
            lastRTCPSendTime = -1;
            delay = calculateRTCPDelay(false, false);
            lastRTCPSendTime = temp;
        }
        removeTargets(reason);
    }

    /**
     * Generate a CNAME. In our case, it just returns the CNAME generated 
     * for the local participant.
     * @return the CNAME generated (ie, the local participant's one)
     */
    public String generateCNAME() {
        return localParticipant.getCNAME();
    }

    
    /**
     * Generates a Synchronization SouRCe (SSRC) number.
     * @return the ssrc generated
     */
    public long generateSSRC() {
        /* Chris: conforming to RFC 3550 - section 8.1 page 58.
         * A simple random() call is not sufficient */ 
        //return (long) (Math.random() * Integer.MAX_VALUE);
        
        /* The cast to long is required by JMF... weird since the SSRC is
         * 32 bits long and AFAIK there's no reason there could be any overflow */
        return generator.generate();
        
    }

    /**
     * Returns the session address. DUMMY.
     * Needed by JMF.
     * @return null
     */
    public SessionAddress getSessionAddress() {
        return null;
    }
    
    /**
     * Returns the remote session address. DUMMY.
     * Needed by JMF.
     * @return null
     */
    public SessionAddress getRemoteSessionAddress() {
        return null;	// TODO: added only for JMF Compatibility.  Called, for example in AVReceiver example.
    }

    /**
     * Returns the local session address.
     * @return the local session address
     */
    public SessionAddress getLocalSessionAddress() {
        return localAddress;
    }

    /**
     * Creates a send stream used to carry data from a source to some receivers over the network.
     * Wrapper for createSendStream, needed by JMF.
     * @param ssrc the stream's SSRC. UNUSED.
     * @param ds the datasource producing the data to send
     * @param streamindex the index of the stream in the datasource's streams table
     * @return the created SendStream
     * @throws javax.media.format.UnsupportedFormatException thrown if the datasource's stream only outputs formats which aren't supported
     * @throws java.io.IOException I/O Exception thrown in case of problem with streams
     */
    public SendStream createSendStream(int ssrc, DataSource ds,
            int streamindex) throws UnsupportedFormatException,
            IOException {
        return createSendStream(ds, streamindex);
    }

    /**
     * Starts a session. DUMMY.
     * Needed by JMF.
     * @param mcastScope multicast scope. UNUSED.
     * @param encryptionInfo encryption information. UNUSED.
     * @return -1
     */
    public int startSession(int mcastScope, EncryptionInfo encryptionInfo) {
        return -1;
    }

    /**
     * Add a peer to send RTP data to.
     * Wrapper for addTarger, needed by JMF.
     * @param peerAddress the remote address used as target
     * @throws java.io.IOException I/O Exception
     */
    public void addPeer(SessionAddress peerAddress) throws IOException {
        addTarget(peerAddress);
    }

    /**
     * Removes a receiver peer from the session.
     * Wrapper for removeTarget, needed by JMF.
     * @param peerAddress the peer's address which is used as target
     */
    public void removePeer(SessionAddress peerAddress) {
        removeTarget(peerAddress, "Leaving");
    }

    /**
     * Removes all the peers from this RTP session.
     * Wrapper for removeTargets, needed by JMF.
     */
    public void removeAllPeers() {
        removeTargets("Leaving");
    }

    /**
     * Returns all the receiver & senders peers we're sending RTP data to.
     * Wrapper for getAllParticipants, needed by JMF.
     * @return the list of all the peers/participants taking part in this RTP session
     */
    public Vector getPeers() {
        return getAllParticipants();
    }

    /**
     * Starts the sending of RTCP packets
     */
    public void start() {
        // Send the first RTCP packet
        long delay = (long) (generator.random() * 1000) + 500;
        rtcpTimer.schedule(new RTCPTimerTask(this), delay);
        globalReceptionStats.resetBytesRecd();
        //lastRTCPSendTime = System.currentTimeMillis();
        
        /* Our the Grim Reaper */
        reaperTimer.schedule(new CheckStreamsTimeout(this), new Date(), REAPER_PERIOD);
    }

    /**
     * Calculate the delay which must seperate two RTCP packets transmission
     * in this RTP session (RTCP Transmission Interval).
     * 
     * The control traffic (RTCP) is not self-limiting.  If the reception
     * reports from each participant were sent at a constant rate, the
     * control traffic would grow linearly with the number of participants.
     * Therefore, the rate must be scaled down by dynamically calculating
     * the interval between RTCP packet transmissions.
     * 
     * Refer to the RFC-3550 6.2 for more details.
     * @return the computed RTCP Transmission Interval
     */
    private long calculateRTCPDelay() {
        return calculateRTCPDelay(false, we_sent /*localParticipant.getStreams().size() > 0*/);
    }
    
    
    /**
     * Calculate the delay which must seperate two RTCP packets transmission
     * in this RTP session (RTCP Transmission Interval).
     * 
     * This version allows the programmer to calculate a customized delay, specifying
     * if the RTCP Transmission Interval wanted must be deterministic (without the
     * randomizing and compensation factors) and if it must be computer for a sender
     * of receiver.
     * @param deterministic specifies if the RTCP Transmission Interval wanted must be deterministic (without the
     * randomizing and compensation factors). This is useful to calculate the deterministic
     * interval of an other participant.
     * @param is_sender true if we want to know the delay for a sender participant
     * @return the RTCP Transmission Interval for this session considering the parameters passed
     */
    private long calculateRTCPDelay(boolean deterministic, boolean is_sender) {
        long delay = MIN_RTCP_INTERVAL;
        long rtcp_min_time = MIN_RTCP_INTERVAL;
        
        /* Bandwidth in bytes/ms !!*/
        double bandwidth = ((double) globalReceptionStats.getBytesRecd() /
                (System.currentTimeMillis() - lastRTCPSendTime));
        
        /* To make things a bit more readable */
        int nbSenders = activeParticipants.size();
        int nbReceivers = inactiveParticipants.size();
        long nbMembers =  nbSenders + nbReceivers;
        
        /*TESTING*/
        /*System.out.println("Bytes "+globalReceptionStats.getBytesRecd()
            +" / Time "+(System.currentTimeMillis() - lastRTCPSendTime)+" => "+bandwidth
            +"\n Last RTCP "+lastRTCPSendTime);
        */
        
        /* To be sure to avoid DIV/0, we set bandwidth to 1 if it's equal to 0 */
        //if (bandwidth <= 0.1) bandwidth = 1;
        
        /* On startup, when no RTCP packet has been sent, we can use half of the
         * minimum delay to get a quicker notification.
         */
        if (!deterministic && lastRTCPSendTime < 0)
            rtcp_min_time /= 2;
        
        //TODO: Putting the number of bytes reveived to 0 when sending an RTCP packet (in sendRTCPPacket)
        // looks strange. We'd better use a bandwidth calculation based on the data received in
        // the whole session and using a formula like the one used for the averageRTCPSize to get
        // rapid reactions to bandwidth changes.
        
        /* 20 packets sounds like a good limit after which we can start using 
         * the dynamic RTCP minimum delay computation */
        if (!deterministic && globalReceptionStats.getBytesRecd() > 20*averageRTCPSize && bandwidth > 1) {
            /* We first calculate the precise bandwidth in kbits/s 
             * and then we can compute the minimum RTCP delay (in ms).
             * (conforming to RFC-3550 end of page 25)  */
            double bw = bandwidth * 8 * 1000 / 1024;
            rtcp_min_time = (long)((360 / bw) * 1000);
            //System.out.println("RTCP Dynamic delay : "+rtcp_min_time+"ms ["+bw+"]");
        }
           
        if (bandwidth < 0.1) {
            delay = rtcp_min_time;
        } else {
            double senderFraction = 0;
            
            /* Calculate the ratio senders/members.
             * activeParticipants = senders, 
             * inactivePartipants = receivers
             */
            if (nbMembers > 0) {
                senderFraction = nbSenders / nbMembers;
            }
            
            /* Allocate sufficient bandwidth to senders : 
             * . if the senders represent less than a specified fraction of 
             *   the members (default is 25%), reserve the specified fraction 
             *   for them;
             * . else simply let them share the bandwidth proportionaly to
             *   their number
             */
            double ratio = rtcpSenderBandwidthFraction / 
                    (rtcpSenderBandwidthFraction + rtcpReceiverBandwidthFraction);
            
            if ((nbSenders > 0) && (senderFraction < ratio)) {
                if (is_sender) {
                    delay = (long) ((averageRTCPSize *nbSenders) /
                            (bandwidth * rtcpSenderBandwidthFraction));
                } else {
                    /* Special test : if rtcpReceiverBandwidth == 0, we have ratio = 1 
                     * => senderFraction < ratio, and if is_sender == false, 
                     * we end here with a division by zero.
                     * To avoid this, we set the delay to rtcp_min_time.
                     */
                    if (rtcpReceiverBandwidthFraction == 0) delay = rtcp_min_time;
                    else delay = (long) ((averageRTCPSize * nbReceivers) /
                            (bandwidth * rtcpReceiverBandwidthFraction));
                }
            } else {
                delay = (long) ((averageRTCPSize * nbMembers) /
                        (bandwidth * (rtcpSenderBandwidthFraction +
                                rtcpReceiverBandwidthFraction)));
            }
            if (delay < rtcp_min_time) {
                delay = rtcp_min_time;
            }
        }
        
        
        /* If we're calculating the deterministic interval Td, we return here */
        if (deterministic) return delay;
        
        /* Conforming to RFC-3550 : the delay must be made uniformly random
         * on the entire 0.5*T < delay < 1.5*T interval to avoid traffic bursts 
         * from unintended synchronization with other sites.
         * We use SSRC.nextFastTruncatedDouble() with as parameter the space on
         * which we want the generation to be done. Since we need a generation
         * on 48 bits, we pass 6 bytes as length.
         */
        delay *= (generator.random() + 0.5);
        
        /* We compensate the fact that the timer reconsideration algorithm
         * converges to a value of the RTCP bandwidth below the intended average.
         * Therefore, we divide delay by e-3/2 as specified in the RFC-3550.
         */
        delay /= 1.21828;
        
        //System.out.println("Generated delay : " + delay);
        
        pmembers = nbMembers;
        
        return delay;
    }

    private void writeSenderInfo(DataOutputStream output, RTPSendStream sendStream) throws IOException
    {
        /* Chris: conforming to the RFC, we must send the NTP time at which
         * this report is sent, not the time at which the last packets were
         * sent. RFC 3550 Page 36
         */

        // this is the wallclock time, TODO: increase resolution, we can use System.nanoseconds, but we must sync
        // both clocks, nanoseconds is only good for relative timestamp !
        // currentTimeMillis() has a bad resolution under Win98 ~50ms WinXP has a resolution of ~15ms, linux ~1ms
        long sendtime = System.currentTimeMillis();

        // NTP starts at 1.Jan.1900 ( but sendtime starts at 1.Jan.1970, adjust sendtime)
        sendtime = sendtime - RTCPSenderInfo.MSB_1_BASE_TIME;

        // NTP timestamp MSW
        long sendTimeSeconds = sendtime / 1000;

        // mgodehardt: NTP timestamp LWS (a 1 in the fractional part is (1 / 2**32) second, ~233 picoseconds)
        double fractionalPart = (double)(sendtime % 1000);
        fractionalPart = fractionalPart / 1000.0d;
        fractionalPart = fractionalPart * 4294967296.0d;
        long sendTimeFractional = (long)fractionalPart;

        // RFC 3550 Page 37 (Corresponds to the same time as the NTP timestamp, but in
        // the same units and with the same random offset as the RTP timestamps in data packets)
        // is calculated base on the ntp timestamp using the relationship between rtp timestamp and real time
        double timestamp = (double)(sendStream.getLastSendTime() - sendStream.getInitialSendTime()) * ((double)sendStream.getClockRate() / 1000.0d);
        long rtpTimestamp = sendStream.getInitialTimestamp() + Math.round(timestamp);

        TransmissionStats stats = sendStream.getSourceTransmissionStats();

        output.writeInt((int) (sendStream.getSSRC() & 0xFFFFFFFF));
        output.writeInt((int) (sendTimeSeconds & 0xFFFFFFFF));
        output.writeInt((int) (sendTimeFractional & 0xFFFFFFFF));
        output.writeInt((int) (rtpTimestamp & 0xFFFFFFFF));
        output.writeInt(stats.getPDUTransmitted());
        output.writeInt(stats.getBytesTransmitted());
    }

    private void writeSDESPacket(DataOutputStream output) throws IOException
    {
        Vector sdesItems = localParticipant.getSourceDescription();
        if ( sdesItems.size() > 0 )
        {
            writeSDESPacketImpl(output, ssrc, sdesItems, ((rtcpPacketsSent % 3) == 0) ? false : true);
        }
    }

    private void writeSDESPacketImpl(DataOutputStream output, long ssrc, Vector sdesItems, boolean onlyCNAME) throws IOException
    {
        // see RFC 3550 (6.5 SDES: Source Description RTCP Packet) for details
        // only every third packet all SDES items are sent
        // CNAME is mandatory and must be included in every RTCP report send ( ssrc may change )

        // initial length (SSRC and the terminating SDES item, a zero octet)
        int sdesLength = 5;
        for (int i=0; i<sdesItems.size(); i++)
        {
            SourceDescription sdesItem = (SourceDescription)sdesItems.elementAt(i);
            if ( !onlyCNAME || (sdesItem.getType() == SourceDescription.SOURCE_DESC_CNAME) )
            {
                sdesLength += sdesItem.getDescription().getBytes("UTF-8").length + 2;
            }
        }

        // do we need padding to next 32-bit boundary
        int padding = 0;
        if ( (sdesLength % 4) != 0 )
        {
            padding = 4 - (sdesLength % 4);
            sdesLength += padding;
        }

        // write the rtcp sdes packet
        output.writeByte(0x81);
        output.writeByte(RTCPPacket.PT_SDES & 0xFF);
        output.writeShort(sdesLength / 4);

        // only one chunk
        output.writeInt((int)(ssrc & 0xFFFFFFFF));

        // write sdes item(s) for ssrc
        for (int i=0; i<sdesItems.size(); i++)
        {
            SourceDescription sdesItem = (SourceDescription)sdesItems.elementAt(i);
            if ( !onlyCNAME || (sdesItem.getType() == SourceDescription.SOURCE_DESC_CNAME) )
            {
                output.writeByte(sdesItem.getType() & 0xFF);

                byte[] desc = sdesItem.getDescription().getBytes("UTF-8");
                output.writeByte(desc.length & 0xFF);
                output.write(desc);
            }
        }

        // terminating zero octet, marks end of sdes items
        output.writeByte(0);

        for (int i=0; i<padding; i++)
        {
            output.writeByte(0);
        }
    }

    /**
     * Sends an RTCP packet, and schedules the next one
     */
    public void sendRTCPPacket()
    {
        // mgodehardt, TODO: multi threading issues, this is a separate thread, should be synced with RTP and RTCP receive threads
        // otherwise some statistics may be wrong ( lost packets, jitter calculations )
        
        /* If we're about to leave the session, return to avoid messing up with the
         * BYE sending procedure. */
        if (sayonara) return;
        
        long delay = calculateRTCPDelay();
        long now = System.currentTimeMillis();

        /* Wait until later if :
         * . it's too early
         * . OR if we're a receiver (!we_sent) 
         *   and no bandwidth was allocated for receivers
         */
        if ((now < (lastRTCPSendTime + delay))
          | (rtcpReceiverBandwidthFraction == 0 && !we_sent)) {
            nextRTCPSendTime = lastRTCPSendTime + delay;
            RTCPSendTask = new RTCPTimerTask(this);
            rtcpTimer.schedule(RTCPSendTask, new Date(nextRTCPSendTime));
        } else {
            
            /* If we have more than 31 report blocks to send, we use a round-robin
             * to send each part in a different RTCP interval. */
            Vector streams = new Vector(receiveStreams.values());
            int rc = streams.size();
            if (rc > 31) {
                if (RRRoundRobin >= rc) RRRoundRobin = 0;
                rc = (rc - RRRoundRobin > 31)? 31: rc - RRRoundRobin;
            } else RRRoundRobin = 0;
            
            // Reset the stats
            globalReceptionStats.resetBytesRecd();
            
            // Get the packet details
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(bytes);

            try {

                // Determine the packet type
                int packetType = RTCPPacket.PT_RR;
                int packetSize = (rc * RTCPFeedback.SIZE) + 8;
                if (we_sent)
                {
                    packetType = RTCPPacket.PT_SR;
                    packetSize += RTCPSenderInfo.SIZE;
                }

                // Add a RTCP header
                output.writeByte(0x80 | 0 | (rc & 0x1F));
                output.writeByte(packetType & 0xFF);
                output.writeShort(((packetSize) / 4) - 1);

                // If we are a sender, add sender stats
                if (we_sent)
                {
                    /* As specified in the RFC, we randomly pick a stream and send
                     * information about it */
                     // in which RFC ???
                    //int senderIndex = (int) (Math.random() * localParticipant.getStreams().size());
                    //RTPSendStream sendStream = (RTPSendStream) localParticipant.getStreams().get(senderIndex);

                    // mogdehardt: for unicast only one media per RTP session is used, so we have one stream
                    // TODO: check multicast and broadcast
                    RTPSendStream sendStream = (RTPSendStream)localParticipant.getStreams().get(0);
                    writeSenderInfo(output, sendStream);
                }
                else
                {
                    /* Add the generic SSRC if we're not a sender */
                    output.writeInt((int) (ssrc & 0xFFFFFFFF));
                }
                
                // Add a reception report block  ( RFC 3550 Page 38, TODO: do not send if all packets were lost )
                if ( streams.size() > 0 )
                {
                    now = System.currentTimeMillis();
                    for (int i = RRRoundRobin; i < RRRoundRobin + rc; i++)
                    {
                        RTPReceiveStream stream = (RTPReceiveStream) streams.get(i);
                        RTPReceptionStats stats = (RTPReceptionStats) stream.getSourceReceptionStats();
                        RTPDataSource dataSource = (RTPDataSource) stream.getDataSource();
                        RTPDataStream dataStream = (RTPDataStream) dataSource.getStreams()[0];
                        
                        // lossFraction since the previous SR/RR (RFC 3550 Page 38), TODO: do not send a RR when all packets are lost (RFC 3550
                        long lossFraction = 0;
                        
                        // RFC 3550 Page 38 (extended highest sequence number received)
                        long extendedHighestSequenceNumber = dataStream.itsRTPBuffer.getExtendedHighestSequenceNumber();
                        
                        // TODO: mgodehardt calc loss of fraction
                        /*if ( dataStream.lastPacketsReceived > 0 )
                        {
                            // RFC 3550 Page 82
                            long expected_interval = extendedHighestSequenceNumber - dataStream.lastExtendedHighestSequenceNumber;
                            long received_interval = dataStream.getPacketsReceived() - dataStream.lastPacketsReceived;
                            long lost_interval = expected_interval - received_interval;
                            
                            if ( (expected_interval == 0) || (lost_interval <= 0) )
                            {
                                lossFraction = 0;
                            }
                            else
                            {
                                double lf = (double)(lost_interval << 8) / (double)expected_interval;
                                lossFraction = Math.round(lf);
                            }
                        }*/
                        
                        int cumulativePacketLoss = dataStream.itsRTPBuffer.getCumulativePacketLoss();

                        long jitter = dataSource.getJitter();

                        // RFC 3550 Page 11 the middle 32 Bits of the 64 Bit NTP Timestamp of the last sender report
                        long lsrMSW = stream.getLastSRReportTimestampMSW();
                        long lsrLSW = stream.getLastSRReportTimestampLSW();

                        // RFC 3550 Page 39 ( for round trip calculations )
                        long DLSR = 0;
                        if ( stream.getLastSRReportTime() > 0 )
                        {
                            DLSR = ((now - stream.getLastSRReportTime()) * 65536) / 1000;
                        }

                        output.writeInt((int) (stream.getSSRC() & 0xFFFFFFFF));
                        output.writeByte((int)(lossFraction & 0xFF));
                        output.writeByte((cumulativePacketLoss >> 16) & 0xFF);
                        output.writeShort((cumulativePacketLoss & 0xFFFF));
                        output.writeInt((int) (extendedHighestSequenceNumber & 0xFFFFFFFF));
                        output.writeInt((int) (jitter & 0xFFFFFFFF));
                        output.writeShort((int) (lsrMSW & 0xFFFF));
                        output.writeShort((int) ((lsrLSW >> 16) & 0xFFFF));
                        output.writeInt((int) (DLSR & 0xFFFFFFFF));
                    }
                }

                writeSDESPacket(output);

            }
            catch (IOException e)
            {
                logger.log(Level.WARNING, "" + e, e);
            }

            byte[] data = bytes.toByteArray();
            try
            {
                OutputDataStream outputStream = socket.getControlOutputStream();
                output.close();
                bytes.close();
                data = bytes.toByteArray();
                outputStream.write(data, 0, data.length);
                rtcpPacketsSent++;
            }
            catch (IOException e)
            {
                logger.log(Level.WARNING, "" + e, e);
                dispose();
            }
            
            // RTP average Size computation
            if (data != null) computeAverageRTPSize(data.length);
            
            // Prepare to send the next packet
            if (!done) {
                delay = calculateRTCPDelay();
                lastRTCPSendTime = System.currentTimeMillis();
                nextRTCPSendTime = lastRTCPSendTime + delay;
                rtcpTimer.schedule(new RTCPTimerTask(this), new Date(nextRTCPSendTime));
                if (rc > 31) {
                    RRRoundRobin += 31;
                    if (RRRoundRobin > rc) RRRoundRobin = 0;
                }
            }
        }
    }

    
    /**
     * A timer task for sending RTCP packets
     */
    class RTCPTimerTask extends TimerTask {

        private RTPSessionMgr rtpSessionManager = null;

        private RTCPTimerTask(RTPSessionMgr rtpSessionManager) {
            this.rtpSessionManager = rtpSessionManager;
        }

        @Override
		public void run() {
            if (!sayonara) {
                rtpSessionManager.sendRTCPPacket();
                return;
            }
        }
    }
    
    
  
    /**
     * A private class to periodically check the streams to remove those
     * which have timed-out.
     */
    private class CheckStreamsTimeout extends TimerTask {
        
        /* The timeout multipliers (cf. RFC3550 6.3.5) */
        private final int TIMEOUT_MULT = 5;
        private final int SENDER_TIMEOUT_MULT = 2;
        
        javax.media.rtp.RTPManager rtpmgr;
        
        public CheckStreamsTimeout(javax.media.rtp.RTPManager rtpmgr) {
            this.rtpmgr = rtpmgr;
            //System.out.println("RTP: Reaping streams - Checking for timeouts...");
        }
        
        @Override
		public void run() {
            
            // TODO : Access to the Hashmaps should be synchronized !!
             
            boolean modified = false;
            
            logger.finer("Reaping...");
            
            /* FIRST PASS : Check the receivers 
             * --------------------------------
             */
            
            /* Compute the deterministic interval Td for receivers */
            long delay = calculateRTCPDelay(true, false);
            
            long timeLimit = System.currentTimeMillis() - TIMEOUT_MULT*delay;
       
            Set cnames = inactiveParticipants.keySet();
            Iterator it = cnames.iterator();
            while (it.hasNext()) {
                String cname = (String)it.next();
                RTPParticipant pal = (RTPParticipant)inactiveParticipants.get(cname);
                
                if (!pal.equals(localParticipant)) {
                    /*Vector streamsV = pal.getStreams();
                    for (int i = 0; i < streamsV.size(); i++) {
                        RTPReceiveStream stream = (RTPReceiveStream)streamsV.get(i);
                        stream.getLastSRReportTime();
                    }*/
                    long lastSR = pal.getLastReportTime();
                    if (lastSR < timeLimit) {
                        /* This participant timed out! */
                    
                        // TODO: Stop our send streams ?
                    
                        // Remove the participant
                        inactiveParticipants.remove(pal.getCNAME());
                        //System.out.println(pal+" TIMED OUT");
                        modified = true;
                    }
                }
            }
            
            
            /* SECOND PASS : Check the senders
             * -------------------------------
             */
            
            /* Compute the deterministic interval T for senders */
            delay = calculateRTCPDelay(true, true);
            
            timeLimit = System.currentTimeMillis() - SENDER_TIMEOUT_MULT*delay;
            
            cnames = activeParticipants.keySet();
            it = cnames.iterator();
            while (it.hasNext()) {
                String cname = (String)it.next();
                RTPParticipant pal = (RTPParticipant)activeParticipants.get(cname);
                                              
                long lastSR = pal.getLastReportTime();
                if (lastSR < timeLimit) {
                    /* This participant timed out! */
                    
                    /* If it's the localParticipant, then it's no real timeout,
                     * just part of the algorithm to update we_sent described in
                     * RFC-3550 6.3.4.
                     */
                    if (pal.equals(localParticipant)) {
                        we_sent = false;
                        inactiveParticipants.put(localParticipant.getCNAME(), localParticipant);
                    } else {
                        // Remove its streams
                        Vector streamsV = pal.getStreams();
                        for (int i = 0; i < streamsV.size(); i++) {
                            RTPStream stream = (RTPStream)streamsV.get(i);
                            receiveStreams.remove(new Long(stream.getSSRC()));
                            senders.remove(new Long(stream.getSSRC()));
                        }
                        modified = true;
                    }
                    // Remove the participant
                    activeParticipants.remove(pal.getCNAME());
                    //System.out.println(pal+" TIMED OUT [sender]");
                } 
                
                /* Check only streams timeout to send InactiveReceiveStreamEvent */
                else if (!pal.equals(localParticipant)) {
                    Vector streamsV = pal.getStreams();
                    for (int i = 0; i < streamsV.size(); i++) {
                           RTPReceiveStream stream = (RTPReceiveStream)streamsV.get(i);
                           if (stream.checkInactivity())
                           {
                               // mgodehardt: resetting bitratecontrol
                               DataSource ds = stream.getDataSource();
                               if ( ds instanceof PushBufferDataSource )
                               {
                                   PushBufferDataSource pbds = (PushBufferDataSource)ds;
                                   PushBufferStream pbs = pbds.getStreams()[0];
                                   if ( pbs instanceof RTPDataStream )
                                   {
                                       ((RTPDataStream)pbs).bitsPerSecond = 0;
                                   }
                               }
                               /* Trigger InactiveReceiveStreamEvent */
                               getEventLock();
                               new ReceiveStreamNotifier(receiveStreamListeners,
                                    new InactiveReceiveStreamEvent((SessionManager)rtpmgr, pal, stream, (streamsV.size() == 1)));
                           } 
                    }
                }
            }
            
            /* THIRD PASS : Check the unmapped streams
             * ---------------------------------------
             */
            Set unassigned = unassignedStreams.keySet();
            it = unassigned.iterator();
            while (it.hasNext()) {
                RTPReceiveStream stream = (RTPReceiveStream)unassignedStreams.get(it.next());
                if (stream.checkInactivity()) {
                    /* Trigger InactiveReceiveStreamEvent */
                    getEventLock();
                    new ReceiveStreamNotifier(receiveStreamListeners,
                           new InactiveReceiveStreamEvent((SessionManager)rtpmgr, new RTPParticipant("Unknown"), stream, true));
                }
            }
            
            if (modified) {
                /* Reverse reconsideration algorithm */
                doReverseReconsideration();
            }
            
            /* TODO: maybe add a BYE event when participants time out ? */
            /*getEventLock();
            new ReceiveStreamNotifier(receiveStreamListeners,
                          new ByeEvent((SessionManager)rtpmgr, (Participant)participant, (ReceiveStream)stream,
                                        report.getByeReason(),
                                        participant.getStreams().size() == 0));
           */
            
            
           /* TODO: Find a way to check dead streams not assigned to any participant */
           
        }
    }
    
    /**
     * Sort of callback method called by RTPSendStreams to inform the RTPSessionManager they're linked to 
     * that an RTP packet has been sent by the local participant. This is used to maintain some local 
     * variables and statistics used in several algorithms (reaper timer and RTCP delay calculation for example).
     * @param time time when the packet was sent
     * @param size size of the packet sent
     */
    public synchronized void RTPPacketSent(long time, int size) {
        
        /* If we're about to leave the session, return to avoid messing up with the
         * BYE sending procedure. */
        if (sayonara) return;
        
        /* We update the lastReportTime of the localParticipant */
        localParticipant.touch(time);
        
        /* We set we_sent to true and move the localParticipant to the active member list */
        we_sent = true;
        activeParticipants.put(localParticipant.getCNAME(), localParticipant);
        inactiveParticipants.remove(localParticipant.getCNAME());
        
        /* We use this to compute the average RTP/RTCP size */
        computeAverageRTPSize(size);
    }
    
    /**
     * Apply the reverse reconsideration.
     * 
     * This algorithm is meant to reduce the incidence and duration of false 
     * participant timeouts when the number of participants drops rapidly.  
     * Reverse reconsideration is also used to possibly shorten the delay 
     * before sending RTCP SR when transitioning from passive receiver to 
     * active sender mode.
     * 
     * Refer to the RFC-3550 Sections 6.3.3, 6.3.6 and 6.3.7 for more details.
     */
    synchronized private void doReverseReconsideration() {
        if (RTCPSendTask != null && !sayonara) {
                    /* Cancel the programmed task */
                    RTCPSendTask.cancel();
                    
                    /* Apply the reverse reconsideration algorithm */
                    long now = System.currentTimeMillis();
                    long nbMembers = inactiveParticipants.size()+activeParticipants.size();
                    nextRTCPSendTime = now + (nbMembers/pmembers) * (nextRTCPSendTime - now);
                    lastRTCPSendTime = now - (nbMembers/pmembers) * (now - lastRTCPSendTime);
                    pmembers = nbMembers;
                    
                    /* Program the new task */
                    RTCPSendTask = new RTCPTimerTask(this);
                    rtcpTimer.schedule(RTCPSendTask, new Date(nextRTCPSendTime));
                    
        }
    }

    /**
     * Returns the average RTP packet size computed until now.
     * @return the average RTP packet size
     */
    public int getAverageRTCPSize()
    {
        return averageRTCPSize;
    }
    
    /**
     * Calculate the average RTP/RTCP packet size if the report
     * is not a BYE. Conforming to the RFC-3550 6.3.3.
     * @param length the length of the packet that just arrived 
     */
    synchronized void computeAverageRTPSize(int length) {
        averageRTCPSize = (length + LOWLAYERS) * (1/16) + averageRTCPSize * (15/16);
    } 
}
