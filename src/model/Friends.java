package model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "friends")
public class Friends {
	private URL next;
	private ArrayList<Link> friends;

	public Friends() {

	}

	public Friends(String next) {
		try {
			this.next = new URL(next);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		this.friends = new ArrayList<>();
	}

	@XmlAttribute
	public URL getNext() {
		return next;
	}

	public void setNext(URL next) {
		this.next = next;
	}

	@XmlElement(name = "user")
	public ArrayList<Link> getUsers() {
		return friends;
	}

	public void setUsers(ArrayList<Link> users) {
		this.friends = users;
	}

}
