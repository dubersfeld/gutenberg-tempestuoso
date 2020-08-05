package com.dub.gutenberg;

import java.io.IOException;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.dub.gutenberg.domain.AddressOperations;
import com.dub.gutenberg.domain.MyUser;
import com.dub.gutenberg.domain.PaymentOperations;
import com.dub.gutenberg.domain.Primary;
import com.dub.gutenberg.exceptions.DuplicateUserException;
import com.dub.gutenberg.exceptions.UserNotFoundException;
import com.dub.gutenberg.services.UserService;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

/**
 * Never use try-catch, always use onErrorResume 
 * */

@Component
public class UserHandler {
	
	@Autowired
	UserService userService;
	
	@Value("${baseUsersUrl}")
	private String baseUsersUrl; 
	
	
	public Mono<ServerResponse> createUser(ServerRequest request) {
		
	
		
		// new candidate user
		Mono<MyUser> user = request.bodyToMono(MyUser.class);	
		Mono<String> newUser = user.flatMap(transformCreateUser);
		Mono<String> location = newUser.flatMap(s -> Mono.just(baseUsersUrl + s));
			
		return location
				.flatMap(createUserSuccess)
				.onErrorResume(createUserFallback);
	}
	
	
	public Mono<ServerResponse> primaryAddress(ServerRequest request) {
		
	
		
		Mono<Primary> primary = request.bodyToMono(Primary.class);
		
		// here I use Mono.zip method
		Mono<Tuple2<MyUser, Integer>> tuple = primary.flatMap(transformSetPrimaryAddress);
		
		Mono<MyUser> user = tuple.flatMap(transformSetPrimaryAddress2);
	
		return user
				.flatMap(primaryAddressSuccess)
				.onErrorResume(primaryAddressFallback);	
	}
	
	
	public Mono<ServerResponse> primaryPayment(ServerRequest request) {
		
		
		
		Mono<Primary> primary = request.bodyToMono(Primary.class);
		
		// here I use Mono.zip method
		Mono<Tuple2<MyUser, Integer>> tuple = primary.flatMap(transformSetPrimaryPayment);
		
		Mono<MyUser> user = tuple.flatMap(transformSetPrimaryPayment2);
	
		return user
				.flatMap(primaryPaymentSuccess)
				.onErrorResume(primaryPaymentFallback);
	}
			
	
	public Mono<ServerResponse> addAddress(ServerRequest request) {
		
		
		return request
				.bodyToMono(AddressOperations.class)
				.flatMap(transformAddAddress)
				.flatMap(userSuccess)
				.onErrorResume(userFallback);
	}
	
	
	public Mono<ServerResponse> addPaymentMethod(ServerRequest request) {
		
		
		
		return request
				.bodyToMono(PaymentOperations.class)
				.flatMap(transformAddPaymentMethod)	
				.flatMap(userSuccess)
				.onErrorResume(userFallback);
	}
	
	
	public Mono<ServerResponse> deleteAddress(ServerRequest request) {
		
	
		
		return request
				.bodyToMono(AddressOperations.class)
				.flatMap(transformDeleteAddress)	
				.flatMap(userSuccess)
				.onErrorResume(userFallback);
	}
	

	public Mono<ServerResponse> deletePaymentMethod(ServerRequest request) {
		
		
			
		return request
				.bodyToMono(PaymentOperations.class)
				.flatMap(transformDeletePaymentMethod)
				.flatMap(userSuccess)
				.onErrorResume(userFallback);
	}
	
	
	public Mono<ServerResponse> findById(ServerRequest request) {
		
		
		
		return request
				.bodyToMono(String.class)
				.flatMap(transformById)
				.flatMap(userSuccess)
				.onErrorResume(userFallback);
	}
	
	
	public Mono<ServerResponse> getUserByName(ServerRequest request) {
		Mono<String> username = Mono.just(request.pathVariable("username"));
		
		
		return username
				.flatMap(transformByUsername)
				.flatMap(userSuccess)
				.onErrorResume(userFallback);
	}


	// all utility functions
	
	Function<AddressOperations, Mono<MyUser>> transformAddAddress = 		
			s -> {
				try {			
					return userService.monoAddAddress(s.getUserId(), s.getAddress());
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException("SATOR");
				}
	};
			
	Function<PaymentOperations, Mono<MyUser>> transformAddPaymentMethod = 
			s -> {
				try {			
					return userService.monoAddPaymentMethod(s.getUserId(), s.getPaymentMethod());
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException("SATOR");
				}
	};
	
	Function<AddressOperations, Mono<MyUser>> transformDeleteAddress = 
			s -> {
				try {	
					System.out.println("FUTRE " + s.getUserId());
					return userService.monoDeleteAddress(s.getUserId(), s.getAddress());
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException("SATOR");
				}
	};
	
