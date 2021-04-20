package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.ws.rs.core.UriInfo;

import model.Link;
import model.User;
import model.Users;

public class UserDAO {
	private static UserDAO singleton = null;

	private UserDAO() {

	}

	public static UserDAO getInstance() {
		if (singleton == null) {
			singleton = new UserDAO();
		}
		return singleton;
	}

	public User addUser(Connection conn, User user) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(
				"INSERT INTO `SocialReading`.`user`(`name`,`gender`,`age`,`email`) VALUES (?, ?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS);
		ps.setString(1, user.getName());
		ps.setString(2, user.getGender());
		ps.setInt(3, user.getAge());
		ps.setString(4, user.getEmail());
		ps.executeUpdate();
		ResultSet generatedID = ps.getGeneratedKeys();
		if (generatedID.next()) {
			user.setId(generatedID.getInt(1));
			return user;
		}
		return null;
	}

	public User getUser(Connection conn, int id) throws SQLException {
		User user = null;
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM user WHERE id = ?");
		ps.setInt(1, id);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			user = new User(rs.getString("name"), rs.getString("gender"), rs.getInt("age"), rs.getString("email"));
			user.setId(id);
		}
		return user;
	}

	public boolean updateUser(Connection conn, User user) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(
				"UPDATE `SocialReading`.`user` SET `name` = ?, `gender` = ?, `age` = ?, `email` = ? WHERE `id` = ?");
		ps.setString(1, user.getName());
		ps.setString(2, user.getGender());
		ps.setInt(3, user.getAge());
		ps.setString(4, user.getEmail());
		ps.setInt(5, user.getId());
		int affectedRows = ps.executeUpdate();
		if (affectedRows == 1) {
			return true;
		}
		return false;
	}

	public boolean deleteUser(Connection conn, int id) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("DELETE FROM user WHERE id = ?");
		ps.setInt(1, id);
		int affectedRows = ps.executeUpdate();
		if (affectedRows == 1) {
			return true;
		}
		return false;
	}

	public Users getUsers(Connection conn, UriInfo uriInfo, String name) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("SELECT id FROM user WHERE name LIKE '%" + name + "%'");
		ResultSet rs = ps.executeQuery();
		Users users = new Users();
		ArrayList<Link> listUsers = users.getUsers();
		while (rs.next()) {
			listUsers.add(new Link(rs.getInt("id"), uriInfo.getAbsolutePath() + "/" + rs.getInt("id"), "self"));
		}
		return users;
	}

}
