package com.cjf.dao.Impl;

import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;

import com.cjf.dao.UserDao;
import com.cjf.entity.User;
import com.cjf.utils.DataSourceUtils;

public class UserDaoImpl implements UserDao{

		public User login(String username, String password) throws SQLException {
			
			QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
			String sql = "select * from user where username=? and password=?";
			return runner.query(sql, new BeanHandler<User>(User.class), username,password);
			
		}

		public int regist(User user) throws SQLException {
			QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
			String sql = "insert into user values(?,?,?,?,?,?,?,?,?,?)";
			int update = runner.update(sql, user.getUid(),user.getUsername(),user.getPassword(),
					user.getName(),user.getEmail(),user.getTelephone(),user.getBirthday(),
					user.getSex(),user.getState(),user.getCode());
			return update;
		}

		@Override
		public void active(String activeCode) throws SQLException {

			QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());			
			String sql="update user set state=? where code=?";			
			runner.update(sql,1,activeCode);
			
		}
	


}
