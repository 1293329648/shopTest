package com.cjf.web.service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.beanutils.BeanUtils;
import com.cjf.entity.Category;
import com.cjf.entity.Order;
import com.cjf.entity.Product;
import com.cjf.service.ProductService;
import com.cjf.service.Impl.ProductServiceImpl;
import com.google.gson.Gson;


public class AdminServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;
	

			
	public void  delProduct (HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		//获取要删除的pid
		String pid = request.getParameter("pid");
		
		//传递pid到service层
		ProductService productService=new ProductServiceImpl();
		productService.delProductByPid(pid);
		
		response.sendRedirect(request.getContextPath()+"/admin?method=queryAllProduct");
	}
	
	
	
	//queryAllProduct
	public void  queryAllProduct(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		
		
		//传递请求到service层
	 	ProductService productService=new ProductServiceImpl();
		List<Product> productList = null;
		try {
			productList = productService.findAllProduct();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//将productList放到request域
		request.setAttribute("productList", productList);
		
		request.getRequestDispatcher("/admin/product/list.jsp").forward(request, response);
		
	}
	
	
	
	//findOrderInfoByOid
	public void findOrderInfoByOid(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	            String oid = request.getParameter("oid");
	        	ProductService productService=new ProductServiceImpl();   	        	
	        	List<Map<String,Object>> orderItems= productService.findAllOrderItemByOid(oid);        	
	        	Gson gson =new Gson();        	
	        	String json = gson.toJson(orderItems);
	        	//设置   编码
	        	response.setContentType("text/html;charset=UTF-8");        	
	        	response.getWriter().write(json);
	}
		
	
	public void queryAllOrders(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
	
		ProductService productService=new ProductServiceImpl();
		
		List<Order> orderList = productService.findAllOrder();
		
		request.setAttribute("orderList", orderList);
		
		request.getRequestDispatcher("/admin/order/list.jsp").forward(request, response);
				
	}	
	//异步加载所有分类
	public void findAllCategory(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		ProductService productService=new ProductServiceImpl();
		
		List<Category> categoryList = productService.findCategoryList();
		
		Gson gson=new Gson();
		
		 String json = gson.toJson(categoryList);
		 
		 response.setContentType("text/html;charset=UTF-8");
		 
		 response.getWriter().write(json);
		
		
		
	}

}
