package resources;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.naming.NamingContext;

import model.App;
import model.Book;
import model.Link;
import model.Reading;
import model.User;

@Path("/users/{id_user}/app")
public class AppResource {
	@Context
	private UriInfo uriInfo;

	private DataSource ds;
	private Connection conn;

	private int userId;
	private final SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");

	public AppResource(@PathParam("id_user") int userId) {
		this.userId = userId;
		InitialContext ctx;
		try {
			ctx = new InitialContext();
			NamingContext envCtx = (NamingContext) ctx.lookup("java:comp/env");

			ds = (DataSource) envCtx.lookup("jdbc/SocialReading");
			conn = ds.getConnection();
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getAppInfo() {
		try {
			User user;
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM user WHERE id = ?");
			ps.setInt(1, this.userId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				user = new User(rs.getString("name"), rs.getString("gender"), rs.getInt("age"), rs.getString("email"));
				user.setId(this.userId);
			} else {
				return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado").build();
			}

			int numFriends = 0;
			ps = conn.prepareStatement(
					"SELECT COUNT(id_user_b) FROM is_friend_of WHERE id_user_a = ? GROUP BY id_user_a");
			ps.setInt(1, this.userId);
			rs = ps.executeQuery();
			if (rs.next()) {
				numFriends = rs.getInt(1);
			}

			Book book = null;
			Reading reading = new Reading();
			ps = conn.prepareStatement("SELECT id_book, qualification, date FROM reading WHERE id_user = ?");
			ps.setInt(1, this.userId);
			rs = ps.executeQuery();

			ps = conn.prepareStatement("SELECT * FROM book WHERE id = ?");
			if (rs.next()) {
				ps.setInt(1, rs.getInt(1));
				reading.setQualification((byte) rs.getInt(2));
				reading.setDate(this.format.format(new Date(rs.getDate(3).getTime())));
			}
			rs = ps.executeQuery();
			if (rs.next()) {
				book = new Book(rs.getString("title"), rs.getString("author"), rs.getString("category"),
						rs.getString("isbn"));
				book.setId(rs.getInt("id"));
				reading.setBook(book);
			}
			
			App app = new App(user, reading, numFriends);
			ArrayList<Link> listReadings = app.getlastFriendsReadings();
			
			ps = conn.prepareStatement(
					"SELECT r.id_user FROM reading r, is_friend_of i WHERE i.id_user_b = r.id_user AND i.id_user_a = ? GROUP BY r.id_user ORDER BY MAX(r.date) DESC");
			ps.setInt(1, this.userId);
			rs = ps.executeQuery();
			ps = conn.prepareStatement("SELECT id_book FROM reading WHERE id_user = ? ORDER BY date DESC LIMIT 1");
			while (rs.next()) {
				ps.setInt(1, rs.getInt("id_user"));
				ResultSet rs1 = ps.executeQuery();
				if (rs1.next()) {
					listReadings.add(new Link(rs1.getInt("id_book"), uriInfo.getBaseUri() + "users/"
							+ rs.getInt("id_user") + "/readings/" + rs1.getInt("id_book"), "self"));
				}

			}
			return Response.status(Response.Status.OK).entity(app).build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
		}
	}

}
