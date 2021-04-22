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
	private URL prev;
	private ArrayList<Link> readings;

	public Readings() {
		readings = new ArrayList<>();
	}

	@XmlAttribute(required = false)
	public URL getPrev() {
		return prev;
	}

	public void setPrev(String prev) {
		try {
			this.prev = new URL(prev);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@XmlAttribute(required = false)
	public URL getNext() {
		return next;
	}

	public void setNext(String next) {
		try {
			this.next = new URL(next);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@XmlElement(name = "reading")
	public ArrayList<Link> getReadings() {
		return readings;
	}

	public void setReadings(ArrayList<Link> books) {
		this.readings = books;
	}
}
