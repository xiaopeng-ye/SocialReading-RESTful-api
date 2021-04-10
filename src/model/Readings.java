package model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "readings")
public class Readings {
	private URL next;
	private ArrayList<Link> books;

	public Readings() {

	}

	public Readings(String next) {
		try {
			this.next = new URL(next);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		books = new ArrayList<>();
	}

	@XmlAttribute
	public URL getNext() {
		return next;
	}

	public void setNext(URL next) {
		this.next = next;
	}

	@XmlElement(name = "reading")
	public ArrayList<Link> getBooks() {
		return books;
	}

	public void setBooks(ArrayList<Link> books) {
		this.books = books;
	}
}
