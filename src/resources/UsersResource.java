package resources;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import model.Link;
import model.User;
import model.Users;

@Path("/users")
public class UsersResource {
	@Context
	private UriInfo uriInfo;

	private DataSource ds;
	private Connection conn;
	private final UserDAO userDAO;

	public UsersResource() {
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
	}

	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response createUser(User user) {
		try {
			User createdUser = userDAO.addUser(conn, user);
			if (createdUser == null) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No se pudo crear el usuario")
						.build();
			}
			return Response.status(Response.Status.CREATED)
					.header("Location", uriInfo.getAbsolutePath() + "/" + user.getId()).build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("No se pudo crear el usuario\n" + e.getStackTrace()).build();
		}
	}

	@GET
	@Path("{id_user}")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getUser(@PathParam("id_user") int id) {
		try {
			User user = userDAO.getUser(conn, id);
			if (user == null) {
				return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado").build();
			}
			return Response.status(Response.Status.OK).entity(user)
					.header("Content-Location", uriInfo.getAbsolutePath()).build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
		}
	}

	@PUT
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("{id_user}")
	public Response updateUser(@PathParam("id_user") int id, User newUser) {
		try {
			User user = userDAO.getUser(conn, id);
			if (user == null) {
				return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado").build();
			}

			user.setId(id);
			user.setGender(newUser.getGender());
			user.setAge(newUser.getAge());
			user.setEmail(newUser.getEmail());

			userDAO.updateUser(conn, user);

			return Response.status(Response.Status.OK).header("Content-Location", uriInfo.getAbsolutePath()).build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("No se pudo actualizar el usuario\n" + e.getStackTrace()).build();
		}
	}

	@DELETE
	@Path("{id_user}")
	public Response deleteUser(@PathParam("id_user") int id) {
		try {
			if (userDAO.deleteUser(conn, id)) {
				return Response.status(Response.Status.NO_CONTENT).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado").build();
			}
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("No se pudo eliminar el usuario\n" + e.getStackTrace()).build();
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getUsers(@QueryParam("name") @DefaultValue("") String name) {
		try {
			Users users = userDAO.getUsers(conn, uriInfo, name);
			return Response.status(Response.Status.OK).entity(users)
					.header("Content-Location", uriInfo.getAbsolutePath() + "?name=" + name).build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
		}
	}

}
