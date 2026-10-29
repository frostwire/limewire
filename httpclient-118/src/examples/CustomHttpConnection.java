/*
 * $Header: /cvs/httpclient-118/src/examples/CustomHttpConnection.java,v 1.1.1.1 2003/10/30 20:32:58 sberlin Exp $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/10/30 20:32:58 $
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "HttpClient", and "Apache Software
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
 */

import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.ConnectMethod;

/**
 * This example demonstrates how to establishing connection to an HTTP server
 * when higher level of control is desired
 *
 * @author Armando Anton
 * @author Oleg Kalnichevski
 */

public class CustomHttpConnection
{
    public static void main(String[] args) throws Exception
    {
        if (args.length != 1)
        {
            System.err.println("Usage: java CustomHttpConnection <url>");
            System.err.println("<url> The url of a webpage");
            System.exit(1);
        }

        URI uri = new URI(args[0].toCharArray()); // i like this constructor :)

        String schema = uri.getScheme();
        if ((schema == null) || (schema.equals("")))
        {
            schema = "http";
        }
        Protocol protocol = Protocol.getProtocol(schema);

        HttpState state = new HttpState();

        HttpMethod method = new GetMethod(uri.toString());
        String host = uri.getHost();
        int port = uri.getPort();

        HttpConnection connection = new HttpConnection(host, port, protocol);

        connection.setProxyHost(System.getProperty("http.proxyHost"));
        connection.setProxyPort( Integer.parseInt(System.getProperty("http.proxyPort","80")));

        if (System.getProperty("http.proxyUserName") != null)
        {
            state.setProxyCredentials(null, null,
              new UsernamePasswordCredentials(
                System.getProperty("http.proxyUserName"),
                System.getProperty("http.proxyPassword")));
        }

        if (connection.isProxied() && connection.isSecure()) {
            method = new ConnectMethod(method);
        }
        method.execute(state, connection);

        if (method.getStatusCode() == HttpStatus.SC_OK) {
            System.out.println(method.getResponseBodyAsString());
        } else {
            System.out.println("Unexpected failure: " + method.getStatusLine().toString());
        }
        method.releaseConnection();
    }
}
