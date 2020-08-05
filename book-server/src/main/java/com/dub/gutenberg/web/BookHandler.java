package com.dub.gutenberg.web;

import java.io.IOException;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.dub.gutenberg.domain.Book;
import com.dub.gutenberg.domain.BookSearch;
import com.dub.gutenberg.exceptions.BookNotFoundException;
import com.dub.gutenberg.services.BookService;
import com.dub.gutenberg.services.SearchService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/** 
 * Here I use a consistent local exception handling.
 * The exception is caught and a new custom exception is returned, 
 * wrapped in a Mono.error.
 * This Mono.error triggers a fallback.
 * */
@Component
public class BookHandler {
	
	@Autowired
	private BookService bookService;
	
	@Autowired
	private SearchService searchService;
	
	public Mono<ServerResponse> getBookBySlug(ServerRequest request) {
		
		Mono<String> bookSlug = Mono.just(request.pathVariable("bookSlug"));
		
		return bookSlug
				.flatMap(transformBySlug)
				.flatMap(bookSuccess)
				.onErrorResume(bookFallback);
	}

	public Mono<ServerResponse> allBooksByCategory(ServerRequest request) {
		
		Mono<Flux<Book>> books = getBooksByCategory(
							request.pathVariable("categorySlug"), 
							request.pathVariable("sortBy"));
						
		return books.flatMap(searchSuccess).onErrorResume(searchFallback);	
	}
	
	public Mono<ServerResponse> bookById(ServerRequest request) {
		
		Mono<String> bookId = Mono.just(request.pathVariable("id"));
		
		return bookId
				.flatMap(transformById)
				.flatMap(bookSuccess)
				.onErrorResume(bookFallback);
	}
	

	public Mono<ServerResponse> booksBoughtWith(ServerRequest request) {
		
		Mono<Flux<Book>> books = getBooksBoughtWith(
				request.pathVariable("bookId"),
				Integer.parseInt(request.pathVariable("outLimit")));
		
		return books
				.flatMap(searchSuccess)
				.onErrorResume(searchFallback);
	}
	
	
	public Mono<ServerResponse> searchByTitle(ServerRequest request) {
					
		final Mono<BookSearch> toto = request
					.bodyToMono(BookSearch.class);		
		Mono<Flux<Book>> books = toto.flatMap(convertByTitle);		
		return books.flatMap(searchSuccess).onErrorResume(searchFallback);
	}
	
	
	public Mono<ServerResponse> searchByDescription(ServerRequest request) {
		
		final Mono<BookSearch> toto = request
				.bodyToMono(BookSearch.class);
		Mono<Flux<Book>> books = toto.flatMap(convertByDescription);		
		return books.flatMap(searchSuccess).onErrorResume(searchFallback);
	}
	
	 
	public Mono<ServerResponse> searchByTags(ServerRequest request) {
			
		final Mono<BookSearch> toto = request
				.bodyToMono(BookSearch.class);
		Mono<Flux<Book>> books = toto.flatMap(convertByTags);		
		return books.flatMap(searchSuccess).onErrorResume(searchFallback);
	}
		
	
	private Mono<Flux<Book>> getBooksByCategory(String categorySlug, String sortBy) {
		
		try {
			return Mono.just(bookService.fluxBooksByCategory(categorySlug, sortBy));
		} catch (Exception e) {
			return Mono.error(new RuntimeException("SATOR"));
		}
	}
	
	private Mono<Flux<Book>> getBooksBoughtWith(String bookId, int outLimit) {
		
		try {
			return Mono.just(bookService.getFluxBooksBoughtWith(bookId, outLimit));
		} catch (IOException e) {
			return Mono.error(new RuntimeException("SATOR"));
		}
		
	}
	

	Function<String, Mono<Book>> transformBySlug = 
			s -> {
				try {			
					return bookService.getBookBySlug(s);
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException("SATOR");
				}
	};
	
	Function<String, Mono<Book>> transformById = 
			s -> {
				try {			
					return bookService.getBookById(s);
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException("SATOR");
				}
	};
		
	Function<Book, Mono<ServerResponse>> bookSuccess =
			s -> {
					return ServerResponse.ok()
								.contentType(MediaType.APPLICATION_JSON)
								.body(Mono.just(s), Book.class);
	};		
	
	Function<Throwable, Mono<ServerResponse>> bookFallback =
			e -> {
					if (e.getClass() == BookNotFoundException.class) {
						System.err.println("LOREM IPSUM");
						return ServerResponse
								.status(HttpStatus.NOT_FOUND)
								.build();
					} else {
						return ServerResponse
								.status(HttpStatus.INTERNAL_SERVER_ERROR)
								.build();			
					}
	};
		
			
	Function<BookSearch, Mono<Flux<Book>>> convertByTitle =
					s -> {
						try {
							return Mono.just(searchService.fluxSearchByTitle(s.getSearchString()));
						} catch (IOException e) {
							// catch IOException here and return a custom exception wrapped in a Mono
							return Mono.error(new RuntimeException("SATOR"));
						}
	};		
		
	Function<BookSearch, Mono<Flux<Book>>> convertByDescription =
			s -> {
				try {
					return Mono.just(searchService.fluxSearchByDescription(s.getSearchString()));
				} catch (IOException e) {
					// catch IOException here and return a custom exception wrapped in a Mono
					return Mono.error(new RuntimeException("SATOR"));
				}
	};	
			
	Function<BookSearch, Mono<Flux<Book>>> convertByTags =
			s -> {
				try {
					return Mono.just(searchService.fluxSearchByTags(s.getSearchString()));
				} catch (IOException e) {
					// catch IOException here and return a custom exception wrapped in a Mono
					return Mono.error(new RuntimeException("SATOR"));
				}
	};				
		
	Function<Throwable, Mono<ServerResponse>> searchFallback = 
			s -> {
					return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.build();
	};
	
	Function<Flux<Book>, Mono<ServerResponse>> searchSuccess = 
			s -> {
					return ServerResponse.ok()
							.contentType(MediaType.APPLICATION_JSON)
							.body(s, Book.class);
	};

}
