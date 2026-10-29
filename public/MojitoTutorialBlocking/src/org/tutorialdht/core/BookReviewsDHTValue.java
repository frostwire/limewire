package org.tutorialdht.core;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.limewire.mojito.db.DHTValue;
import org.limewire.mojito.db.DHTValueType;
import org.limewire.mojito.routing.Version;

/**
 * Custom <code>DHTValue</code> to put book review data into the DHT. 
 */
class BookReviewsDHTValue implements DHTValue {

    /**
     * DHTValueType for Book. When you do a publish or retrieval, setting the type
     * to BOOK only shows values with this type.
     */
    public static final DHTValueType BOOK = DHTValueType.valueOf("Book Reviews", "BOOK");

	private Book book;
	private static final long serialVersionUID = 1L;

	Version VERSION = Version.valueOf(0);
	public BookReviewsDHTValue(Book book) {
		this.book = book;
	}
	@Override
	public DHTValueType getValueType() {
		return BOOK;
	}
	@Override
	public Version getVersion() {
		return VERSION;
	}
	@Override
	public byte[] getValue() {
		return book.toString().getBytes(Charset.forName("UTF-8"));
	}
	@Override
	public void write(OutputStream out) throws IOException {
		out.write(getValue());
	}
	@Override
	public int size() {
		return getValue().length;
	}
	@Override
	public String toString() {
		return book.toString();
	}
}
