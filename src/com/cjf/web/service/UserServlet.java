package com.cjf.web.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.cjf.entity.User;
import com.cjf.service.UserService;
import com.cjf.service.Impl.UserServiceImpl;

public class UserServlet extends BaseServlet{
	private static final long serialVersionUID = 1L;

	//用户注销
	public void quickUser(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		
		HttpSession session = request.getSession();
		//从session中将user删除
		session.removeAttribute("user");
		
		//将存储在客户端的cookie删除掉
		Cookie cookie_username = new Cookie("cookie_username","");
		cookie_username.setMaxAge(0);
		//创建存储密码的cookie
		Cookie cookie_password = new Cookie("cookie_password","");
		cookie_password.setMaxAge(0);

		response.addCookie(cookie_username);
		response.addCookie(cookie_password);
		
		response.sendRedirect(request.getContextPath()+"/login.jsp");		
	}
	
    //用户登录
	public void login(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		        HttpSession session = request.getSession();
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		UserService userService = new UserServiceImpl();
	          
		User user = userService.login(username, password);
		if(user!=null){
			//登录成功
			//判断用户是否勾选自动登录
			String autoLogin = request.getParameter("autoLogin");
			if(autoLogin!=null){				
				//编码
				String username_code = URLEncoder.encode(username, "UTF-8");			
				Cookie cookie_username = new Cookie("cookie_username",username_code);
				Cookie cookie_password = new Cookie("cookie_password",password);
				//设置cookie的持久化时间
				cookie_username.setMaxAge(60*60);
				cookie_password.setMaxAge(60*60);
				//设置cookie的携带路径
				cookie_username.setPath(request.getContextPath());
				cookie_password.setPath(request.getContextPath());
				//发送cookie
				response.addCookie(cookie_username);
				response.addCookie(cookie_password);
			}
			
			//将登录的用户的user对象存到session中
			session.setAttribute("user", user);
			//重定向到首页
			response.sendRedirect(request.getContextPath());
			
		}else {
			//失败 转发到登录页面 提出提示信息
			request.setAttribute("loginInfo", "用户名或密码错误");
			request.getRequestDispatcher("/login.jsp").forward(request, response);
		}
	}

}
