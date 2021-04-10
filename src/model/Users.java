package model;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "users")
public class Users {
	private ArrayList<Link> users;

	public Users() {
		this.users = new ArrayList<>();
	}

	@XmlElement(name = "user")
	public ArrayList<Link> getUsers() {
		return users;
	}

	public void setUsers(ArrayList<Link> users) {
		this.users = users;
	}

}
