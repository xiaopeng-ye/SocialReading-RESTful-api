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

import model.Link;
import model.User;
import model.Users;

@Path("/users")
public class UsersResource {
	@Context
	private UriInfo uriInfo;

	private DataSource ds;
	private Connection conn;

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
	}

	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response createUser(User user) {
		try {
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
				String location = uriInfo.getAbsolutePath() + "/" + user.getId();
				return Response.status(Response.Status.CREATED).entity(user).header("Location", location).build();
				// .header("Content-Location", location).build();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No se pudo crear el usuario").build();

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
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM user WHERE id = ?");
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				User user = new User(rs.getString("name"), rs.getString("gender"), rs.getInt("age"),
						rs.getString("email"));
				user.setId(id);
				return Response.status(Response.Status.OK).entity(user)
						.header("Content-Location", uriInfo.getAbsolutePath()).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado").build();
			}
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
		}
	}

	@PUT
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("{id_user}")
	public Response updateGaraje(@PathParam("id_user") int id, User newUser) {
		try {
			User user;
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM user WHERE id = ?");
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				user = new User(rs.getString("name"), rs.getString("gender"), rs.getInt("age"), rs.getString("email"));
			} else {
				return Response.status(Response.Status.NOT_FOUND).entity("Elemento no encontrado").build();
			}
			user.setName(newUser.getName());
			user.setGender(newUser.getGender());
			user.setAge(newUser.getAge());
			user.setEmail(newUser.getEmail());

			ps = conn.prepareStatement(
					"UPDATE `SocialReading`.`user` SET `name` = ?, `gender` = ?, `age` = ?, `email` = ? WHERE `id` = ?");
			ps.setString(1, user.getName());
			ps.setString(2, user.getGender());
			ps.setInt(3, user.getAge());
			ps.setString(4, user.getEmail());
			ps.setInt(5, id);
			ps.executeUpdate();

			return Response.status(Response.Status.OK).entity(user)
					.header("Content-Location", uriInfo.getAbsolutePath()).build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("No se pudo actualizar el usuario\n" + e.getStackTrace()).build();
		}
	}

	@DELETE
	@Path("{id_user}")
	public Response deleteGaraje(@PathParam("id_user") int id) {
		try {
			PreparedStatement ps = conn.prepareStatement("DELETE FROM user WHERE id = ?;");
			ps.setInt(1, id);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 1)
				return Response.status(Response.Status.NO_CONTENT).build();
			else
				return Response.status(Response.Status.NOT_FOUND).entity("Elemento no encontrado").build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("No se pudo eliminar el usuario\n" + e.getStackTrace()).build();
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getGarajes2(@QueryParam("name") @DefaultValue("") String name) {
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT id FROM user WHERE name LIKE '%" + name + "%'");
			ResultSet rs = ps.executeQuery();
			Users users = new Users();
			ArrayList<Link> listUsers = users.getUsers();
			while (rs.next()) {
				listUsers.add(new Link(uriInfo.getAbsolutePath() + "/" + rs.getInt("id"), "self"));
			}
			return Response.status(Response.Status.OK).entity(users).build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
		}
	}

}
