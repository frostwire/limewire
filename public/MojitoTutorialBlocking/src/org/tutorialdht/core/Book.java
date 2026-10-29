package org.tutorialdht.core;

/**
 * Stores a book's information, along with a rating and a comment section for 
 * a particular book.
 */
class Book {
	
	final String title;
	final String author;
	final String date;
	final String rating;
	final String comment;
		
	public Book(String title, String author, String when, String rating, String comment) {
		this.title = title;
		this.author = author;
		this.date = when;
		this.rating = rating;
		this.comment = comment;
	}
	public Book(String info) {
		String [] values = info.split(",");
		if(values.length != 5) {
			throw new IllegalArgumentException("Fields in book are not filled properly");
		}
		title = values[0];
		author = values[1];
		date = values[2];
		rating = values[3];
		comment = values[4];
	}
	
	public String getTitle() {
		return title;
	}
	public String getAuthor() {
		return author;
	}
	public String getWhen() {
		return date;
	}
	public String getRating() {
		return rating;
	}
	public String getComment() {
		return comment;
	}
	public String toString() {
		return title + "," + author + "," + date + "," + rating + "," + comment;
	}
}
