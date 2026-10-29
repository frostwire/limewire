/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/AutoCloseInputStream.java,v 1.1.1.1 2003/10/30 20:32:58 sberlin Exp $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/10/30 20:32:58 $
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
 */

package org.apache.commons.httpclient;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Closes an underlying stream as soon as the end of the stream is reached, and
 * notifies a client when it has done so.
 *
 * @author Ortwin Glück
 * @author Eric Johnson
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 *
 * @since 2.0
 */

class AutoCloseInputStream extends FilterInputStream {

    /** 
     * True if this stream is open.  Assume that the underlying stream 
     * is open until we get an EOF indication.
     */
    private boolean streamOpen = true;

    /** True if the stream closed itself. */
    private boolean selfClosed = false;

    /** 
     * The watcher is notified when the contents of the stream have
     * been  exhausted
     */ 
    private ResponseConsumedWatcher watcher = null;

    /**
     * Create a new auto closing stream for the provided connection
     *
     * @param in the input stream to read from
     * @param watcher   To be notified when the contents of the stream have been
     *  consumed.
     */
    public AutoCloseInputStream(
            final InputStream in, final ResponseConsumedWatcher watcher) {
        super(in);
        this.watcher = watcher;
    }

    /**
     * Reads the next byte of data from the input stream.
     *
     * @throws IOException when there is an error reading
     * @return the character read, or -1 for EOF
     */
    public int read() throws IOException {
        int l = -1;

        if (isReadAllowed()) {
            // underlying stream not closed, go ahead and read.
            l = super.read();
            checkClose(l);
        }

        return l;
    }

    /**
     * Reads up to <code>len</code> bytes of data from the stream.
     *
     * @param b a <code>byte</code> array to read data into
     * @param off an offset within the array to store data
     * @param len the maximum number of bytes to read
     * @return the number of bytes read or -1 for EOF
     * @throws IOException if there are errors reading
     */
    public int read(byte[] b, int off, int len) throws IOException {
        int l = -1;

        if (isReadAllowed()) {
            l = super.read(b,  off,  len);
            checkClose(l);
        }

        return l;
    }

    /**
     * Reads some number of bytes from the input stream and stores them into the
     * buffer array b.
     *
     * @param b a <code>byte</code> array to read data into
     * @return the number of bytes read or -1 for EOF
     * @throws IOException if there are errors reading
     */
    public int read(byte[] b) throws IOException {
        int l = -1;

        if (isReadAllowed()) {
            l = super.read(b);
            checkClose(l);
        }
        return l;
    }

    /**
     * Close the stream, and also close the underlying stream if it is not
     * already closed.
     * @throws IOException If an IO problem occurs.
     */
    public void close() throws IOException {
        if (!selfClosed) {
            selfClosed = true;
            notifyWatcher();
        }
    }

    /**
     * Close the underlying stream should the end of the stream arrive.
     *
     * @param readResult    The result of the read operation to check.
     * @throws IOException If an IO problem occurs.
     */
    private void checkClose(int readResult) throws IOException {
        if (readResult == -1) {
            notifyWatcher();
        }
    }

    /**
     * See whether a read of the underlying stream should be allowed, and if
     * not, check to see whether our stream has already been closed!
     *
     * @return <code>true</code> if it is still OK to read from the stream.
     * @throws IOException If an IO problem occurs.
     */
    private boolean isReadAllowed() throws IOException {
        if (!streamOpen && selfClosed) {
            throw new IOException("Attempted read on closed stream.");
        }
        return streamOpen;
    }

    /**
     * Notify the watcher that the contents have been consumed.
     * @throws IOException If an IO problem occurs.
     */
    private void notifyWatcher() throws IOException {
        if (streamOpen) {
            super.close();
            streamOpen = false;

            if (watcher != null) {
                watcher.responseConsumed();
            }
        }
    }
}

