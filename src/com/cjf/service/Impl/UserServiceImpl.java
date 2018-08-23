package com.cjf.service.Impl;

import java.sql.SQLException;

import com.cjf.dao.UserDao;
import com.cjf.dao.Impl.UserDaoImpl;
import com.cjf.entity.User;
import com.cjf.service.UserService;
import com.sun.org.apache.bcel.internal.generic.DALOAD;

public class UserServiceImpl implements UserService {
     UserDao userdao=new UserDaoImpl();
	 
	@Override
	public User login(String username,String password) {
		
		User user = null;
	     try {
		       user= userdao.login(username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}	     
		return user;		
	}

	@Override
	public boolean regist(User user) {
		
		UserDao dao = new UserDaoImpl();
		int row = 0;
		try {
			row = dao.regist(user);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return row>0?true:false;
	}

	@Override
	public void active(String activeCode) {		
		try {
			userdao.active(activeCode);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
