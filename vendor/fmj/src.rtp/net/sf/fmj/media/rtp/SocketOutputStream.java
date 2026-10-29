/*
 * @(#)SocketOutputStream.java
 * Created: 26-Oct-2005
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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Vector;

import javax.media.rtp.OutputDataStream;

/**
 * An output Datagram Socket for JMF
 * 
 * @author Andrew G D Rowley
 * @author Christian Vincenot
 * @version 1-1-alpha3
 */
public class SocketOutputStream implements OutputDataStream {
    
    // The datagram socket of the stream
    private DatagramSocket socket = null;
        
    /**
     * Stream targets' ip addresses
     */
    private Vector<InetAddress> remoteAddrs;
    
    /**
     * Stream targets' ports, corresponding to their ip addresses.
     */
    private Vector<Integer> remotePorts;

    /**
     * Creates a new SocketOutputStream
     * @param socket The datagram socket to adapt
     * @param sendAddress The address to send packets to
     * @param sendPort The port to send packets to
     */
    public SocketOutputStream(DatagramSocket sckt) {
        this.socket = sckt;
        remoteAddrs = new Vector<InetAddress>();
        remotePorts = new Vector<Integer>();
    }

    /**
     * Add a target to stream targets list
     * 
     * @param remoteAddr target ip address
     * @param remotePort target port
     */
    public void addTarget(InetAddress remoteAddr, int remotePort) {
        remoteAddrs.add(remoteAddr);
        remotePorts.add(new Integer(remotePort));
    }

    /**
     * Remove a target from stream targets list
     * 
     * @param remoteAddr target ip address
     * @param remotePort target port
     * @return true if the target is in stream target list and can be removed
     *         false if not
     */
    public boolean removeTarget(InetAddress remoteAddr, int remotePort) {
        boolean ok = true;
        ok = ok && remoteAddrs.remove(remoteAddr);
        ok = ok && remoteAddrs.remove(new Integer(remotePort));

        return ok;
    }

    /**
     * Remove all stream targets from this session.
     */
    public void removeTargets() {
        remoteAddrs.removeAllElements();
        remotePorts.removeAllElements();
    }

    /**
     * Write a data buffer to all known remote targets.
     * 
     * The methods loops over the vector of all known targets, builds,
     * and sends datagram packet.
     * 
     * @param buffer The byte array containing the data
     * @param offset Offset into the byte array to get data from
     * @param length Length in bytes of the data to send.
     */
    public int write(byte[] buffer, int offset, int length) {
        for (int i = 0; i < remoteAddrs.size(); ++i) {
            InetAddress remoteAddr = remoteAddrs.elementAt(i);
            int remotePort = (remotePorts.elementAt(i)).intValue();

            try {
                socket.send(new DatagramPacket(buffer, offset, length, remoteAddr, remotePort));
            } catch (Exception e) {
                // TODO error handling
                return -1;
            }
        }
        // yes, we should return the pre-transformed packet length
        return length;
    }
    
    /**
     * Write a data buffer to one specific remote target.
     * 
     * The methods builds and sends datagram packet to a specified target.
     * This is mostly used to send RTCP packet to a dedicated target, for
     * example when removing a target. 
     * 
     * @param buffer The byte array containing the data
     * @param offset Offset into the byte array to get data from
     * @param length Length in bytes of the data to send
     * @param remoteAddr The remote IP address of the target
     * @param remotePort The port number of the target
     */
    public int writeToRemote(byte[] buffer, int offset, int length,
            InetAddress remoteAddr, int remotePort) {

        try {
            socket.send(new DatagramPacket(buffer, offset, length, remoteAddr,
                    remotePort));
        } catch (Exception e) {
            // TODO error handling
            return -1;
        }
        // yes, we should return the pre-transformed packet length
        return length;
    }

}
