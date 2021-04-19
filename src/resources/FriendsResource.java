package resources;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import model.Readings;
import model.Friends;
import model.Link;
import model.User;

@Path("/users/{id_user}/friends")
public class FriendsResource {
	@Context
	private UriInfo uriInfo;

	private DataSource ds;
	private Connection conn;

	private int userId;
	private final SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");

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
	}

	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response addFriend(User user) {
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM user WHERE id = ?");
			ps.setInt(1, user.getId());
			ps.setString(2, user.getName());
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				user.setId(rs.getInt("id"));
				user.setAge(rs.getInt("age"));
				user.setEmail(rs.getString("email"));
				ps = conn.prepareStatement("INSERT INTO is_friend_of VALUES (?, ?)");
				ps.setInt(1, this.userId);
				ps.setInt(2, user.getId());
				ps.executeUpdate();
				return Response.status(Response.Status.CREATED)
						.header("Location", uriInfo.getAbsolutePath() + "/" + user.getId()).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).entity("Usuario como amigo no encontrado").build();
			}
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("No se pudo añadir como amigo\n" + e.getStackTrace()).build();
		}
	}

	@DELETE
	@Path("{id_friend}")
	public Response deleteFriend(@PathParam("id_friend") int friendId) {
		try {
			PreparedStatement ps = conn
					.prepareStatement("DELETE FROM is_friend_of WHERE id_user_a = ? AND id_user_b = ?");
			ps.setInt(1, this.userId);
			ps.setInt(2, friendId);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 1)
				return Response.status(Response.Status.NO_CONTENT).build();
			else
				return Response.status(Response.Status.NOT_FOUND).entity("Amigo no encontrado").build();
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
			PreparedStatement ps = conn.prepareStatement(
					"SELECT e.id_user_b FROM user ub, is_friend_of e WHERE ub.id = e.id_user_b AND e.id_user_a = ? AND ub.name LIKE '%"
							+ name + "%' LIMIT ? OFFSET ?");
			ps.setInt(1, this.userId);
			ps.setInt(2, limit);
			ps.setInt(3, offset);
			ResultSet rs = ps.executeQuery();
			int nextOffset = offset + limit;
			Friends friends = new Friends(
					uriInfo.getAbsolutePath() + "?name=" + name + "&limit=" + limit + "&offset=" + nextOffset);
			ArrayList<Link> listFriends = friends.getUsers();
			while (rs.next()) {
				listFriends.add(new Link(rs.getInt(1), uriInfo.getBaseUri() + "users" + "/" + rs.getInt(1), "self"));
			}
			return Response.status(Response.Status.OK).entity(friends).header("Content-Location", uriInfo.getAbsolutePath()).build();
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

			PreparedStatement ps = conn.prepareStatement(
					"SELECT r.id_user FROM reading r, is_friend_of i WHERE i.id_user_b = r.id_user AND r.date < ? AND i.id_user_a = ? GROUP BY r.id_user ORDER BY MAX(r.date) DESC LIMIT ? OFFSET ?");
			ps.setDate(1, new java.sql.Date(castDate.getTime()));
			ps.setInt(2, this.userId);
			ps.setInt(3, limit);
			ps.setInt(4, offset);
			ResultSet rs = ps.executeQuery();
			int nextOffset = limit + offset;
			Readings readings = new Readings(
					uriInfo.getAbsolutePath() + "?date=" + date + "&limit=" + limit + "&offset=" + nextOffset);
			ArrayList<Link> listReadings = readings.getReadings();
			ps = conn.prepareStatement("SELECT id_book FROM reading WHERE id_user = ? ORDER BY date DESC LIMIT 1");
			while (rs.next()) {
				ps.setInt(1, rs.getInt("id_user"));
				ResultSet rs1 = ps.executeQuery();
				if (rs1.next()) {
					listReadings.add(new Link(rs1.getInt("id_book"), uriInfo.getBaseUri() + "users/"
							+ rs.getInt("id_user") + "/readings/" + rs1.getInt("id_book"), "self"));
				}

			}
			return Response.status(Response.Status.OK).entity(readings).header("Content-Location", uriInfo.getAbsolutePath()).build();
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
			PreparedStatement ps = conn.prepareStatement(
					"SELECT l.id, le.id_user FROM book l, reading le WHERE l.id=le.id_book AND le.qualification > ? AND l.category LIKE '%"
							+ category
							+ "%' AND le.id_user IN ( SELECT id_user_b FROM is_friend_of WHERE id_user_a = ?)");
			ps.setInt(1, qualification);
			ps.setInt(2, this.userId);
			ResultSet rs = ps.executeQuery();
			Readings readings = new Readings();
			ArrayList<Link> listReadings = readings.getReadings();
			while (rs.next()) {
				listReadings.add(new Link(rs.getInt("id_user"),
						uriInfo.getBaseUri() + "users/" + rs.getInt("id_user") + "/readings/" + rs.getInt("id"),
						"self"));
			}
			return Response.status(Response.Status.OK).entity(readings).header("Content-Location", uriInfo.getAbsolutePath()).build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
		}
	}
}
