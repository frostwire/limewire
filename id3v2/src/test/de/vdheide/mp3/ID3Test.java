package de.vdheide.mp3;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;

/**
 * Tests for class ID3 (ID3v1 and ID3v11 specific tests)
 */
public class ID3Test extends MP3TestCase {

	public void testGetTitleNoTag() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_none"));
		try {
			id3.getTitle();
			fail("Expected NoID3TagException not thrown");
		} catch (NoID3TagException e) {
		}
	}

	public void testGetTitleV1() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_v1reg"));
		assertEquals("Title", id3.getTitle());
	}

	public void testGetTitleV11() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_v11reg"));
		assertEquals("Title", id3.getTitle());
	}

	public void testGetTitleV1Space() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_v1spaces"));
		assertEquals("Title", id3.getTitle());
	}

	public void testGetArtistNoTag() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_none"));
		try {
			id3.getArtist();
			fail("Expected NoID3TagException not thrown");
		} catch (NoID3TagException e) {
		}
	}

	public void testGetArtistV1() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_v1reg"));
		assertEquals("Artist", id3.getArtist());
	}

	public void testGetArtistV11() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_v11reg"));
		assertEquals("Artist", id3.getArtist());
	}

	public void testGetArtistSpace() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_v1spaces"));
		assertEquals("Artist", id3.getArtist());
	}

	public void testGetAlbumNoTag() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_none"));
		try {
			id3.getAlbum();
			fail("Expected NoID3TagException not thrown");
		} catch (NoID3TagException e) {
		}
	}

	public void testGetAlbumV1() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_v1reg"));
		assertEquals("Album", id3.getAlbum());
	}

	public void testGetAlbumV11() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_v11reg"));
		assertEquals("Album", id3.getAlbum());
	}

	public void testGetAlbumV1Space() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_v1spaces"));
		assertEquals("Album", id3.getAlbum());
	}

	public void testGetYearNoTag() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_none"));
		try {
			id3.getYear();
			fail("Expected NoID3TagException not thrown");
		} catch (NoID3TagException e) {
		}
	}

	public void testGetYearV1() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_v1reg"));
		assertEquals("2003", id3.getYear());
	}

	public void testGetYearV11() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_v11reg"));
		assertEquals("2003", id3.getYear());
	}

	public void testGetYearV1Space() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_v1spaces"));
		assertEquals("2003", id3.getYear());
	}

	public void testGetGenreNoTag() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_none"));
		try {
			id3.getGenre();
			fail("Expected NoID3TagException not thrown");
		} catch (NoID3TagException e) {
		}
	}

	public void testGetGenreV1() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_v1reg"));
		assertEquals(7, id3.getGenre());
	}

	public void testGetGenreV11() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_v11reg"));
		assertEquals(7, id3.getGenre());
	}

	public void testGetGenreV1Space() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_v1spaces"));
		assertEquals(7, id3.getGenre());
	}

	public void testGetCommentNoTag() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_none"));
		try {
			id3.getComment();
			fail("Expected NoID3TagException not thrown");
		} catch (NoID3TagException e) {
		}
	}

	public void testGetCommentV1() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_v1reg"));
		assertEquals("Comment", id3.getComment());
	}

	public void testGetCommentV11() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_v11reg"));
		assertEquals("Comment", id3.getComment());
	}

	public void testGetCommentV1Space() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_v1spaces"));
		assertEquals("Comment", id3.getComment());
	}

	public void testGetTrackNoTag() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_none"));
		try {
			id3.getTrack();
			fail("Expected NoID3TagException not thrown");
		} catch (NoID3TagException e) {
		}
	}

	public void testGetTrackV1() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_v1reg"));
		assertEquals(0, id3.getTrack());
	}

	public void testGetTrackV11() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_v11reg"));
		assertEquals(4, id3.getTrack());
	}

	public void testGetTrackV1Space() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_v1spaces"));
		assertEquals(0, id3.getTrack());
	}

	public void testReadTag() {
		// readTag is implicitely tested by the testGet*
		// tests above
	}

	public void testSetTitle() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_none"));
		id3.setTitle("Title");
		assertEquals("Title", id3.getTitle());
	}

	public void testSetArtist() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_none"));
		id3.setArtist("Artist");
		assertEquals("Artist", id3.getArtist());
	}

	public void testSetAlbum() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_none"));
		id3.setAlbum("Album");
		assertEquals("Album", id3.getAlbum());
	}

	public void testSetYear() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_none"));
		id3.setYear("2004");
		assertEquals("2004", id3.getYear());
	}

	public void testSetComment() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_none"));
		id3.setComment("Comment");
		assertEquals("Comment", id3.getComment());
	}

	public void testSetTrack() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_none"));
		id3.setTrack(6);
		assertEquals(6, id3.getTrack());
	}

	public void testSetGenre() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_none"));
		id3.setGenre(11);
		assertEquals(11, id3.getGenre());
	}

	public void testWriteTagNoTag() throws Exception {
		ID3 id3 = new ID3((File) testFiles.get("short_none"));
		id3.setAlbum("Album");
		id3.setArtist("Artist");
		id3.setComment("Comment");
		id3.setGenre(7);
		id3.setTitle("Title");
		id3.setTrack(4);
		id3.setYear("2003");

		id3.writeTag();

		id3 = new ID3((File) testFiles.get("short_none"));
		assertEquals("Album", id3.getAlbum());
		assertEquals("Artist", id3.getArtist());
		assertEquals("Comment", id3.getComment());
		assertEquals(7, id3.getGenre());
		assertEquals("Title", id3.getTitle());
		assertEquals(4, id3.getTrack());
		assertEquals("2003", id3.getYear());

		// additional (redundant) check:
		// The contents of "short_none" must now be identical to the ones of
		// "short_v11reg"
		byte[] contN = new byte[992];
		FileInputStream fis = new FileInputStream((File) testFiles
				.get("short_none"));
		fis.read(contN);
		fis.close();
		byte[] contR = new byte[992];
		fis = new FileInputStream((File) testFiles.get("short_v11reg"));
		fis.read(contR);
		fis.close();
		MP3TestCase.assertEqualsByteArray(contR, contN);
	}

    public void testCheckForTag() throws Exception {
        assertFalse(checkForTag((File) testFiles.get("short_none")));
        assertTrue(checkForTag((File) testFiles.get("short_v1reg")));
        assertTrue(checkForTag((File) testFiles.get("short_v11reg")));
        assertTrue(checkForTag((File) testFiles.get("short_v1spaces")));
    }
    
    private boolean checkForTag(File file) throws Exception {
    	ID3 id3 = new ID3((File) file);
    	RandomAccessFile raf = new RandomAccessFile(file, "r");
    	return id3.checkForTag(raf);
    }

}