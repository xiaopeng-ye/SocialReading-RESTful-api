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

import dao.BookDAO;
import dao.ReadingDAO;
import model.Book;
import model.Readings;
import model.Reading;

@Path("/users/{id_user}/readings")
public class ReadingsResource {
	@Context
	private UriInfo uriInfo;

	private DataSource ds;
	private Connection conn;
	private final BookDAO bookDAO;
	private final ReadingDAO readingDAO;

	private int userId;
	private final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

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
		bookDAO = BookDAO.getInstance();
		readingDAO = ReadingDAO.getInstance();
	}

	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response createReading(Reading reading) {
		try {
			Book book = reading.getBook();
			book = bookDAO.getBook(conn, book.getIsbn());
			if (book == null) {
				book = bookDAO.addBook(conn, reading.getBook());
				if (book == null) {
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No se pudo crear el libro")
							.build();
				}
			}
			reading.setBook(book);
			readingDAO.addReading(conn, format, this.userId, reading);
			return Response.status(Response.Status.CREATED)
					.header("Location", uriInfo.getAbsolutePath() + "/" + book.getId()).build();
		} catch (ParseException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Fecha con formato incorrecto").build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("No se pudo crear la lectura\n" + e.getStackTrace()).build();
		}
	}

	@GET
	@Path("{id_book}")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getReading(@PathParam("id_book") int bookId) {
		try {
			Reading reading = readingDAO.getReading(conn, format, this.userId, bookId);
			if (reading == null) {
				return Response.status(Response.Status.NOT_FOUND).entity("Lectura no encontrada").build();
			}
			return Response.status(Response.Status.OK).entity(reading)
					.header("Content-Location", uriInfo.getAbsolutePath()).build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
		}
	}

	@DELETE
	@Path("{id_book}")
	public Response deleteReading(@PathParam("id_book") int bookId) {
		try {
			if (readingDAO.deleteReading(conn, this.userId, bookId)) {
				return Response.status(Response.Status.NO_CONTENT).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).entity("Lectura no encontrado").build();
			}
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
			if (readingDAO.existReading(conn, this.userId, bookId)) {
				Book book = bookDAO.getBook(conn, newReading.getBook().getIsbn());
				if (book != null) {
					newReading.setBook(book);
				} else {
					return Response.status(Response.Status.NOT_FOUND).entity("Libro no encontrado").build();
				}
			} else {
				return Response.status(Response.Status.NOT_FOUND).entity("Lectura no encontrada").build();
			}

			reading.setBook(newReading.getBook());
			reading.setDate(newReading.getDate());
			reading.setQualification(newReading.getQualification());

			readingDAO.updateReading(conn, format, this.userId, bookId, reading);

			return Response.status(Response.Status.OK)
					.header("Content-Location", uriInfo.getAbsolutePath() + "/" + reading.getBook().getId()).build();
		} catch (ParseException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Fecha con formato incorrecto").build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("No se pudo actualizar la lectura\n" + e.getStackTrace()).build();
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
			Readings readings = readingDAO.getReadings(conn, uriInfo, format, this.userId, castDate, limit, offset);
			int nextOffset = limit + offset;
			readings.setNext(uriInfo.getAbsolutePath() + "?date=" + date + "&limit=" + limit + "&offset=" + nextOffset);
			int prevOffset = offset - limit;
			if (prevOffset >= 0) {
				readings.setPrev(
						uriInfo.getAbsolutePath() + "?date=" + date + "&limit=" + limit + "&offset=" + prevOffset);
			}
			return Response.status(Response.Status.OK).entity(readings)
					.header("Content-Location", uriInfo.getAbsolutePath()).build();
		} catch (ParseException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Fecha con formato incorrecto").build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
		}
	}
}
