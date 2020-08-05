package com.dub.gutenberg.web;

import java.io.IOException;
import java.text.ParseException;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.dub.gutenberg.domain.Book;
import com.dub.gutenberg.domain.EditCart;
import com.dub.gutenberg.domain.Order;
import com.dub.gutenberg.domain.OrderAndBook;
import com.dub.gutenberg.domain.OrderAndState;
import com.dub.gutenberg.domain.UserAndReviewedBooks;
import com.dub.gutenberg.exceptions.OrderNotFoundException;
import com.dub.gutenberg.services.OrderService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class OrderHandler {
	
	@Autowired 
	private OrderService orderService;
	

	public Mono<ServerResponse> getOrderById(ServerRequest request) {
				
		final Mono<Order> upOrder = this.transformById(request.pathVariable("orderId"));
		
		return upOrder
				.flatMap(orderSuccess)
				.onErrorResume(orderFallback);
	}
	
	
	public Mono<ServerResponse> createOrder(ServerRequest request) {
		
		
		final Mono<Order> order = request
				.bodyToMono(Order.class);
				
		
		return order
			.flatMap(transformCreate)
			.flatMap(orderSuccess)
			.onErrorResume(orderFallback);	
	}
	
	
	public Mono<ServerResponse> editCart(ServerRequest request) {
		
		
		final Mono<EditCart> toto = request.bodyToMono(EditCart.class);
				
		return toto
				.flatMap(transformEditCart)
				.flatMap(orderSuccess)
				.onErrorResume(orderFallback);
	}
	
	
	// activeOrder may be null initially
	public Mono<ServerResponse> getActiveOrder(ServerRequest request) {
			
		
		final Mono<String> toto = request.bodyToMono(String.class);
		
		return toto
				.flatMap(transformGetActiveOrder)
				.flatMap(orderSuccess)
				.onErrorResume(activeOrderFallback);
	}
	
	
	public Mono<ServerResponse> setOrderState(ServerRequest request) {
		
		
		final Mono<OrderAndState> toto = request.bodyToMono(OrderAndState.class);
		
		return toto
				.flatMap(transformSetOrderState)
				.flatMap(orderSuccess)
				.onErrorResume(orderFallback);
	}
	
	
	public Mono<ServerResponse> checkoutOrder(ServerRequest request) {
		
		final Mono<String> toto = request.bodyToMono(String.class);
		
		return toto
				.flatMap(transformCheckoutOrder)
				.flatMap(orderSuccess)
				.onErrorResume(orderFallback);
	}
	
	
	public Mono<ServerResponse> addBookToOrder(ServerRequest request) {
				
		final Mono<OrderAndBook> toto = request.bodyToMono(OrderAndBook.class);
		
		return toto
				.flatMap(transformAddBookToOrder)
				.flatMap(orderSuccess)
				.onErrorResume(orderFallback);
	}
	
	
	public Mono<ServerResponse> updateOrder(ServerRequest request) {
		
		final Mono<Order> toto = request
									.bodyToMono(Order.class);
		return toto
				.flatMap(transformUpdateOrder)
				.flatMap(orderSuccess)
				.onErrorResume(orderFallback); 
	}
	
	
	public Mono<ServerResponse> getBooksNotReviewed(ServerRequest request) {
		
		final Mono<UserAndReviewedBooks> toto = request
				.bodyToMono(UserAndReviewedBooks.class);
		
		Mono<Flux<String>> bookIds = toto.flatMap(transformBooksNotReviewed);
		
		return bookIds
				.flatMap(notReviewedSuccess)
				.onErrorResume(orderFallback);
	}
	
	
	// All utility methods
	
	private Function<UserAndReviewedBooks, Mono<Flux<String>>> transformBooksNotReviewed =
			s -> {
				try {
					return Mono.just(orderService.getBooksNotReviewed(s));
				
				} catch (IOException | ParseException  e) {
					e.printStackTrace();
					return Mono.error(new RuntimeException("SATOR"));
				}
			};

			
	
	private Function<OrderAndBook, Mono<Order>> transformAddBookToOrder =
		s -> {
			try {
				return orderService.addBookToOrder(s.getOrderId(), s.getBookId());
			} catch (Exception e) {
				return Mono.error(new RuntimeException("SATOR"));
			}
		};	
			
	
	private Function<String, Mono<Order>> transformCheckoutOrder =
			s -> {
				try {
					return orderService.checkoutOrder(s);
				} catch (IOException e) {
					return Mono.error(new RuntimeException("SATOR"));
				}
			};
	
	
	private Function<OrderAndState, Mono<Order>> transformSetOrderState =
			s -> {
				try {
					return orderService.setOrderState(s.getOrderId(), s.getState());			
				} catch (IOException e) {
					return Mono.error(new RuntimeException("SATOR"));
				}
			};
	
	
	private Function<Order, Mono<Order>> transformCreate =
			order -> {
				try {
					return orderService.saveOrder(order, true);		
				} catch (IOException e) {
					e.printStackTrace();
					return Mono.error(new RuntimeException("SATOR"));				
				}
	};
	
	
	private Function<EditCart, Mono<Order>> transformEditCart =
			editCart -> {
				try {
					return orderService.editCart(editCart);
				} catch (IOException e) {	
					return Mono.error(new RuntimeException());
				}
	};
	
	
	private Function<String, Mono<Order>> transformGetActiveOrder =
			userId -> {
				try {
					// may be null after a payment
					return orderService.getActiveOrder(userId);
				} catch (OrderNotFoundException e) {
					return Mono.error(e);
				} catch (IOException e) {
					return Mono.error(new RuntimeException("SATOR"));
				}
	};
	
	
	private Function<Order, Mono<Order>> transformUpdateOrder =
			order -> {
				try {
					return orderService.saveOrder(order, false);
				} catch (IOException e) {
					return Mono.error(new RuntimeException("SATOR"));
				}
			};
	
			
	private Mono<Order> transformById(String orderId) {
		try {
			return orderService.getOrderById(orderId);
		} catch (IOException e) {
			return Mono.error(new RuntimeException());
		}
	}
	
	
	private Function<Throwable, Mono<ServerResponse>> orderFallback =
			error -> {
				return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)				
						.build();	  			
	};
	
	
	private Function<Order, Mono<ServerResponse>> orderSuccess =
			order -> {
				return ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(Mono.just(order), Order.class);	  			
	};
	
	
	private Function<Throwable, Mono<ServerResponse>> activeOrderFallback =
			error -> {
				if (OrderNotFoundException.class == error.getClass()) {
					
					
					
					return ServerResponse.status(HttpStatus.NOT_FOUND)
							.build();
				} else {
					
					
				
					return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.build();
				}
						
	};

	
	private Function<Flux<String>, Mono<ServerResponse>> notReviewedSuccess = 
			s -> {
					return ServerResponse.ok()
							.contentType(MediaType.TEXT_EVENT_STREAM)
							.body(s, String.class);
	};
	

}
