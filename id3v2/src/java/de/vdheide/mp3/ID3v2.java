// ID3v2.java
//
// $Id: ID3v2.java,v 1.4 2006/11/07 21:07:19 spingel Exp $
//
// de.vdheide.mp3: Access MP3 properties, ID3 and ID3v2 tags
// Copyright (C) 1999 Jens Vonderheide <jens@vdheide.de>
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Library General Public
// License as published by the Free Software Foundation; either
// version 2 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Library General Public License for more details.
//
// You should have received a copy of the GNU Library General Public
// License along with this library; if not, write to the
// Free Software Foundation, Inc., 59 Temple Place - Suite 330,
// Boston, MA  02111-1307, USA.

package de.vdheide.mp3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Instances of this class contain an ID3v2 tag
 * <p>
 * There are two ways of detecting the size of padding used:
 * <ol>
 * <li>The "Size of padding" field in the extended header
 * <li>Detecting all frames and substracting the tag's actual length from its'
 * length in the header.
 * </ol>
 * Method a) is used in preference, so if a wrong padding size is stated in the
 * extended header, all bad things may happen.
 * <ul>
 * <li>Although the ID3v2 informal standard does not state it, this class will
 * only detect an ID3v2 tag if is starts at the first byte of a file.
 * <li>There is no direct access to the header and extended header. Both are
 * read, created and written internally.
 * </ul>
 */
public class ID3v2 {

	private File file;

	private ID3v2Header header;

	private ID3v2ExtendedHeader extended_header;

	private Vector frames;

	private boolean is_changed = false;

	private boolean use_padding = true;

	private boolean use_crc = true;

	private boolean use_unsynchronization = true;

	private boolean use_extended_header = true;

