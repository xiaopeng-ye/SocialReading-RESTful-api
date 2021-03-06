package model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "reading")
@XmlType(propOrder = { "book", "date", "qualification" })
public class Reading {
	private Book book;
	private String date;
	private byte qualification;

	public Reading() {

	}

	public Reading(Book book, String date, byte qualification) {
		super();
		this.book = book;
		this.date = date;
		this.qualification = qualification;
	}

	@XmlElement(name = "book")
	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public byte getQualification() {
		return qualification;
	}

	public void setQualification(byte qualification) {
		this.qualification = qualification;
	}

}
