package com.dub.gutenberg.services;

import java.io.IOException;
import java.util.List;

import com.dub.gutenberg.domain.Book;

import reactor.core.publisher.Flux;

public interface SearchService {
	
	//public List<Book> searchByDescription(String searchString) throws IOException;
	//public List<Book> searchByTags(String searchString) throws IOException;
	//public List<Book> searchByTitle(String searchString) throws IOException;

	public Flux<Book> fluxSearchByDescription(String searchString) throws IOException;
	public Flux<Book> fluxSearchByTags(String searchString) throws IOException;
	public Flux<Book> fluxSearchByTitle(String searchString) throws IOException;


}
