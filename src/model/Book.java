package model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "book")
@XmlType(propOrder = { "title", "author", "category", "isbn" })
public class Book {
	private int id;
	private String title;
	private String author;
	private String category;
	private String isbn;
	
	public Book() {
		
	}
	
	public Book(String title, String author, String category, String isbn) {
		super();
		this.title = title;
		this.author = author;
		this.category = category;
		this.isbn = isbn;
	}
	
	@XmlAttribute(required=false)
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}
	
	
}
