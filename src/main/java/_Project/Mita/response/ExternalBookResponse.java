package _Project.Mita.response;

import java.time.LocalDate;

public record ExternalBookResponse( 
	String title,
	String author,
	String isbn,
	String publisher,
	LocalDate publishedDate
){}
