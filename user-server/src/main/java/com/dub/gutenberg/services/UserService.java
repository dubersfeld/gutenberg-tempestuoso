package com.dub.gutenberg.services;

import java.io.IOException;

import com.dub.gutenberg.domain.Address;
import com.dub.gutenberg.domain.MyUser;
import com.dub.gutenberg.domain.PaymentMethod;

import reactor.core.publisher.Mono;

public interface UserService {
 
	MyUser findById(String userId) throws IOException;	
	Mono<MyUser> monoFindById(String userId) throws IOException;	

	
	MyUser findByUsername(String username) throws IOException;
	Mono<MyUser> monoFindByUsername(String username) throws IOException;
	
	// all profile changes
	MyUser setPrimaryAddress(String userId, int index) throws IOException;		
	Mono<MyUser> monoSetPrimaryAddress(String userId, int index) throws IOException;		
	
	MyUser setPrimaryPayment(String userId, int index) throws IOException;
	Mono<MyUser> monoSetPrimaryPayment(String userId, int index) throws IOException;
	
	MyUser addAddress(String userId, Address newAddress) throws IOException;
	Mono<MyUser> monoAddAddress(String userId, Address newAddress) throws IOException;
	
	MyUser addPaymentMethod(String userId, PaymentMethod newPayment) throws IOException ;
	Mono<MyUser> monoAddPaymentMethod(String userId, PaymentMethod newPayment) throws IOException ;
	
	MyUser deleteAddress(String userId, Address delAddress) throws IOException;
	Mono<MyUser> monoDeleteAddress(String userId, Address delAddress) throws IOException;
	
	MyUser deletePaymentMethod(String userId, PaymentMethod payMeth) throws IOException;
	Mono<MyUser> monoDeletePaymentMethod(String userId, PaymentMethod payMeth) throws IOException;
	
	
	
	// new user registration
	String createUser(MyUser user) throws IOException;
	Mono<String> monoCreateUser(MyUser user) throws IOException;
	 
}