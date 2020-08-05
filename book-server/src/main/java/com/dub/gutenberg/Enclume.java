package com.dub.gutenberg;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Enclume {

	public static void main(String[] args) {
		
		List<String> sator = Arrays.asList("sator", "arepo","tenet","opera","rotas");	
		Flux<String> f1 = Flux.fromIterable(sator);
		
		List<String> lorem = Arrays.asList("lorem", "ipsum","coget","morbus","gravis");	
		Flux<String> f2 = Flux.fromIterable(lorem);
	
		//Mono<Flux<String>> mono = Mono.just(f1);
		//Mono<Flux<String>> mono = Mono.error(new RuntimeException());
		Mono<Flux<String>> mono = build();
		
		Mono<List<String>> toto = mono.flatMap(transform)
				.onErrorResume(fallback);
		
		toto.subscribe(t -> t.forEach(System.out::println));
		
	}
	
	static Function<Throwable, Mono<List<String>>> fallback =
			e -> {
					List<String> lorem = Arrays.asList("lorem", "ipsum","coget","morbus","gravis");	
					return Mono.just(lorem);
			};
	
	static Function<Flux<String>, Mono<List<String>>> transform =
			s -> {
				return  s.collectList();
			};
			
	static Mono<Flux<String>> build() {
		try {
			Random random = new Random();
			if (random.nextBoolean()) {
				crash();
			}
			List<String> sator = Arrays.asList("sator", "arepo","tenet","opera","rotas");	
			Flux<String> f1 = Flux.fromIterable(sator);
			return Mono.just(f1);
		} catch (IOException e) {
			return Mono.error(new RuntimeException("SATOR"));
		}
	}	
	
	static void crash() throws IOException {
		throw new IOException();
	}

}
