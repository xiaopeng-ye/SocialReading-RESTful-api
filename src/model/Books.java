package model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "books")
public class Books {
	private ArrayList<Link> books;

	public Books() {
		books = new ArrayList<>();
	}

	@XmlElement(name = "book")
	public ArrayList<Link> getBooks() {
		return books;
	}

	public void setBooks(ArrayList<Link> books) {
		this.books = books;
	}
}
