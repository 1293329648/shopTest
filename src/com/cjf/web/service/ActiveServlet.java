package com.cjf.web.service;

import java.io.IOException;

import javax.jws.soap.SOAPBinding.Use;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cjf.service.UserService;
import com.cjf.service.Impl.UserServiceImpl;


public class ActiveServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public ActiveServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
		   String activeCode = request.getParameter("activeCode");
		   UserService userService=new UserServiceImpl();
		   userService.active(activeCode);		
			//跳转到登录页面
			response.sendRedirect(request.getContextPath()+"/login.jsp");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
