/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/protocol/SOCKSSocketFactory.java,v 1.2 2005/07/19 16:21:33 zlatinb Exp $
 * $Revision: 1.2 $
 * $Date: 2005/07/19 16:21:33 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 * @author Sumeet Thadani
 */

package org.apache.commons.httpclient.protocol;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class SOCKSSocketFactory implements ProtocolSocketFactory {
    
    private int _version;
    private String _host;
    private int _port;
    private String _user;
    private String _pass;

    private String _destHost;
    private int _destPort;


    private SOCKSSocketFactory() {} //so nobody uses this by accident

    /**
     * @param user null if the sever does not need authentication
     * @param pass null if the server does not need authentication
     */
    public SOCKSSocketFactory(int version ,String proxyHost, int proxyPort, 
                              String user, String pass) {
        if(version  < 4 || version > 5)
            throw new IllegalArgumentException("unsupported version of SOCKS");
        if(proxyHost == null || "".equals(proxyHost))
            throw new IllegalArgumentException("Proxy host is null");
		if((proxyPort & 0xFFFF0000) != 0 || (proxyPort ==0) )
            throw new IllegalArgumentException("Invalid Proxy Port");

        _version = version;
        _host = proxyHost;
        _port = proxyPort;
        _user = user;
        _pass = pass;
    }
    
    public Socket createSocket(String host, int port, InetAddress clientHost, 
                      int clientPort) throws IOException, UnknownHostException {
        this._destHost = host;
        this._destPort = port;
        //make a connection with the proxy server
        Socket sock = new Socket(_host, _port, clientHost, clientPort);
        initializeConnection(sock);
        return sock;
    }
    
    public Socket createSocket(String host, int port) throws IOException, 
                                                        UnknownHostException {
        this._destHost = host;
        this._destPort = port;
        Socket sock = new Socket(_host, _port);
        initializeConnection(sock);
        return sock;
    }
    
    private void initializeConnection(Socket socket) throws IOException, 
                                                          UnknownHostException {
        if(_version == 4)
            doSOCKSv4Handshake(socket);
        else if(_version == 5) 
            doSOCKSv5Handshake(socket);
        else 
            throw new IllegalArgumentException("bad SOCKS version");
    }

    private void doSOCKSv4Handshake(Socket socket) throws IOException, 
                                                          UnknownHostException {
		byte[] hostBytes;
        hostBytes = InetAddress.getByName(_destHost).getAddress();

		byte[] portBytes = new byte[2];
		portBytes[0] = ((byte) (_destPort >> 8));
		portBytes[1] = ((byte) _destPort);

		OutputStream os = null;
		InputStream in = null;
        
        //TODO: we need to add a TIMEOUT to these sockets
		//proxySocket.setSoTimeout(timeout);
		os = socket.getOutputStream();
		in = socket.getInputStream();

		os.write(0x04); //version
		os.write(0x01); //connect command
		os.write(portBytes); //port to connect to
		os.write(hostBytes); //host to connect to
		//write user name if necessary
		if(_user!=null)
			os.write(_user.getBytes());
		os.write(0x00); //terminating 0
		os.flush();

		// read response
		// version should be 0 but some socks proxys answer 4
		int version = in.read();
		if (version != 0x00 && version != 0x04) {
			socket.close();
			throw new IOException("Invalid version from socks proxy: "+
                                                  version+ " expected 0 or 4");
		}

		// read the status, 0x5A is success
		int status = in.read();
		if (status != 0x5A) {
			socket.close();
			throw new IOException("Request rejected with status: " + status);
		}

		// the socks proxy will now send the connected port and host
		// we don't really check if it's the right one.
		byte[] connectedHostPort = new byte[2];
		byte[] connectedHostAddress = new byte[4];
		if (in.read(connectedHostPort) == -1 || 
                                         in.read(connectedHostAddress) == -1) {
			socket.close();
			throw new IOException("Connection failed");
		}
		socket.setSoTimeout(0);
        
    }
    
    private void doSOCKSv5Handshake(Socket socket) throws IOException, 
                                                       UnknownHostException {
		byte[] hostBytes;
        hostBytes = InetAddress.getByName(_destHost).getAddress();

		byte[] portBytes = new byte[2];
		portBytes[0] = ((byte) (_destPort >> 8));
		portBytes[1] = ((byte) _destPort);

		OutputStream os = null;
		InputStream in = null;
        //TODO: We should have a timout setting here. 
		//socket.setSoTimeout(timeout);
		os = socket.getOutputStream();
		in = socket.getInputStream();

		os.write(0x05); //version
		if (_user != null) {
			os.write(0x02); //the number of authentication methods we support
			os.write(0x00); //authentication method: no authentication
			os.write(0x02); //authentication method: username/password
		} else {
			os.write(0x01); //the number of authentication methods we support
			os.write(0x00); //authentication method: no authentication
		}
		os.flush();

		int version = in.read();
		if (version != 0x05) {
			socket.close();
			throw new IOException(
				"Invalid version from socks proxy: " + version + " expected 5");
		}

		int auth_method = in.read();
		if (auth_method == 0x00) {
			// no authentication
		} else if (auth_method == 0x02) {
			// username/password

			os.write(0x01); // version of authentication protocol
			os.write((byte) _user.length()); // length of username field
			os.write(_user.getBytes()); // username
			os.write((byte) _pass.length()); // length of password field
			os.write(_pass.getBytes()); // password
			os.flush();

			// read version for auth protocol from proxy, expects 1
			version = in.read();
			if (version != 0x01) {
				socket.close();
				throw new IOException("Invalid version for authentication: "+
                                      version + " expected 1");
			}

			// read status, 0 is success
			int status = in.read();
			if (status != 0x00) {
				socket.close();
				throw new IOException(
                                "Authentication failed with status: " + status);
			}
		}

		// request connection
		os.write(0x05); // version again
		os.write(0x01); // connect command, 
		// 0x02 would be bind, 0x03 UDP associate
		os.write(0x00); // reserved field, must be 0x00
		os.write(0x01); // address type: 0x01 is IPv4, 0x04 would be IPv6
		os.write(hostBytes); //host to connect to
		os.write(portBytes); //port to connect to
		os.flush();

		// read response
		// version should be 0x05
		version = in.read();
		if (version != 0x05) {
			socket.close();
			throw new IOException(
				"Invalid version from socks proxy: " + version + " expected 5");
		}
		// read the status, 0x00 is success
		int status = in.read();
		if (status != 0x00) {
			socket.close();
			throw new IOException("Request rejected with status: " + status);
		}

		// skip reserved byte;
		in.read();

		// read the address type in the reply and skip it.
		int addrType = in.read();
		int bytesToSkip = 0;
		if (addrType == 1) { // IPv4
			bytesToSkip = 4 + 2;
		} else if (addrType == 3) { // domain name
			bytesToSkip = in.read() + 2;
		} else if (addrType == 4) { // IPv6
			bytesToSkip = 16 + 2;
		}

		for (int i = 0; i < bytesToSkip; i++) {
			if (in.read() == -1) {
				throw new IOException("Connection failed");
			}
		}

		socket.setSoTimeout(0);
    }

    public Socket createSocket(String host, int port, int timeout) throws IOException, UnknownHostException {
        // TODO Auto-generated method stub
        return null;
    }


}
