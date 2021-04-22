package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.ws.rs.core.UriInfo;

import model.Link;
import model.Reading;
import model.Readings;

public class ReadingDAO {

	private static ReadingDAO singleton = null;

	private ReadingDAO() {

	}

	public static ReadingDAO getInstance() {
		if (singleton == null) {
			singleton = new ReadingDAO();
		}
		return singleton;
	}

	public boolean addReading(Connection conn, SimpleDateFormat format, int userId, Reading reading)
			throws SQLException, ParseException {
		PreparedStatement ps = conn.prepareStatement(
				"INSERT INTO `SocialReading`.`reading` (`id_user`,`id_book`,`date`,`qualification`) VALUES(?, ?, ?, ?)");
		ps.setInt(1, userId);
		ps.setInt(2, reading.getBook().getId());
		ps.setDate(3, new java.sql.Date(format.parse(reading.getDate()).getTime()));
		ps.setInt(4, reading.getQualification());
		int affectedRows = ps.executeUpdate();
		if (affectedRows == 1) {
			return true;
		}
		return false;
	}

	public Reading getReading(Connection conn, SimpleDateFormat format, int userId, int bookId) throws SQLException {
		Reading reading = null;
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM reading WHERE id_user = ? AND id_book = ?");
		ps.setInt(1, userId);
		ps.setInt(2, bookId);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			reading = new Reading();
			reading.setBook(BookDAO.getInstance().getBook(conn, bookId));
			reading.setDate(format.format(new Date(rs.getDate("date").getTime())));
			reading.setQualification((byte) rs.getInt("qualification"));
		}
		return reading;
	}

	public Reading getLastReading(Connection conn, SimpleDateFormat format, int userId) throws SQLException {
		Reading reading = null;
		PreparedStatement ps = conn.prepareStatement(
				"SELECT id_book, qualification, date FROM reading WHERE id_user = ? ORDER BY date DESC");
		ps.setInt(1, userId);
		ResultSet rs = ps.executeQuery();

		if (rs.next()) {
			reading = new Reading();
			reading.setBook(BookDAO.getInstance().getBook(conn, rs.getInt("id_book")));
			reading.setDate(format.format(new Date(rs.getDate("date").getTime())));
			reading.setQualification((byte) rs.getInt("qualification"));
		}
		return reading;
	}

	public boolean deleteReading(Connection conn, int userId, int bookId) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("DELETE FROM reading WHERE id_user = ? AND id_book = ?");
		ps.setInt(1, userId);
		ps.setInt(2, bookId);
		int affectedRows = ps.executeUpdate();
		if (affectedRows == 1) {
			return true;
		}
		return false;
	}

	public boolean updateReading(Connection conn, SimpleDateFormat format, int userId, int bookId, Reading reading)
			throws SQLException, ParseException {
		PreparedStatement ps = conn.prepareStatement(
				"UPDATE reading SET id_book = ?, date = ?, qualification = ? WHERE id_user = ? AND id_book = ?");
		ps.setInt(1, reading.getBook().getId());
		ps.setDate(2, new java.sql.Date(format.parse(reading.getDate()).getTime()));
		ps.setInt(3, reading.getQualification());
		ps.setInt(4, userId);
		ps.setInt(5, bookId);
		int affectedRows = ps.executeUpdate();
		if (affectedRows == 1) {
			return true;
		}
		return false;
	}

	public boolean existReading(Connection conn, int userId, int bookId) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM reading WHERE id_user = ? AND id_book = ?");
		ps.setInt(1, userId);
		ps.setInt(2, bookId);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			return true;
		}
		return false;
	}

	public Readings getReadings(Connection conn, UriInfo uriInfo, SimpleDateFormat format, int userId, Date castDate,
			int limit, int offset) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(
				"SELECT id_book FROM reading WHERE date < ? AND id_user = ? ORDER BY date DESC LIMIT ? OFFSET ?");
		ps.setDate(1, new java.sql.Date(castDate.getTime()));
		ps.setInt(2, userId);
		ps.setInt(3, limit);
		ps.setInt(4, offset);
		ResultSet rs = ps.executeQuery();
		Readings readings = new Readings();
		ArrayList<Link> listBooks = readings.getReadings();
		while (rs.next()) {
			listBooks.add(
					new Link(rs.getInt("id_book"), uriInfo.getAbsolutePath() + "/" + rs.getInt("id_book"), "self"));
		}
		return readings;
	}

	public Readings getFriendsReadings(Connection conn, UriInfo uriInfo, SimpleDateFormat format, int userId,
			Date castDate, int limit, int offset) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(
				"SELECT r.id_user FROM reading r, is_friend_of i WHERE i.id_user_b = r.id_user AND r.date < ? AND i.id_user_a = ? GROUP BY r.id_user ORDER BY MAX(r.date) DESC LIMIT ? OFFSET ?");
		ps.setDate(1, new java.sql.Date(castDate.getTime()));
		ps.setInt(2, userId);
		ps.setInt(3, limit);
		ps.setInt(4, offset);
		ResultSet rs = ps.executeQuery();
		Readings readings = new Readings();
		ArrayList<Link> listReadings = readings.getReadings();
		ps = conn.prepareStatement("SELECT id_book FROM reading WHERE id_user = ? ORDER BY date DESC LIMIT 1");
		while (rs.next()) {
			ps.setInt(1, rs.getInt("id_user"));
			ResultSet rs1 = ps.executeQuery();
			if (rs1.next()) {
				listReadings.add(new Link(rs1.getInt("id_book"),
						uriInfo.getBaseUri() + "users/" + rs.getInt("id_user") + "/readings/" + rs1.getInt("id_book"),
						"self"));
			}

		}
		return readings;
	}

	public void getLastFriendsReadings(Connection conn, UriInfo uriInfo, ArrayList<Link> listReadings, int userId)
			throws SQLException {
		PreparedStatement ps = conn.prepareStatement(
				"SELECT r.id_user FROM reading r, is_friend_of i WHERE i.id_user_b = r.id_user AND i.id_user_a = ? GROUP BY r.id_user ORDER BY MAX(r.date) DESC");
		ps.setInt(1, userId);
		ResultSet rs = ps.executeQuery();
		ps = conn.prepareStatement("SELECT id_book FROM reading WHERE id_user = ? ORDER BY date DESC LIMIT 1");
		while (rs.next()) {
			ps.setInt(1, rs.getInt("id_user"));
			ResultSet rs1 = ps.executeQuery();
			if (rs1.next()) {
				listReadings.add(new Link(rs1.getInt("id_book"),
						uriInfo.getBaseUri() + "users/" + rs.getInt("id_user") + "/readings/" + rs1.getInt("id_book"),
						"self"));
			}

		}
	}
}
