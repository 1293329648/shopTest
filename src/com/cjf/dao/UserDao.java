package com.cjf.dao;

import java.sql.SQLException;

import com.cjf.entity.User;

public interface UserDao {

	public User login(String username,String password) throws SQLException;

	public int regist(User user) throws SQLException;

	public void active(String activeCode) throws SQLException;
	
	
}
