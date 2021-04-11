package model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "app")
public class App {
	private User user;
	private Reading lastReading;
	private int numFriends;
	private ArrayList<Link> lastFriendsReadings;

	public App() {

	}

	public App(User user, Reading lastBook, int numFriends) {
		this.user = user;
		this.lastReading = lastBook;
		this.numFriends = numFriends;
		this.lastFriendsReadings = new ArrayList<>();
	}
	
	@XmlElement(name = "user")
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	@XmlElement(name = "last_reading")
	public Reading getlastReading() {
		return lastReading;
	}

	public void setlastReading(Reading lastBook) {
		this.lastReading = lastBook;
	}
	
	@XmlElement(name = "friends_count")
	public int getNumFriends() {
		return numFriends;
	}

	public void setNumFriends(int numFriends) {
		this.numFriends = numFriends;
	}
	
	@XmlElementWrapper(name="friends_readings")
	@XmlElement(name = "reading")
	public ArrayList<Link> getlastFriendsReadings() {
		return lastFriendsReadings;
	}

	public void setlastFriendsReadings(ArrayList<Link> lastFriendsBooks) {
		this.lastFriendsReadings = lastFriendsBooks;
	}

}
