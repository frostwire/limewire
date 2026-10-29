/*
 * @(#)RTPSocketAdapter.java
 * Created: 2005-04-21
 * Version: 1-1-alpha2
 * Copyright (c) 2005-2006, University of Manchester All rights reserved. 
 * Andrew G D Rowley
 *
 * Modified by Christian Vincenot <sipcom@cyberspace7.net>
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
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

import javax.media.protocol.PushSourceStream;
import javax.media.rtp.OutputDataStream;
import javax.media.rtp.RTPConnector;
import javax.media.rtp.SessionAddress;

import memetic.crypto.EncryptedRTPSocket;
import memetic.crypto.RTPCrypt;

/**
 * An implementation of RTPConnector based on UDP sockets.
 * 
 * @author Andrew G D Rowley
 * @author Christian Vincenot
 * @version 1-1-alpha2
 */
public class RTPSocketAdapter implements RTPConnector {

    // The data socket
    private EncryptedRTPSocket dataSock;

    // The control socket
    private EncryptedRTPSocket ctrlSock;

    // The address to send data to
    private InetAddress addr;

    // The port to send / receive data
    private int port;

    // The input stream of the data socket
    private SimpleSocketInputStream dataInStrm = null;

    // The input stream of the control socket
    private SimpleSocketInputStream ctrlInStrm = null;

    // The output stream of the data socket
    private SocketOutputStream dataOutStrm = null;

    // The output stream of the control socket
    private SocketOutputStream ctrlOutStrm = null;

    /**
     * Creates a new RTPSocketAdaptor with TTL 1
     * @param addr The address of the socket
     * @param port The port of the socket (data)
     * @throws IOException I/O Exception
     */
    public RTPSocketAdapter(InetAddress addr, int port) throws IOException {
        this(addr, port, 1);
    }
    
    /**
     * Creates a new RTPSocketAdaptor
     * @param laddr The local address to bind to
     * @param addr The address of the socket
     * @param port The port of the socket
     * @param ttl The ttl for sending data
     * @throws IOException I/O Exception
     */
    public RTPSocketAdapter(InetAddress addr, int port, 
            int ttl)
            throws IOException {
        try {

            // If the address is multicast, create multicast sockets and joing
            // groups etc
            if (addr.isMulticastAddress()) {
                dataSock = new EncryptedRTPSocket(null, port);
                ctrlSock = new EncryptedRTPSocket(null, port + 1);
                //((MulticastSocket) dataSock).setInterface(laddr);
                ((MulticastSocket) dataSock).joinGroup(addr);
                ((MulticastSocket) dataSock).setTimeToLive(ttl);
                ((MulticastSocket) dataSock).setLoopbackMode(false);
                //((MulticastSocket) ctrlSock).setInterface(laddr);
                ((MulticastSocket) ctrlSock).joinGroup(addr);
                ((MulticastSocket) ctrlSock).setTimeToLive(ttl);
                ((MulticastSocket) ctrlSock).setLoopbackMode(false);
            }

            // If the address is unicast, create unicast sockets
            else {
                dataSock = new EncryptedRTPSocket(null, port);
                ctrlSock = new EncryptedRTPSocket(null, port + 1);
            }
        } catch (SocketException e) {
            throw new IOException(e.getMessage());
        }

        // Set the address and port
        this.addr = addr;
        this.port = port;
        
    }

    /**
     * Sets the encryption on the socket
     * 
     * @param crypter
     *            The encrypter/decrypter
     */
    public void setEncryption(RTPCrypt crypter) {
        dataSock.setEncryption(crypter);
        ctrlSock.setEncryption(crypter);
    }

    /**
     * Returns an input stream to receive the RTP data.
     * @return input stream to receive the RTP data
     */
    public PushSourceStream getDataInputStream() {
        if (dataInStrm == null) {
            dataInStrm = new SimpleSocketInputStream(dataSock);
            dataInStrm.setName("SimpleSocketInputStream [data], port " + port);
            dataInStrm.start();
        }
        return dataInStrm;
    }

