package com.cjf.service;

import com.cjf.entity.User;

public interface UserService {
   
	public User login(String username,String password);

	public boolean regist(User user);

	public void active(String activeCode);
	 
}
