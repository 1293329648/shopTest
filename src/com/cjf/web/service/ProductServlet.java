package com.cjf.web.service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;

import com.cjf.entity.Cart;
import com.cjf.entity.CartItem;
import com.cjf.entity.Category;
import com.cjf.entity.Order;
import com.cjf.entity.OrderItem;
import com.cjf.entity.PageBean;
import com.cjf.entity.Product;
import com.cjf.entity.User;
import com.cjf.service.ProductService;
import com.cjf.service.Impl.ProductServiceImpl;
import com.cjf.utils.CommonsUtils;
import com.cjf.utils.JedisPoolUtils;
import com.cjf.utils.PaymentUtil;
import com.google.gson.Gson;

import redis.clients.jedis.Jedis;

public class ProductServlet extends BaseServlet {

	private static final long serialVersionUID = 1L;
	

	// 我的订单
	public void myOrder(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		// 判断用户是否登陆
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");

		if (user == null) {
			response.sendRedirect(request.getContextPath() + "/login.jsp");
			return;
		}
		ProductService productService = new ProductServiceImpl();
		List<Order> orderList = productService.findOrderListByUid(user.getUid());
		if (orderList != null) {
			for (Order order : orderList) {
				// 获得每一个订单的订单号
				String oid = order.getOid();
				// 根据订单号查询所有订单项
				List<Map<String, Object>> mapList = productService.findAllOrderItemByOid(oid);
				// 将mapList转换成List<OrderItem> orderItems
				for (Map<String, Object> map : mapList) {
					OrderItem orderItem = new OrderItem();
					try {
						// 封装每一个订单项信息
						BeanUtils.populate(orderItem, map);
						Product product = new Product();
						
						BeanUtils.populate(product, map);
						orderItem.setProduct(product);// 订单项加入product
						// 再将orderItem 封装的 order 中
						order.getOrderItems().add(orderItem);

					} catch (IllegalAccessException | InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}		
		//orderList封装完整了
		request.setAttribute("orderList", orderList);
		
		request.getRequestDispatcher("/order_list.jsp").forward(request, response);		
	}

	// 确认订单---更新收获人信息+在线支付
	public void confirmOrder(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 1、更新收货人信息
		Map<String, String[]> properties = request.getParameterMap();
		Order order = new Order();

		try {
			BeanUtils.populate(order, properties);
		} catch (IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ProductService service = new ProductServiceImpl();
		// 更新收货地址
		service.updateOrderAdrr(order);
		// 2、在线支付
		/*
		 * if(pd_FrpId.equals("ABC-NET-B2C")){ //介入农行的接口 }else
		 * if(pd_FrpId.equals("ICBC-NET-B2C")){ //接入工行的接口 }
		 */
		// .......
		// 只接入一个接口，这个接口已经集成所有的银行接口了 ，这个接口是第三方支付平台提供的
		// 接入的是易宝支付
		// 获得 支付必须基本数据
		String orderid = request.getParameter("oid");
		// String money = order.getTotal()+"";//支付金额
		String money = "0.01";// 支付金额
		// 银行
		String pd_FrpId = request.getParameter("pd_FrpId");

		// 发给支付公司需要哪些数据
		String p0_Cmd = "Buy";
		String p1_MerId = ResourceBundle.getBundle("merchantInfo").getString("p1_MerId");
		String p2_Order = orderid;
		String p3_Amt = money;
		String p4_Cur = "CNY";
		String p5_Pid = "";
		String p6_Pcat = "";
		String p7_Pdesc = "";
		// 支付成功回调地址 ---- 第三方支付公司会访问、用户访问
		// 第三方支付可以访问网址
		String p8_Url = ResourceBundle.getBundle("merchantInfo").getString("callback");
		String p9_SAF = "";
		String pa_MP = "";
		String pr_NeedResponse = "1";
		// 加密hmac 需要密钥
		String keyValue = ResourceBundle.getBundle("merchantInfo").getString("keyValue");
		String hmac = PaymentUtil.buildHmac(p0_Cmd, p1_MerId, p2_Order, p3_Amt, p4_Cur, p5_Pid, p6_Pcat, p7_Pdesc,
				p8_Url, p9_SAF, pa_MP, pd_FrpId, pr_NeedResponse, keyValue);
		String url = "https://www.yeepay.com/app-merchant-proxy/node?pd_FrpId=" + pd_FrpId + "&p0_Cmd=" + p0_Cmd
				+ "&p1_MerId=" + p1_MerId + "&p2_Order=" + p2_Order + "&p3_Amt=" + p3_Amt + "&p4_Cur=" + p4_Cur
				+ "&p5_Pid=" + p5_Pid + "&p6_Pcat=" + p6_Pcat + "&p7_Pdesc=" + p7_Pdesc + "&p8_Url=" + p8_Url
				+ "&p9_SAF=" + p9_SAF + "&pa_MP=" + pa_MP + "&pr_NeedResponse=" + pr_NeedResponse + "&hmac=" + hmac;

		// 重定向到第三方支付平台
		response.sendRedirect(url);
	}

	// 提交订单
	public void submitOrder(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 判断用户是否登陆
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");

		if (user == null) {
			response.sendRedirect(request.getContextPath() + "/login.jsp");
			return;
		}
		Order order = new Order();
		order.setOid(CommonsUtils.getUUID());// private String oid;//该订单的订单号
		order.setOrdertime(new Date()); // private Date ordertime;//下单时间
		// 获得购物车
		Cart cart = (Cart) session.getAttribute("cart");
		order.setTotal(cart.getTotal()); // private double total;//该订单的总金额
		order.setState(0);// private int state;//订单支付状态 1代表已付款 0代表未付款
		order.setAddress(null); // private String address;//收货地址
		order.setName(user.getName()); // private String name;//收货人
		order.setTelephone(null); // private String telephone;//收货人电话
		order.setUser(user); // private User user;//该订单属于哪个用户

		Map<String, CartItem> cartItems = cart.getCartItems();
		for (Map.Entry<String, CartItem> entry : cartItems.entrySet()) {
			// 获得每一个购物项的值
			CartItem cartItem = entry.getValue();
			OrderItem orderItem = new OrderItem();
			orderItem.setItemid(CommonsUtils.getUUID()); // private String itemid;//订单项的id
			orderItem.setCount(cartItem.getBuyNum()); // private int count;//订单项内商品的购买数量
			orderItem.setSubtotal(cartItem.getSubtotal());// private double subtotal;//订单项小计
			orderItem.setProduct(cartItem.getProduct());// private Product product;//订单项内部的商品
			orderItem.setOrder(order);// private Order order;//该订单项属于哪个订单

			order.getOrderItems().add(orderItem); // 该订单中有多少订单项 // List<OrderItem> orderItems = new
													// ArrayList<OrderItem>();
		}

		ProductService service = new ProductServiceImpl();
		service.submitOrder(order);

		session.setAttribute("order", order);
		// 页面跳转
		response.sendRedirect(request.getContextPath() + "/order_info.jsp");
	}

	// 清空购物车
	public void clearCart(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		session.removeAttribute("cart");

		// 跳转回cart.jsp
		response.sendRedirect(request.getContextPath() + "/cart.jsp");

	}

	public void delProFromCart(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String pid = request.getParameter("pid");
		HttpSession session = request.getSession();
		Cart cart = (Cart) session.getAttribute("cart");

		if (cart != null) {
			Map<String, CartItem> cartItems = cart.getCartItems();
			// 修改 总价
			cart.setTotal(cart.getTotal() - cartItems.get(pid).getSubtotal());
			cartItems.remove(pid);
			cart.setCartItems(cartItems);
		}
		session.setAttribute("cart", cart);
		// 跳转回cart.jsp
		response.sendRedirect(request.getContextPath() + "/cart.jsp");
	}

	// 将商品添加到购物车
	public void addProductToCart(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {

		HttpSession session = request.getSession();

		ProductService service = new ProductServiceImpl();

		String pid = request.getParameter("pid");

		Integer integer = new Integer(request.getParameter("buyNum"));
		int buyNum = integer.intValue();

		// 获得商品
		Product product = service.findInfo(pid);
		double subtotal = product.getShop_price() * buyNum * 1.0;
		// 封装购物项
		CartItem item = new CartItem();
		item.setBuyNum(buyNum);
		item.setProduct(product);
		item.setSubtotal(subtotal);

		// 从session 域里取
		Cart cart = (Cart) session.getAttribute("cart");
		if (cart == null) {
			cart = new Cart();
		}
		Map<String, CartItem> cartItems = cart.getCartItems();

		// 定义一个新的总计
		double newsubtotal = 0.0;

		if (cartItems.containsKey(pid)) {
			// 获得当前购物项 已经封装好了
			CartItem cartItem = cartItems.get(pid);
			int oldBuyNum = cartItem.getBuyNum();
			oldBuyNum += buyNum;
			cartItem.setBuyNum(oldBuyNum);
			cart.setCartItems(cartItems);
			// 修改小计
			newsubtotal = oldBuyNum * product.getMarket_price(); // 修改了 要重新set
			cartItem.setSubtotal(newsubtotal);
		} else {
			// 把该商品放进去
			cart.getCartItems().put(product.getPid(), item);
			// 小计
			newsubtotal = buyNum * product.getShop_price();
		}
		// 更新总价
		double total = cart.getTotal() + newsubtotal;
		cart.setTotal(total);

		// 将车再次访问session
		session.setAttribute("cart", cart);
		// 直接跳转到购物车页面
		response.sendRedirect(request.getContextPath() + "/cart.jsp");
	}

	public void index(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		ProductService productService = new ProductServiceImpl();

		try {

			List<Product> hotProductList = productService.findHotProductList();

			List<Product> newProductList = productService.findNewProductList();

			request.setAttribute("hotProductList", hotProductList);
			request.setAttribute("newProductList", newProductList);

			request.getRequestDispatcher("/index.jsp").forward(request, response);

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void categoryList(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		ProductService service = new ProductServiceImpl();

		Jedis jedis = JedisPoolUtils.getJedis();
		String categoryListJson = jedis.get("categoryListJson");
		// 2、判断categoryListJson是否为空
		if (categoryListJson == null) {
			System.out.println("缓存没有数据 查询数据库");
			// 准备分类数据
			List<Category> categoryList = null;
			try {
				categoryList = service.findCategoryList();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			Gson gson = new Gson();
			categoryListJson = gson.toJson(categoryList);
			jedis.set("categoryListJson", categoryListJson);
		}
		response.setContentType("text/html;charset=UTF-8");
		response.getWriter().write(categoryListJson);
	}

	public void productListByCid(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		ProductService productService = new ProductServiceImpl();

		String cid = request.getParameter("cid");
		String currentPageStr = request.getParameter("currentPage");// 获得传过来的当前页
		if (currentPageStr == null)
			currentPageStr = "1";
		int currentPage = Integer.parseInt(currentPageStr);
		int currentCount = 12;
		PageBean pageBean = null;
		try {
			pageBean = productService.findProductBycid(cid, currentPage, currentCount);
			request.setAttribute("pageBean", pageBean);
			request.setAttribute("cid", cid);

			List<Product> historyProduct = new ArrayList<Product>();
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("pids".equals(cookie.getName())) {
						String pids = cookie.getValue();
						String[] split = pids.split("-");
						for (String pid : split) {
							Product product = productService.findInfo(pid);
							historyProduct.add(product);
						}
					}
				}
			}

			request.setAttribute("historyProduct", historyProduct);

			request.getRequestDispatcher("/product_list.jsp").forward(request, response);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void productInfo(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 获得当前页
		String currentPage = request.getParameter("currentPage");
		// 获得商品类别
		String cid = request.getParameter("cid");
		// 获得要查询的商品的pid
		String pid = request.getParameter("pid");

		ProductService service = new ProductServiceImpl();
		Product product = null;
		try {
			product = service.findInfo(pid);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		request.setAttribute("product", product);
		request.setAttribute("currentPage", currentPage);
		request.setAttribute("cid", cid);

		// 获得第一次访问的pids
		String pids = pid;

		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				// 寻找cookie name 为 pids-
				if ("pids".equals(cookie.getName())) {
					pids = cookie.getValue();
					String[] split = pids.split("-");
					List<String> asList = Arrays.asList(split);
					LinkedList<String> list = new LinkedList<String>(asList);

					if (list.contains(pid)) {
						list.remove(pid);
						list.addFirst(pid);
					} else {
						list.addFirst(pid);
					}

					StringBuffer sbBuffer = new StringBuffer();

					for (int i = 0; i < list.size() && i < 7; i++) {
						sbBuffer.append(list.get(i));
						sbBuffer.append("-");
					}
					// 字符串截取
					pids = sbBuffer.substring(0, sbBuffer.length() - 1);
				}
			}
		}

		Cookie cookie_pids = new Cookie("pids", pids);
		response.addCookie(cookie_pids);

		request.getRequestDispatcher("/product_info.jsp").forward(request, response);

	}

}
