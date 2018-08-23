package com.cjf.web.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

import com.cjf.entity.Category;
import com.cjf.entity.Product;
import com.cjf.service.ProductService;
import com.cjf.service.Impl.ProductServiceImpl;
import com.cjf.utils.CommonsUtils;


public class AdminAddproductServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		Product product = new Product();
		
		//收集数据的容器
		Map<String,Object> map = new HashMap<String,Object>();
		
		try {
			//创建磁盘文件项工厂
			DiskFileItemFactory factory = new DiskFileItemFactory();
			//创建文件上传核心对象
			ServletFileUpload upload = new ServletFileUpload(factory);
			//解析request获得文件项对象集合
			List<FileItem> parseRequest = upload.parseRequest(request);
			for(FileItem item : parseRequest){
				//判断是否是普通表单项
				boolean formField = item.isFormField();
				if(formField){
					//普通表单项 获得表单的数据 封装到Product实体中
					String fieldName = item.getFieldName();
					String fieldValue = item.getString("UTF-8");					
					map.put(fieldName, fieldValue);					
				}else{
					//文件上传项 获得文件名称 获得文件的内容
					String fileName = item.getName();					
				/*	String path = this.getServletContext().getRealPath("/images");*/					
					InputStream in = item.getInputStream();//获得文件额输入流
					//拷贝到本地的文件的输出流
					OutputStream out = new FileOutputStream("D:/Tomcat/upload"+"/"+fileName);  
					IOUtils.copy(in, out);
					in.close();
					out.close();
					item.delete();					
					map.put("pimage", fileName);					
				}				
			}
			
			BeanUtils.populate(product, map);
			//是否product对象封装数据完全
			//private String pid;
			product.setPid(CommonsUtils.getUUID());
			//private Date pdate;
			product.setPdate(new Date());
			//private int pflag;
			product.setPflag(0);
			//private Category category;
			Category category = new Category();
			category.setCid(map.get("cid").toString());
			product.setCategory(category);
			
			//将product传递给service层
	       ProductService productService=new ProductServiceImpl();
			productService.addProduct(product);
			
			response.sendRedirect(request.getContextPath()+"/admin?method=queryAllProduct");
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		doGet(request, response);
	}

}
