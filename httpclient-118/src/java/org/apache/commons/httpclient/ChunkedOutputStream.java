/*
 * $Header: /cvs/httpclient-118/src/java/org/apache/commons/httpclient/ChunkedOutputStream.java,v 1.1.1.1 2003/10/30 20:32:58 sberlin Exp $
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

import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Wrapper supporting the chunked transfer encoding.
 * </p>
 *
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @author Sean C. Sullivan
 * @author <a href="mailto:dion@apache.org">dIon Gillard</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @version $Revision: 1.1.1.1 $ $Date: 2003/10/30 20:32:58 $
 *
 * @see ChunkedInputStream
 * @since 2.0
 *
 */
public class ChunkedOutputStream extends OutputStream {

    // ------------------------------------------------------- Static Variables

    /** <tt>"\r\n"</tt>, as bytes. */
    private static final byte CRLF[] = new byte[] {(byte) 13, (byte) 10};

    /** End chunk */
    private static final byte ENDCHUNK[] = CRLF;

    /** 0 */
    private static final byte ZERO[] = new byte[] {(byte) '0'};

    /** 1 */
    private static final byte ONE[] = new byte[] {(byte) '1'};

    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(ChunkedOutputStream.class);

    // ----------------------------------------------------- Instance Variables

    /** Has this stream been closed? */
    private boolean closed = false;

    /** The underlying output stream to which we will write data */
    private OutputStream stream = null;

    // ----------------------------------------------------------- Constructors

    /**
     * Construct an output stream wrapping the given stream.
     * The stream will not use chunking.
     *
     * @param stream wrapped output stream. Must be non-null.
     */
    public ChunkedOutputStream(OutputStream stream) {
        if (stream == null) {
            throw new NullPointerException("stream parameter is null");
        }
        this.stream = stream;
    }


    // --------------------------------------------------------- Public Methods

    /**
     * Writes a <code>String</code> to the client, without a carriage return
     * line feed (CRLF) character at the end. The platform default encoding is
     * used!
     *
     * @param s the <code>String</code> to send to the client. Must be non-null.
     * @throws IOException if an input or output exception occurred
     */
    public void print(String s) throws IOException {
        LOG.trace("enter ChunckedOutputStream.print(String)");
        if (s == null) {
            s = "null";
        }
        write(HttpConstants.getBytes(s));
    }

    /**
     * Writes a carriage return-line feed (CRLF) to the client.
     *
     * @throws IOException   if an input or output exception occurred
     */
    public void println() throws IOException {
        print("\r\n");
    }

    /**
     * Writes a <code>String</code> to the client,
     * followed by a carriage return-line feed (CRLF).
     *
     * @param s         the </code>String</code> to write to the client
     * @exception IOException   if an input or output exception occurred
     */
    public void println(String s) throws IOException {
        print(s);
        println();
    }

    // -------------------------------------------- OutputStream Methods

    /**
     * Write the specified byte to our output stream.
     *
     * @param b The byte to be written
     * @throws IOException if an input/output error occurs
     * @throws IllegalStateException if stream already closed
     */
    public void write (int b) throws IOException, IllegalStateException {
        if (closed) {
            throw new IllegalStateException("Output stream already closed"); 
        }
        //FIXME: If using chunking, the chunks are ONE byte long!
        stream.write(ONE, 0, ONE.length);
        stream.write(CRLF, 0, CRLF.length);
        stream.write(b);
        stream.write(ENDCHUNK, 0, ENDCHUNK.length);
        LOG.debug("Writing chunk (length: 1)");
    }

    /**
     * Write the specified byte array.
     *
     * @param b the byte array to write out
     * @param off the offset within <code>b</code> to start writing from
     * @param len the length of data within <code>b</code> to write
     * @throws IOException when errors occur writing output
     */
    public void write (byte[] b, int off, int len) throws IOException {
        LOG.trace("enter ChunckedOutputStream.write(byte[], int, int)");

        if (closed) {
            throw new IllegalStateException("Output stream already closed"); 
        }
        byte chunkHeader[] = HttpConstants.getBytes (
            Integer.toHexString(len) + "\r\n");
        stream.write(chunkHeader, 0, chunkHeader.length);
        stream.write(b, off, len);
        stream.write(ENDCHUNK, 0, ENDCHUNK.length);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Writing chunk (length: " + len + ")");
        }
    }

    /**
     * Close this output stream, causing any buffered data to be flushed and
     * any further output data to throw an IOException. The underlying stream
     * is not closed!
     *
     * @throws IOException if an error occurs closing the stream
     */
    public void writeClosingChunk() throws IOException {
        LOG.trace("enter ChunkedOutputStream.writeClosingChunk()");

        if (!closed) {
            try {
                // Write the final chunk.
                stream.write(ZERO, 0, ZERO.length);
                stream.write(CRLF, 0, CRLF.length);
                stream.write(ENDCHUNK, 0, ENDCHUNK.length);
                LOG.debug("Writing closing chunk");
            } catch (IOException e) {
                LOG.debug("Unexpected exception caught when closing "
                    + "output stream", e);
                throw e;
            } finally {
                // regardless of what happens, mark the stream as closed.
                // if there are errors closing it, there's not much we can do
                // about it
                closed = true;
            }
        }
    }

    /**
     * Flushes the underlying stream.
     * @throws IOException If an IO problem occurs.
     */
    public void flush() throws IOException {
        stream.flush();
    }

    /**
     * Close this output stream, causing any buffered data to be flushed and
     * any further output data to throw an IOException. The underlying stream
     * is not closed!
     *
     * @throws IOException if an error occurs closing the stream
     */
    public void close() throws IOException {
        LOG.trace("enter ChunkedOutputStream.close()");
        writeClosingChunk();
        super.close();
    }


}
