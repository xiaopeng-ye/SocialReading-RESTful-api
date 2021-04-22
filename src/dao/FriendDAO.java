package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.ws.rs.core.UriInfo;

import model.Friends;
import model.Link;

public class FriendDAO {
	private static FriendDAO singleton = null;

	private FriendDAO() {

	}

	public static FriendDAO getInstance() {
		if (singleton == null) {
			singleton = new FriendDAO();
		}
		return singleton;
	}

	public boolean addFriend(Connection conn, int userId, int friendId) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("INSERT INTO is_friend_of VALUES (?, ?)");
		ps.setInt(1, userId);
		ps.setInt(2, friendId);
		int affectedRows = ps.executeUpdate();
		if (affectedRows == 1) {
			return true;
		}
		return false;
	}

	public boolean deleteFriend(Connection conn, int userId, int friendId) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("DELETE FROM is_friend_of WHERE id_user_a = ? AND id_user_b = ?");
		ps.setInt(1, userId);
		ps.setInt(2, friendId);
		int affectedRows = ps.executeUpdate();
		if (affectedRows == 1) {
			return true;
		}
		return false;
	}

	public Friends getFriends(Connection conn, UriInfo uriInfo, int userId, String name, int limit, int offset)
			throws SQLException {
		PreparedStatement ps = conn.prepareStatement(
				"SELECT e.id_user_b FROM user ub, is_friend_of e WHERE ub.id = e.id_user_b AND e.id_user_a = ? AND ub.name LIKE '%"
						+ name + "%' LIMIT ? OFFSET ?");
		ps.setInt(1, userId);
		ps.setInt(2, limit);
		ps.setInt(3, offset);
		ResultSet rs = ps.executeQuery();
		Friends friends = new Friends();
		ArrayList<Link> listFriends = friends.getFriends();
		while (rs.next()) {
			listFriends.add(new Link(rs.getInt(1), uriInfo.getBaseUri() + "users" + "/" + rs.getInt(1), "self"));
		}
		return friends;
	}

	public int getFriendsNum(Connection conn, int userId) throws SQLException {
		PreparedStatement ps = conn
				.prepareStatement("SELECT COUNT(id_user_b) FROM is_friend_of WHERE id_user_a = ? GROUP BY id_user_a");
		ps.setInt(1, userId);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			return rs.getInt(1);
		}
		return 0;
	}
	

}