    /**
     * Add a stream target. A stream target is the destination address which 
     * this RTP session will send its data to. For a single session, we can add
     * multiple SessionAddresses, and for each address, one copy of data will be
     * sent to.
     *
     * @param target Destination target address 
     */
    public void addTarget(SessionAddress target) {
        if (ctrlOutStrm == null) {
            ctrlOutStrm = new SocketOutputStream(ctrlSock);
        }
        ctrlOutStrm.addTarget(target.getControlAddress(),
                                        target.getControlPort());

        if (this.dataOutStrm == null) {
            this.dataOutStrm = new SocketOutputStream(dataSock);
        }
        dataOutStrm.addTarget(target.getDataAddress(),
                                        target.getDataPort());
    }
    /**
     * Returns an output stream to send the RTP data.
     * @return output stream to send the RTP data
     */
    public OutputDataStream getDataOutputStream() {
        if (dataOutStrm == null) {
            dataOutStrm = new SocketOutputStream(dataSock);
        }
        return dataOutStrm;
    }

    /**
     * Returns an input stream to receive the RTCP data.
     * @return input stream to receive the RTCP data
     */
    public PushSourceStream getControlInputStream() {
        if (ctrlInStrm == null) {
            ctrlInStrm = new SimpleSocketInputStream(ctrlSock);
            ctrlInStrm.setName("SimpleSocketInputStream [ctrl], port " + (port + 1));
            ctrlInStrm.start();
        }
        return ctrlInStrm;
    }

    /**
     * Returns an output stream to send the RTCP data.
     * @return an output stream to send the RTCP data
     */
    public OutputDataStream getControlOutputStream() {
        if (ctrlOutStrm == null) {
            ctrlOutStrm = new SocketOutputStream(ctrlSock);
        }
        return ctrlOutStrm;
    }

    /**
     * Close all the RTP, RTCP streams.
     */
    public void close() {
        if (dataInStrm != null) {
            dataInStrm.kill();
        }
        if (ctrlInStrm != null) {
            ctrlInStrm.kill();
        }
        dataSock.close();
        ctrlSock.close();
    }

    /**
     * Removes a target from our session. If a target is removed, there will be
     * no data sent to that address.
     *
     * @param target Destination target to be removed
     * @return true if the target address is removed successfully
     *         false if there is something wrong 
     */
    public boolean removeTarget(SessionAddress target) {
        boolean ok = true;

        if (this.ctrlOutStrm != null) {
            ok &= this.ctrlOutStrm.removeTarget(target.getControlAddress(),
                                                    target.getControlPort());
        }

        if (this.dataOutStrm != null) {
            ok &= this.dataOutStrm.removeTarget(target.getDataAddress(),
                                                    target.getDataPort());
        }
        return ok;
    }

    /**
     * Remove all stream targets. After this operation is done. There will be
     * no targets receiving data, so no data will be sent.
     */
    public void removeTargets() {
        if (this.ctrlOutStrm != null) {
            this.ctrlOutStrm.removeTargets();
        }

        if (this.dataOutStrm != null) {
            this.dataOutStrm.removeTargets();
        }
    }
    
    /**
     * Set the receive buffer size of the RTP data channel. This is only a hint
     * to the implementation. The actual implementation may not be able to do
     * anything to this.
     * @param size the wanted size of the buffer
     * @throws java.io.IOException I/O Exception
     */
    public void setReceiveBufferSize(int size) throws IOException {
        dataSock.setReceiveBufferSize(size);
    }

    /**
     * Get the receive buffer size set on the RTP data channel. Return -1 if the
     * receive buffer size is not applicable for the implementation.
     * @return the buffer's size
     */
    public int getReceiveBufferSize() {
        try {
            return dataSock.getReceiveBufferSize();
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Set the send buffer size of the RTP data channel. This is only a hint to
     * the implementation. The actual implementation may not be able to do
     * anything to this.
     * @param size the wanted size of the buffer
     * @throws java.io.IOException I/O Exception
     */
    public void setSendBufferSize(int size) throws IOException {
        dataSock.setSendBufferSize(size);
    }

    /**
     * Get the send buffer size set on the RTP data channel. Return -1 if the
     * send buffer size is not applicable for the implementation.
     * @return the buffer's size
     */
    public int getSendBufferSize() {
        try {
            return dataSock.getSendBufferSize();
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Return the RTCP bandwidth fraction. This value is used to initialize the
     * RTPManager. Check RTPManager for more detauls. Return -1 to use the
     * default values.
     * @return the bandwidth fraction allocated for RTCP traffic
     */
    public double getRTCPBandwidthFraction() {
        return -1;
    }

    /**
     * Return the RTCP sender bandwidth fraction. This value is used to
     * initialize the RTPManager. Check RTPManager for more detauls. Return -1
     * to use the default values.
     * @return the RTCP bandwidth fraction allocated for senders' traffic
     */
    public double getRTCPSenderBandwidthFraction() {
        return -1;
    }
 
}
