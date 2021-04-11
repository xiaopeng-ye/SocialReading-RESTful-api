package resources;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

import model.Book;
import model.Readings;
import model.Link;
import model.Reading;

@Path("/users/{id_user}/readings")
public class ReadingsResource {
	@Context
	private UriInfo uriInfo;

	private DataSource ds;
	private Connection conn;

	private int userId;
	private final SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");

	public ReadingsResource(@PathParam("id_user") int userId) {
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
	public Response createReading(Reading reading) {
		try {
			Book book = reading.getBook();
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM book WHERE isbn = ?");
			ps.setString(1, book.getIsbn());
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				book = new Book(rs.getString("title"), rs.getString("author"), rs.getString("category"),
						rs.getString("isbn"));
				book.setId(rs.getInt("id"));
				reading.setBook(book);
			} else {
				ps = conn.prepareStatement(
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
				} else {
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No se pudo crear el libro ")
							.build();
				}
			}
			ps = conn.prepareStatement(
					"INSERT INTO `SocialReading`.`reading` (`id_user`,`id_book`,`date`,`qualification`) VALUES(?, ?, ?, ?)");
			ps.setInt(1, this.userId);
			ps.setInt(2, book.getId());
			ps.setDate(3, new java.sql.Date(format.parse(reading.getDate()).getTime()));
			ps.setInt(4, reading.getQualification());
			ps.executeUpdate();
			return Response.status(Response.Status.CREATED)
					.header("Location", uriInfo.getAbsolutePath() + "/" + book.getId()).build();
		} catch (ParseException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Fecha con formato incorrecto").build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("No se pudo crear la lectura\n" + e.getStackTrace()).build();
		}
	}

	@DELETE
	@Path("{id_book}")
	public Response deleteReading(@PathParam("id_book") int bookId) {
		try {
			PreparedStatement ps = conn.prepareStatement("DELETE FROM reading WHERE id_user = ? AND id_book = ?");
			ps.setInt(1, this.userId);
			ps.setInt(2, bookId);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 1)
				return Response.status(Response.Status.NO_CONTENT).build();
			else
				return Response.status(Response.Status.NOT_FOUND).entity("Lectura no encontrado").build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("No se pudo eliminar la lectura\n" + e.getStackTrace()).build();
		}
	}

	@PUT
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("{id_book}")
	public Response updateReading(@PathParam("id_book") int bookId, Reading newReading) {
		try {
			Reading reading = new Reading();
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM reading WHERE id_user = ? AND id_book = ?");
			ps.setInt(1, this.userId);
			ps.setInt(2, bookId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				Book book = newReading.getBook();
				ps = conn.prepareStatement("SELECT * FROM book WHERE isbn = ?");
				ps.setString(1, book.getIsbn());
				rs = ps.executeQuery();
				if (rs.next()) {
					book = new Book(rs.getString("title"), rs.getString("author"), rs.getString("category"),
							rs.getString("isbn"));
					book.setId(rs.getInt("id"));
					newReading.setBook(book);
				} else {
					return Response.status(Response.Status.NOT_FOUND).entity("Libro no encontrado").build();
				}
			} else {
				return Response.status(Response.Status.NOT_FOUND).entity("Lectura no encontrado").build();
			}
			reading.setBook(newReading.getBook());
			reading.setDate(newReading.getDate());
			reading.setQualification(newReading.getQualification());

			ps = conn.prepareStatement(
					"UPDATE reading SET id_book = ?, date = ?, qualification = ? WHERE id_user = ? AND id_book = ?");
			ps.setInt(1, reading.getBook().getId());
			ps.setDate(2, new java.sql.Date(format.parse(reading.getDate()).getTime()));
			ps.setInt(3, reading.getQualification());
			ps.setInt(4, this.userId);
			ps.setInt(5, bookId);
			ps.executeUpdate();

			return Response.status(Response.Status.OK)
					.header("Content-Location", uriInfo.getAbsolutePath() + "/" + reading.getBook().getId()).build();
		} catch (ParseException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Fecha con formato incorrecto").build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("No se pudo actualizar la lecturan" + e.getStackTrace()).build();
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getReadingBooks(@QueryParam("date") @DefaultValue("") String date,
			@QueryParam("limit") @DefaultValue("10") int limit, @QueryParam("offset") @DefaultValue("0") int offset) {
		try {
			Date castDate;
			if (date.equals("")) {
				castDate = new Date();
			} else {
				castDate = format.parse(date);
			}
			PreparedStatement ps = conn.prepareStatement(
					"SELECT id_book FROM reading WHERE date < ? AND id_user = ? ORDER BY date DESC LIMIT ? OFFSET ?");
			ps.setDate(1, new java.sql.Date(castDate.getTime()));
			ps.setInt(2, this.userId);
			ps.setInt(3, limit);
			ps.setInt(4, offset);
			ResultSet rs = ps.executeQuery();
			int nextOffset = limit + offset;
			Readings readings = new Readings(uriInfo.getAbsolutePath() + "?date=" + format.format(castDate) + "&limit="
					+ limit + "&offset=" + nextOffset);
			ArrayList<Link> listBooks = readings.getReadings();
			while (rs.next()) {
				listBooks.add(
						new Link(rs.getInt("id_book"), uriInfo.getAbsolutePath() + "/" + rs.getInt("id_book"), "self"));
			}
			return Response.status(Response.Status.OK).entity(readings).build();
		} catch (ParseException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Fecha con formato incorrecto").build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
		}
	}
}