	/**
	 * Provides access to ID3v2 tag. When used with an InputStream, no writes
	 * are possible (<code>update</code> will fail with an
	 * <code>IOException</code>, so make sure you just read.
	 * 
	 * @param in Input stream to read from. Stream position must be set to
	 *        beginning of file (i.e. position of ID3v2 tag).
	 * @exception IOException If I/O errors occur ID3v2IllegalVersionException
	 *            If file contains an IDv2 tag of higher version than
	 *            <code>VERSION</code>.<code>REVISION</code>
	 * @exception ID3v2WrongCRCException If file contains CRC and this differs
	 *            from CRC calculated from the frames
	 * @exception ID3v2DecompressionException If a decompression error occured
	 *            while decompressing a compressed frame
	 */
	public ID3v2(InputStream in) throws IOException, ID3v2BadParsingException,
			ID3v2IllegalVersionException, ID3v2WrongCRCException,
			ID3v2BadHeaderException, ID3v2DecompressionException {
		try {
			this.file = null;
			// open file and read tag (if present)
			try {
				readHeader(in);
			} catch (NoID3v2HeaderException e) { // no tag
				header = null;
				extended_header = null;
				frames = null;
				return;
			}

			// tag present
			if (header.hasExtendedHeader()) {
				readExtendedHeader(in);
			} else {
				extended_header = null;
			}
			readFrames(in);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException ignored) {
			}
			is_changed = false;
		}
	}

	/**
	 * Provides access to <code>file</code>'s ID3v2 tag
	 * 
	 * @param file File to access
	 * @exception IOException If I/O errors occur
	 * @exception ID3v2IllegalVersionException If file contains an IDv2 tag of
	 *            higher version than <code>VERSION</code>.<code>REVISION</code>
	 * @exception ID3v2WrongCRCException If file contains CRC and this differs
	 *            from CRC calculated from the frames
	 * @exception ID3v2DecompressionException If a decompression error occured
	 *            while decompressing a compressed frame
	 */
	public ID3v2(File file) throws IOException, ID3v2IllegalVersionException,
			ID3v2WrongCRCException, ID3v2DecompressionException,
			ID3v2BadHeaderException, ID3v2BadParsingException {
		this(new FileInputStream(file));
		this.file = file;
	}

	/**
	 * ID3v2 version
	 */
	public final static byte VERSION = 3;

	/**
	 * ID3v2 revision
	 */
	public final static byte REVISION = 0;

	/**
	 * This method undoes the effect of the unsynchronization scheme by
	 * replacing $FF $00 by $FF
	 * 
	 * @param in Array of bytes to be "synchronized"
	 * @return Changed array or null if no "synchronization" was necessary
	 */
	public static byte[] synchronize(byte[] in) {
		final byte FF = (byte) 0xFF;
		final byte OO = (byte) 0x00;

		boolean did_synch = false;
		byte out[] = new byte[in.length];
		int outpos = 0; // next position to write to

		for (int i = 0; i < in.length; i++) {
			out[outpos++] = in[i];

			// skip next byte if this byte is 0xff and next byte is 0x00
			if (in[i] == FF && i < in.length - 1 && in[i + 1] == OO) {
				did_synch = true;
				i++;
			}
		}

		// make out smaller if necessary
		if (outpos != in.length) {
			// removed one or more bytes
			byte[] tmp = new byte[outpos];
			System.arraycopy(out, 0, tmp, 0, outpos);
			out = tmp;
		}

		if (did_synch == true) {
			return out;
		} else {
			return null;
		}
	}

	/**
	 * Unsynchronizes an array of bytes by replacing $FF 00 with $FF 00 00 and
	 * %11111111 111xxxxx with %11111111 00000000 111xxxxx.
	 * 
	 * @param in Array of bytes to be "unsynchronized"
	 * @return Changed array or null if no change was necessary
	 */
	public static byte[] unsynchronize(byte[] in) {
		byte[] out = new byte[in.length];
		int outpos = 0; // next position to write to
		boolean did_unsync = false;

		for (int i = 0; i < in.length; i++) {
			if (in[i] == -1) { // Check every byte in in if it is $FF
				// yes, perhaps we must unsynchronize
				if (((i + 1) < in.length)
						&& ((in[i + 1] & 0xff) >= 0xe0 || in[i + 1] == 0)) {
					// next byte is %111xxxxx or %00000000, we must
					// unsynchronize

					// first, enlarge out by one element
					byte[] tmp = new byte[out.length + 1];
					System.arraycopy(out, 0, tmp, 0, outpos);
					out = tmp;
					tmp = null;
					out[outpos++] = -1;
					out[outpos++] = 0;
					out[outpos++] = in[i + 1];

					// skip next byte, we have already written it
					i++;

					did_unsync = true;
				} else {// no unsynchronization necessary
					out[outpos++] = in[i];
				}
			} else {// no unsynchronization necessary
				out[outpos++] = in[i];
			}
		}

		if (did_unsync) // we did some unsynchronization
			return out;
		// OK. return null then
		return null;
	}

	/**
	 * Enables or disables use of padding (enabled by default)
	 * 
	 * @param use_padding True if padding should be used
	 */
	public void setUsePadding(boolean use_padding) {
		if (this.use_padding != use_padding) {
			is_changed = true;
			this.use_padding = use_padding;
		}
	}

	/**
	 * @return True if padding is used
	 */
	public boolean getUsePadding() {
		return use_padding;
	}

	/**
	 * Enables / disables use of CRC
	 * 
	 * @param use_crc True if CRC should be used
	 */
	public void setUseCRC(boolean use_crc) {
		if (this.use_crc != use_crc) {
			is_changed = true;
			this.use_crc = use_crc;
		}
	}

	/**
	 * @return True if CRC is used
	 */
	public boolean getUseCRC() {
		return use_crc;
	}

	/**
	 * Enables / disables use of unsynchronization
	 * 
	 * @param use_crc True if unsynchronization should be used
	 */
	public void setUseUnsynchronization(boolean use_unsynch) {
		if (this.use_unsynchronization != use_unsynch) {
			is_changed = true;
			this.use_unsynchronization = use_unsynch;
		}
	}

	public void setUseExtendedHeader(boolean use_extended_header) {
		if (this.use_extended_header != use_extended_header) {
			is_changed = true;
			this.use_extended_header = use_extended_header;
		}
	}

	/**
	 * @return True if unsynchronization should be used
	 */
	public boolean getUseUnsynchronization() {
		return use_unsynchronization;
	}

	/**
	 * Test if file already has an ID3v2 tag
	 * 
	 * @return true if file has IDv2 tag
	 */
	public boolean hasTag() {
		if (header == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Get all frames
	 * 
	 * @return <code>Vector</code> of all frames
	 * @exception NoID3v2TagException If file does not contain ID3v2 tag
	 */
	public Vector getFrames() throws NoID3v2TagException {
		if (frames == null) {
			throw new NoID3v2TagException();
		}

		return frames;
	}

	/**
	 * Return all frame with ID <code>id</code>
	 * 
	 * @param id Frame ID
	 * @return Requested frames
	 * @exception NoID3v2TagException If file does not contain ID3v2Tag
	 * @exception ID3v2NoSuchFrameException If file does not contain requested
	 *            ID3v2 frame
	 */
	public Vector getFrame(String id) throws NoID3v2TagException,
			ID3v2NoSuchFrameException {
		if (frames == null) {
			throw new NoID3v2TagException();
		}

		Vector res = new Vector();
		ID3v2Frame tmp;
		for (Enumeration e = frames.elements(); e.hasMoreElements();) {
			tmp = (ID3v2Frame) e.nextElement();
			if (tmp.getID().equals(id)) {
				res.addElement(tmp);
			}
		}

		if (res.size() == 0) {
			// no frame found
			throw new ID3v2NoSuchFrameException();
		} else {
			return res;
		}
	}

	/**
	 * Add a frame
	 * 
	 * @param frame Frame to add
	 */
	public void addFrame(ID3v2Frame frame) {
		if (frames == null) {
			frames = new Vector();
		}

		frames.addElement(frame);
		is_changed = true;
	}

	/**
	 * Remove a frame.
	 * 
	 * @param frame Frame to remove
	 * @exception NoID3v2TagException If file does not contain ID3v2Tag
	 * @exception ID3v2NoSuchFrameException If file does not contain requested
	 *            ID3v2 frame
	 */
	public void removeFrame(ID3v2Frame frame) throws NoID3v2TagException,
			ID3v2NoSuchFrameException {
		if (frames == null) {
			throw new NoID3v2TagException();
		}

		if (frames.removeElement(frame) == false) {
			throw new ID3v2NoSuchFrameException();
		}
		is_changed = true;
	}

	/**
	 * Remove all frames with a given id.
	 * 
	 * @param id ID of frames to remove
	 * @exception NoID3v2TagException If file does not contain ID3v2Tag
	 * @exception ID3v2NoSuchFrameException If file does not contain requested
	 *            ID3v2 frame
	 */
	public void removeFrame(String id) throws NoID3v2TagException,
			ID3v2NoSuchFrameException {
		if (frames == null) {
			throw new NoID3v2TagException();
		}

		ID3v2Frame tmp;
		boolean found = false; // will be true if at least one frame was found
		for (Enumeration e = frames.elements(); e.hasMoreElements();) {
			tmp = (ID3v2Frame) e.nextElement();
			if (tmp.getID().equals(id)) {
				frames.removeElement(tmp);
				found = true;
			}
		}

		if (found == false) {
			throw new ID3v2NoSuchFrameException();
		}
		is_changed = true;
	}

	/**
	 * Remove a spefic frames with a given id. A number is given to identify the
	 * frame if more than one frame exists
	 * 
	 * @param id ID of frames to remove
	 * @param number Number of frame to remove (the first frame gets number 0)
	 * @exception NoID3v2TagException If file does not contain ID3v2Tag
	 * @exception ID3v2NoSuchFrameException If file does not contain requested
	 *            ID3v2 frame
	 */
	public void removeFrame(String id, int number) throws NoID3v2TagException,
			ID3v2NoSuchFrameException {
		if (frames == null) {
			throw new NoID3v2TagException();
		}

		ID3v2Frame tmp;
		int count = 0; // Number of frames with id found so far
		boolean removed = false; // will be true if at least frame was
		// removed
		for (Enumeration e = frames.elements(); e.hasMoreElements();) {
			tmp = (ID3v2Frame) e.nextElement();
			if (tmp.getID().equals(id)) {
				if (count == number) {
					frames.removeElement(tmp);
					removed = true;
				} else {
					count++;
				}
			}
		}

		if (removed == false) {
			throw new ID3v2NoSuchFrameException();
		}
		is_changed = true;
	}

	/**
	 * Remove all frames
	 */
	public void removeFrames() {
		if (frames != null) {
			frames = new Vector();
		}
	}

	public void update() throws IOException {
		if (!is_changed)
			return;

		if (file == null) {
			// object was created from an InputStream
			// => update not possible
			throw new IOException("Cannot update InputStream");
		}

		boolean usesUnsynchronization = false;
		byte[] newFramesBytes = convertFramesToArrayOfBytes();
		// create new extended header (padding_size is set later if necessary)
		int crc = 0;
		if (use_crc) { // find the value of crc
			java.util.zip.CRC32 crcCalculator = new java.util.zip.CRC32();
			crcCalculator.update(newFramesBytes);
			crc = (int) crcCalculator.getValue();
		}
		// get the new Extension Header
		ID3v2ExtendedHeader newExtHeader = null;
		byte[] newExtHeaderBytes = new byte[0];
		if (use_extended_header) {
			newExtHeader = new ID3v2ExtendedHeader(use_crc, crc, 0);
			newExtHeaderBytes = newExtHeader.getBytes();
		}
		if (use_unsynchronization) {
			// unsynch and set correct vals for bFrames and bExtHeader
			// 1. first the header
			byte[] unsynchExtHeader = unsynchronize(newExtHeaderBytes);
			if (unsynchExtHeader != null) { // we have a value
				usesUnsynchronization = true;
				newExtHeaderBytes = unsynchExtHeader;
			}
			// 2. Then the frames
			byte[] unsynchFrames = unsynchronize(newFramesBytes);
			if (unsynchFrames != null) {
				usesUnsynchronization = true;
				newFramesBytes = unsynchFrames;
			}
		}
		// get the new header length
		int newHeaderLen = newFramesBytes.length + newExtHeaderBytes.length;
		// create the new header with the calculated new length
		ID3v2Header newHeader = new ID3v2Header(VERSION, REVISION,
				usesUnsynchronization, use_extended_header, false, newHeaderLen);

		int oldHeaderLen = 0;
		if (header != null) {
			oldHeaderLen = header.getTagSize() + 10;
		}
		
		// start writing stuff to an output stream so make the file.
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// write the header
		baos.write(newHeader.getBytes());
		baos.write(newExtHeaderBytes);
		baos.write(newFramesBytes);
		
		// extend size of new header to size of old header
		if (use_padding && oldHeaderLen > newHeaderLen) {
			long padding = oldHeaderLen - newHeaderLen;
			for (long i = 0; i < padding; i++) {
				baos.write(0);
			}
		}

		long contentLength = file.length() - oldHeaderLen;
		byte[] finalNewHeader = baos.toByteArray();
		
		updateInternal(oldHeaderLen, contentLength, finalNewHeader);
		
		header = newHeader;
		extended_header = newExtHeader;
		is_changed = false;
	}

	/**
	 * OK. now we need to start writing the header to the file without ruining
	 * the contents of the file.
	 * <p>
	 * There are three cases here:
	 * <ol>
	 * <li>newHeaderContent.length < oldHeaderLen -- we need to shift the content over
	 * <li>newHeaderContent.length == oldHeaderLen -- we simply overwrite the old header
	 * <li>newHeaderContent.length > oldHeaderLen -- we need to shift outwards, be very
	 * careful to not kill the content in this case
	 * </ol>
	 */
	private void updateInternal(int oldHeaderLen, long contentLen,
			byte[] newHeaderContent) throws FileNotFoundException, IOException {
		int minBuffSize = Math.abs(newHeaderContent.length - oldHeaderLen);
		int buffSize = Math.max(2048, minBuffSize);// pick the min or good size
		byte[] buf = new byte[buffSize];
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(file, "rw");
			long amountToRead = contentLen;
		
			// read the first chunk from the beginning of content
			raf.seek(oldHeaderLen == 0 ? 0 : oldHeaderLen - 1);
			int read = raf.read(buf, 0, buffSize);
			amountToRead -= read;
			
			// continue reading content at this point later
			long readPointer = raf.getFilePointer();
			
			// write new header to beginning of file
			raf.seek(0); 
			raf.write(newHeaderContent);
			
			// continue writing at this point
			long writePointer = raf.getFilePointer();

			while (amountToRead > 0) {
				byte[] buf2 = new byte[buffSize]; // create the new buffer
				raf.seek(readPointer);
				// read the bytes
				read = raf.read(buf2, 0, Math.min(buffSize, (int) amountToRead));
				if (read == -1)
					break;
				readPointer = raf.getFilePointer();
				amountToRead -= read;

				raf.seek(writePointer);
				raf.write(buf);// write the older buffer
				writePointer = raf.getFilePointer();
				buf = buf2; // make the new buffer the old one for next iter
			}
			
			// we still have one more buffer load to write
			raf.seek(writePointer);
			raf.write(buf);
		} finally {
			try {
				if (raf != null)
					raf.close();
			} catch (IOException ignored) {
			}
		}
	}

	/**
	 * Read ID3v2 header from file <code>in</code>
	 */
	private void readHeader(InputStream in) throws NoID3v2HeaderException,
			ID3v2IllegalVersionException, IOException {
		header = new ID3v2Header(in);
	}

	/**
	 * Read extended ID3v2 header from input stream <tt>in</tt>
	 * 
	 * @param in Input stream to read from
	 */
	private void readExtendedHeader(InputStream in) throws IOException {
		// in file pointer must be at correct position (header has just been
		// read)
		extended_header = new ID3v2ExtendedHeader(in);
	}

	/**
	 * Read ID3v2 frames from stream <tt>in</tt> Stream position must be set
	 * to beginning of frames
	 * 
	 * @param in Stream to read from
	 * 
	 */
	private void readFrames(InputStream in) throws IOException,
			ID3v2BadParsingException, ID3v2BadHeaderException,
			ID3v2WrongCRCException, ID3v2DecompressionException {
		// steps to read frames:

		// 1) Read all frames as bytes (don't includepadding if size of padding
		// is
		// known, i.e. ext. header exists)
		// 2) If CRC is present, make CRC check on frames
		// 3) Convert bytes to ID3v2Frames

		// // read all frames as bytes
		// calculate number of bytes to be read
		int bytes_to_read;
		if (extended_header != null) { // ext. header exists
			int crc = extended_header.hasCRC() ? 0 : 4;
			bytes_to_read = header.getTagSize()
					- (extended_header.getSize() + crc)
					- extended_header.getPaddingSize();
		} else {// no ext. header, include padding
			bytes_to_read = header.getTagSize();
		}

		// read bytes
		byte[] unsynch_frames_as_byte = null;
		try {
			unsynch_frames_as_byte = new byte[bytes_to_read];
		} catch (NegativeArraySizeException nax) {
			// The tag size specified by the header is wrong, can't really go
			// on.
			throw new ID3v2BadHeaderException();
		} catch (OutOfMemoryError ome) {
			throw new ID3v2BadParsingException(ome);
		}
		fillBuffer(unsynch_frames_as_byte, in);

		byte[] frames_as_byte = null;
		if (header.getUnsynchronization()) {
			// undo effects of unsynchronization
			frames_as_byte = synchronize(unsynch_frames_as_byte);
			if (frames_as_byte == null) {
				frames_as_byte = unsynch_frames_as_byte;
			}
		} else {
			frames_as_byte = unsynch_frames_as_byte;
		}

		// // CRC check
		if (extended_header != null && extended_header.hasCRC() == true) {
			// make CRC check
			// calculate crc of read frames (because extended header exists,
			// they contain no padding)
			java.util.zip.CRC32 crc_calculator = new java.util.zip.CRC32();
			crc_calculator.update(frames_as_byte);
			int crc = (int) crc_calculator.getValue();

			if ((int) crc != (int) extended_header.getCRC()) {
				// crc mismatch
				// throw new ID3v2WrongCRCException();
			}
		}

		// // Convert bytes to ID3v2Frames
		frames = new Vector();

		ByteArrayInputStream bis = new ByteArrayInputStream(frames_as_byte);
		// read frames as long as there are bytes and we are not reading from
		// padding (indicated by invalid frame id)
		ID3v2Frame frame = null;
		boolean cont = true;
		while ((bis.available() > 0) && (cont == true)) {
			try {
				frame = new ID3v2Frame(bis);
			} catch (ID3v2BadFrameException e) {
				continue;
			}
			if (frame.getID() == ID3v2Frame.ID_INVALID) { // reached end of
				// frames
				cont = false;
			} else {
				frames.addElement(frame);
			}
		}
	}

	/**
	 * Convert all frames to an array of bytes
	 */
	private byte[] convertFramesToArrayOfBytes() {
		ID3v2Frame tmp = null;

		ByteArrayOutputStream out = new ByteArrayOutputStream(500);

		for (Enumeration e = frames.elements(); e.hasMoreElements();) {
			tmp = (ID3v2Frame) e.nextElement();
			// System.out.println("Sumeet: frame:"+tmp.getID());
			// System.out.println("Sumeet length:"+tmp.getLength());
			// System.out.println("Sumeet: value:"+new
			// String(tmp.getContent()));
			byte frame_in_bytes[] = tmp.getBytes();
			out.write(frame_in_bytes, 0, frame_in_bytes.length);
		}

		return out.toByteArray();
	}

	public static void fillBuffer(byte[] buffer, InputStream in)
			throws IOException {
		int offset = 0;
		while (offset < buffer.length) {
			int read = in.read(buffer, offset, buffer.length - offset);
			if (read == -1) {
				throw new IOException("eof");
			} else if (read == 0) {
				throw new IOException("can't read");
			} else {
				offset += read;
			}
		}
	}

	/**
	 * Skips however much data was padded for the given length.
	 */
	public static void skipData(int length, InputStream in) throws IOException {
		long skipped = 0;
		while (skipped < length) {
			long current = in.skip(length - skipped);
			if (current == -1)
				throw new IOException("eof");
			else if (current == 0)
				throw new IOException("can't read");
			else
				skipped += current;
		}
	}

}
