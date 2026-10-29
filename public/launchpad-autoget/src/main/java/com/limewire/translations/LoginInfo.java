package com.limewire.translations;

/**
 * Holds the information to pass around for logging in.
 */
final class LoginInfo {
	
	final String username;
  final String password;
  final String email;
  final String passwordEmail;
  final String server;
  final String group;
  
  LoginInfo(final String username, final String password, final String email,
            final String passwordEmail, final String server, final String group) {
  	this.username = username;
  	this.password = password;
  	this.email = email;
  	this.passwordEmail = passwordEmail;
  	this.server = server;
  	this.group = group;
  }  	
}