package com.dub.gutenberg.services;


import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.dub.gutenberg.domain.Book;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** 
 * Clean this mess, return here Book, not Optional<Book> 
 * and throw instead a BookNotFoundException
 * */

public interface BookService {
	
	//List<Book> allBooksByCategory(String categorySlug, String sortBy) throws IOException;
		
	Mono<Book> getBookBySlug(String slug) throws IOException;
	
	Mono<Book> getBookById(String bookId) throws IOException;
		
	Flux<Book> fluxBooksByCategory(String categorySlug, String sortBy) throws IOException;
	
	// more advanced methods
	//List<Book> getBooksBoughtWith(String bookId, int limit) throws IOException;

	Flux<Book> getFluxBooksBoughtWith(String bookId, int limit) throws IOException;

}