package model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "app")
@XmlType(propOrder = {"user", "lastReading", "friendsCount", "lastFriendsReadings" })
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
	public Reading getLastReading() {
		return lastReading;
	}

	public void setLastReading(Reading lastBook) {
		this.lastReading = lastBook;
	}

	@XmlElement(name = "friends_count")
	public int getFriendsCount() {
		return numFriends;
	}

	public void setFriendsCount(int numFriends) {
		this.numFriends = numFriends;
	}

	@XmlElementWrapper(name = "last_friends_readings")
	@XmlElement(name = "reading")
	public ArrayList<Link> getLastFriendsReadings() {
		return lastFriendsReadings;
	}

	public void setLastFriendsReadings(ArrayList<Link> lastFriendsBooks) {
		this.lastFriendsReadings = lastFriendsBooks;
	}

}