	Function<PaymentOperations, Mono<MyUser>> transformDeletePaymentMethod = 
			s -> {
				try {			
					return userService.monoDeletePaymentMethod(s.getUserId(), s.getPaymentMethod());
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException("SATOR");
				}
	};
	
	Function<String, Mono<MyUser>> transformById = 
			s -> {
				try {			
					return userService.monoFindById(s);
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException("SATOR");
				}
	};
	
	Function<String, Mono<MyUser>> transformByUsername = 
			s -> {
				try {			
					return userService.monoFindByUsername(s);
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException("SATOR");
				}
	};
					
	Function<Primary, Mono<Tuple2<MyUser,Integer>>> transformSetPrimaryPayment =
			s -> {
					System.err.println("FORGE " + s.getUsername());
					try {
							Mono<MyUser> enclume = userService.monoFindByUsername(s.getUsername());
							Mono<Integer> index = Mono.just(s.getIndex());
							return Mono.zip(enclume, index);
					} catch (IOException e) {
							e.printStackTrace();
							throw new RuntimeException("SATOR");
						}
					};		
	
				
	Function<Tuple2<MyUser, Integer>, Mono<MyUser>> transformSetPrimaryPayment2 =
			s -> {
					String userId = s.getT1().getId();
					int index = s.getT2();
						
					try {
						return userService.monoSetPrimaryPayment(userId, index);
					} catch (IOException e) {
						e.printStackTrace();
						throw new RuntimeException("SATOR");
					}		
	};
								
	Function<Primary, Mono<Tuple2<MyUser,Integer>>> transformSetPrimaryAddress =
			s -> {
					System.err.println("FORGE " + s.getUsername());
					try {
						Mono<MyUser> enclume = userService.monoFindByUsername(s.getUsername());
						Mono<Integer> index = Mono.just(s.getIndex());
						return Mono.zip(enclume, index);
					} catch (IOException e) {
						e.printStackTrace();
						throw new RuntimeException("SATOR");
					}
	};		
					
	Function<Tuple2<MyUser, Integer>, Mono<MyUser>> transformSetPrimaryAddress2 =
			s -> {
					String userId = s.getT1().getId();
					int index = s.getT2();
									
					try {
						return userService.monoSetPrimaryAddress(userId, index);
					} catch (IOException e) {
						e.printStackTrace();
						throw new RuntimeException("SATOR");
					}		
	};
											
	Function<MyUser, Mono<String>> transformCreateUser =
			s -> {
					try {
						return userService.monoCreateUser(s);
					} catch (IOException e) {
						throw new RuntimeException("SATOR");
					}
	};
			
	Function<Throwable, Mono<ServerResponse>> fallback = 
			e -> {
				System.err.println("Error " + e);
				return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();			
	};
							
	Function<String, Mono<ServerResponse>> createUserSuccess =
			s -> {
				return ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(Mono.just(s), String.class);
	};
			
	Function<Throwable, Mono<ServerResponse>> createUserFallback =
			e -> {
					if (e.getClass() == DuplicateUserException.class) {
						System.err.println("LOREM IPSUM");
						return ServerResponse
									.status(HttpStatus.CONFLICT)
									.build();
					} else {
						return ServerResponse
								.status(HttpStatus.INTERNAL_SERVER_ERROR)
								.build();			
					}
	};
							
	Function<MyUser, Mono<ServerResponse>> primaryAddressSuccess =
			s -> {
					return ServerResponse.ok()
								.contentType(MediaType.APPLICATION_JSON)
								.body(Mono.just(s), MyUser.class);
	};		
	
	Function<Throwable, Mono<ServerResponse>> primaryAddressFallback =
			e -> {
					if (e.getClass() == UserNotFoundException.class) {
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
	
	Function<MyUser, Mono<ServerResponse>> primaryPaymentSuccess =
			s -> {
					return ServerResponse.ok()
								.contentType(MediaType.APPLICATION_JSON)
								.body(Mono.just(s), MyUser.class);
	};		
	
	Function<Throwable, Mono<ServerResponse>> primaryPaymentFallback =
			e -> {
					if (e.getClass() == UserNotFoundException.class) {
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
	
	Function<MyUser, Mono<ServerResponse>> userSuccess =
			s -> {
					return ServerResponse.ok()
								.contentType(MediaType.APPLICATION_JSON)
								.body(Mono.just(s), MyUser.class);
	};		
	
	Function<Throwable, Mono<ServerResponse>> userFallback =
			e -> {
					if (e.getClass() == UserNotFoundException.class) {
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
	
	/** reduce code duplication later here */
			
}
