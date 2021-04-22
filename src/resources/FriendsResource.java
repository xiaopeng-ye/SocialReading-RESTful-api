package resources;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.naming.NamingContext;

import dao.FriendDAO;
import dao.ReadingDAO;
import dao.UserDAO;
import model.Readings;
import model.Friends;
import model.User;

@Path("/users/{id_user}/friends")
public class FriendsResource {
	@Context
	private UriInfo uriInfo;

	private DataSource ds;
	private Connection conn;
	private final FriendDAO friendDAO;
	private final UserDAO userDAO;
	private final ReadingDAO readingDAO;

	private int userId;
	private final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

	public FriendsResource(@PathParam("id_user") int userId) {
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
		friendDAO = FriendDAO.getInstance();
		userDAO = UserDAO.getInstance();
		readingDAO = ReadingDAO.getInstance();
	}

	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response addFriend(User user) {
		try {
			User friend = userDAO.getUser(conn, user.getId());

			if (friend == null) {
				return Response.status(Response.Status.NOT_FOUND).entity("Usuario como amigo no encontrado").build();
			}
			friendDAO.addFriend(conn, this.userId, friend.getId());
			return Response.status(Response.Status.CREATED)
					.header("Location", uriInfo.getAbsolutePath() + "/" + user.getId()).build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("No se pudo añadir como amigo\n" + e.getStackTrace()).build();
		}
	}

	@DELETE
	@Path("{id_friend}")
	public Response deleteFriend(@PathParam("id_friend") int friendId) {
		try {
			if (friendDAO.deleteFriend(conn, this.userId, friendId))
				return Response.status(Response.Status.NO_CONTENT).build();
			else
				return Response.status(Response.Status.NOT_FOUND).entity("Usuario como amigo no encontrado").build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("No se pudo eliminar el amigo \n" + e.getStackTrace()).build();
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getFriends(@QueryParam("name") @DefaultValue("") String name,
			@QueryParam("limit") @DefaultValue("10") int limit, @QueryParam("offset") @DefaultValue("0") int offset) {
		try {
			Friends friends = friendDAO.getFriends(conn, uriInfo, this.userId, name, limit, offset);
			int nextOffset = offset + limit;
			friends.setNext(uriInfo.getAbsolutePath() + "?name=" + name + "&limit=" + limit + "&offset=" + nextOffset);
			int prevOffset = offset - limit;
			if (prevOffset >= 0) {
				friends.setPrev(
						uriInfo.getAbsolutePath() + "?name=" + name + "&limit=" + limit + "&offset=" + prevOffset);
			}
			return Response.status(Response.Status.OK).entity(friends)
					.header("Content-Location",
							uriInfo.getAbsolutePath() + "?name=" + name + "&limit=" + limit + "&offset=" + offset)
					.build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
		}
	}

	@GET
	@Path("readings")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getFriendsReadingBook(@QueryParam("date") @DefaultValue("") String date,
			@QueryParam("limit") @DefaultValue("10") int limit, @QueryParam("offset") @DefaultValue("0") int offset) {
		try {
			Date castDate;
			if (date.equals("")) {
				castDate = new Date();
			} else {
				castDate = format.parse(date);
			}

			Readings readings = readingDAO.getFriendsReadings(conn, uriInfo, format, this.userId, castDate, limit,
					offset);
			int nextOffset = limit + offset;
			readings.setNext(uriInfo.getAbsolutePath() + "?date=" + format.format(castDate) + "&limit=" + limit
					+ "&offset=" + nextOffset);
			int prevOffset = offset - limit;
			if (prevOffset >= 0) {
				readings.setPrev(uriInfo.getAbsolutePath() + "?date=" + format.format(castDate) + "&limit=" + limit
						+ "&offset=" + prevOffset);
			}
			return Response.status(Response.Status.OK).entity(readings)
					.header("Content-Location", uriInfo.getAbsolutePath()).build();
		} catch (ParseException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Fecha con formato incorrecto").build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
		}
	}
	
	@GET
	@Path("recommended_books")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getRecommendedBook(@QueryParam("qualification") @DefaultValue("0") int qualification,
			@QueryParam("category") @DefaultValue("") String category) {
		try {
			Readings readings = readingDAO.getRecommendedReadings(conn, uriInfo, this.userId, qualification, category);
			return Response.status(Response.Status.OK).entity(readings)
					.header("Content-Location", uriInfo.getAbsolutePath()).build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
		}
	}

}
