package com.dub.gutenberg.services;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.dub.gutenberg.domain.Category;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface CategoryService {

	public List<Category> getAllCategories() throws IOException;	
	
	public Flux<Category> fluxAllCategories() throws IOException;	
	
	//public Category getCategory(String categorySlug) throws IOException;
	
	public Mono<Category> monoCategory(String categorySlug) throws IOException;
}
