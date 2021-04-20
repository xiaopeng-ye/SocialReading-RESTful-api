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

import dao.FriendDAO;
import dao.ReadingDAO;
import dao.UserDAO;
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
	private final UserDAO userDAO;
	private final FriendDAO friendDAO;
	private final ReadingDAO readingDAO;

	private int userId;
	private final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

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
		userDAO = UserDAO.getInstance();
		friendDAO = FriendDAO.getInstance();
		readingDAO = ReadingDAO.getInstance();
	}

	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getAppInfo() {
		try {
			User user = userDAO.getUser(conn, this.userId);
			if (user == null) {
				return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado").build();
			}

			int numFriends = friendDAO.getFriendsNum(conn, this.userId);

			Reading reading = readingDAO.getLastReading(conn, format, this.userId);

			App app = new App(user, reading, numFriends);

			ArrayList<Link> listReadings = app.getLastFriendsReadings();
			readingDAO.getLastFriendsReadings(conn, uriInfo, listReadings, this.userId);

			return Response.status(Response.Status.OK).entity(app).header("Content-Location", uriInfo.getAbsolutePath())
					.build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Error de acceso a BBDD" + e.getMessage()).build();
		}
	}

}
