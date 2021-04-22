package resources;

import java.sql.Connection;
import java.sql.SQLException;

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

import dao.BookDAO;
import model.Book;

@Path("/books")
public class BooksResource {
	@Context
	private UriInfo uriInfo;

	private DataSource ds;
	private Connection conn;
	private final BookDAO bookDAO;

	public BooksResource() {
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
	}

	@GET
	@Path("{id_book}")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getUser(@PathParam("id_book") int id) {
		try {
			Book book = bookDAO.getBook(conn, id);
			if (book == null) {
				return Response.status(Response.Status.NOT_FOUND).entity("Libro no encontrado").build();
			}
			return Response.status(Response.Status.OK).entity(book)
					.header("Content-Location", uriInfo.getAbsolutePath()).build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
		}
	}

}
