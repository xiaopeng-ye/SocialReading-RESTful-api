package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.ws.rs.core.UriInfo;

import model.Book;
import model.Books;
import model.Link;

public class BookDAO {
	private static BookDAO singleton = null;

	private BookDAO() {

	}

	public static BookDAO getInstance() {
		if (singleton == null) {
			singleton = new BookDAO();
		}
		return singleton;
	}

	public Book getBook(Connection conn, String isbn) throws SQLException {
		Book book = null;
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM book WHERE isbn = ?");
		ps.setString(1, isbn);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			book = new Book(rs.getString("title"), rs.getString("author"), rs.getString("category"),
					rs.getString("isbn"));
			book.setId(rs.getInt("id"));
		}
		return book;
	}
	
	public Book getBook(Connection conn, int id) throws SQLException {
		Book book = null;
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM book WHERE id = ?");
		ps.setInt(1, id);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			book = new Book(rs.getString("title"), rs.getString("author"), rs.getString("category"),
					rs.getString("isbn"));
			book.setId(rs.getInt("id"));
		}
		return book;
	}

	public Book addBook(Connection conn, Book book) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(
				"INSERT INTO `SocialReading`.`book` (`title`,`author`,`category`,`isbn`) VALUES(?, ?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS);
		ps.setString(1, book.getTitle());
		ps.setString(2, book.getAuthor());
		ps.setString(3, book.getCategory());
		ps.setString(4, book.getIsbn());
		ps.executeUpdate();
		ResultSet generatedID = ps.getGeneratedKeys();
		if (generatedID.next()) {
			book.setId(generatedID.getInt(1));
			return book;
		}
		return null;
	}
	
	public Books getRecommendedBooks(Connection conn, UriInfo uriInfo, int userId, int qualification,
			String category) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(
				"SELECT l.id FROM book l, reading le WHERE l.id=le.id_book AND le.qualification > ? AND l.category LIKE '%"
						+ category + "%' AND le.id_user IN ( SELECT id_user_b FROM is_friend_of WHERE id_user_a = ?) GROUP BY l.id");
		ps.setInt(1, qualification);
		ps.setInt(2, userId);
		ResultSet rs = ps.executeQuery();
		Books books = new Books();
		ArrayList<Link> listReadings = books.getBooks();
		while (rs.next()) {
			listReadings.add(new Link(rs.getInt("id"), uriInfo.getBaseUri() + "books/" + rs.getInt("id"), "self"));
		}
		return books;
	}
}
