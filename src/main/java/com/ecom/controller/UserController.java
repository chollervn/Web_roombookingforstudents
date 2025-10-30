package com.ecom.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Cart;
import com.ecom.model.Room;
import com.ecom.model.RoomType;
import com.ecom.model.OrderRequest;
import com.ecom.model.RoomOrder;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.OrderService;
import com.ecom.service.RoomService;
import com.ecom.service.RoomTypeService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserService userService;
	@Autowired
	private RoomTypeService roomTypeService;

	@Autowired
	private CartService cartService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private RoomService roomService;


	@GetMapping("/")
	public String home() {
		return "user/home";
	}

	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		if (p != null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
			Integer countCart = cartService.getCountCart(userDtls.getId());
			m.addAttribute("countCart", countCart);
		}

		List<RoomType> allActiveRoomType = roomTypeService.getAllActiveRoomType();
		m.addAttribute("roomTypes", allActiveRoomType);
	}

	@GetMapping("/addCart")
	public String addToCart(@RequestParam Integer rid, @RequestParam Integer uid, HttpSession session) {
		Cart saveCart = cartService.saveCart(rid, uid);

		if (ObjectUtils.isEmpty(saveCart)) {
			session.setAttribute("errorMsg", "Room add to cart failed");
		} else {
			session.setAttribute("succMsg", "Room added to cart");
		}
		return "redirect:/room/" + rid;
	}

	@GetMapping("/cart")
	public String loadCartPage(Principal p, Model m) {

		UserDtls user = getLoggedInUserDetails(p);
		List<Cart> carts = cartService.getCartsByUser(user.getId());
		m.addAttribute("carts", carts);
		if (carts.size() > 0) {
			Double totalOrderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
			m.addAttribute("totalOrderPrice", totalOrderPrice);
		}
		return "/user/cart";
	}

	@GetMapping("/cartQuantityUpdate")
	public String updateCartQuantity(@RequestParam String sy, @RequestParam Integer cid) {
		cartService.updateQuantity(sy, cid);
		return "redirect:/user/cart";
	}

	private UserDtls getLoggedInUserDetails(Principal p) {
		String email = p.getName();
		UserDtls userDtls = userService.getUserByEmail(email);
		return userDtls;
	}

	@GetMapping("/orders")
	public String orderPage(Principal p, Model m) {
		UserDtls user = getLoggedInUserDetails(p);
		List<Cart> carts = cartService.getCartsByUser(user.getId());
		m.addAttribute("carts", carts);
		if (carts.size() > 0) {
			Double orderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
			Double totalOrderPrice = carts.get(carts.size() - 1).getTotalOrderPrice() + 250 + 100;
			m.addAttribute("orderPrice", orderPrice);
			m.addAttribute("totalOrderPrice", totalOrderPrice);
		}
		return "/user/order";
	}

	@PostMapping("/save-order")
	public String saveOrder(@ModelAttribute OrderRequest request, Principal p, HttpSession session) {
		try {
			UserDtls user = getLoggedInUserDetails(p);
			orderService.saveOrder(user.getId(), request);
			session.setAttribute("succMsg", "Đặt phòng thành công!");
			return "redirect:/user/success";
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("errorMsg", "Có lỗi xảy ra khi đặt phòng: " + e.getMessage());
			return "redirect:/user/cart";
		}
	}

	@GetMapping("/success")
	public String loadSuccess(Model model, HttpSession session) {
		// Add any additional data if needed
		return "/user/success";
	}

	@GetMapping("/user-orders")
	public String myOrder(Model m, Principal p) {
		UserDtls loginUser = getLoggedInUserDetails(p);
		List<RoomOrder> orders = orderService.getOrdersByUser(loginUser.getId());
		m.addAttribute("orders", orders);
		return "/user/my_orders";
	}

	@GetMapping("/update-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {

		OrderStatus[] values = OrderStatus.values();
		String status = null;

		for (OrderStatus orderSt : values) {
			if (orderSt.getId().equals(st)) {
				status = orderSt.getName();
			}
		}

		RoomOrder updateOrder = orderService.updateOrderStatus(id, status);

		try {
			commonUtil.sendMailForRoomOrder(updateOrder, status);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!ObjectUtils.isEmpty(updateOrder)) {
			session.setAttribute("succMsg", "Status Updated");
		} else {
			session.setAttribute("errorMsg", "status not updated");
		}
		return "redirect:/user/user-orders";
	}

	@GetMapping("/profile")
	public String profile() {
		return "/user/profile";
	}

	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute UserDtls user, @RequestParam(value = "img", required = false) MultipartFile img, HttpSession session) {
		UserDtls updateUserProfile = userService.updateUserProfile(user, img);
		if (ObjectUtils.isEmpty(updateUserProfile)) {
			session.setAttribute("errorMsg", "Profile not updated");
		} else {
			session.setAttribute("succMsg", "Profile Updated");
		}
		return "redirect:/user/profile";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p,
			HttpSession session) {
		UserDtls loggedInUserDetails = getLoggedInUserDetails(p);

		boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDetails.getPassword());

		if (matches) {
			String encodePassword = passwordEncoder.encode(newPassword);
			loggedInUserDetails.setPassword(encodePassword);
			UserDtls updateUser = userService.updateUser(loggedInUserDetails);
			if (ObjectUtils.isEmpty(updateUser)) {
				session.setAttribute("errorMsg", "Password not updated !! Error in server");
			} else {
				session.setAttribute("succMsg", "Password Updated sucessfully");
			}
		} else {
			session.setAttribute("errorMsg", "Current Password incorrect");
		}

		return "redirect:/user/profile";
	}

	// New: Chat page for a room (simple view)
	@GetMapping("/chat")
	public String chatWithOwner(@RequestParam Integer rid, Model m, Principal p) {
		Room room = roomService.getRoomById(rid);
		m.addAttribute("room", room);
		return "/user/chat";
	}

	// New: Deposit page for a room (simple flow)
	@GetMapping("/deposit")
	public String depositPage(@RequestParam Integer rid, Model m, Principal p) {
		Room room = roomService.getRoomById(rid);
		m.addAttribute("room", room);
		// default deposit amount: if null use 20% of monthly rent
		double suggested = (room.getDeposit() != null) ? room.getDeposit() : ((room.getMonthlyRent() != null) ? room.getMonthlyRent() * 0.2 : 0.0);
		m.addAttribute("suggestedDeposit", suggested);
		return "/user/deposit";
	}

	@PostMapping("/save-deposit")
	public String saveDeposit(@RequestParam Integer rid, @RequestParam(required = false) Double amount, Principal p, HttpSession session) {
		// Minimal placeholder behavior: mark deposit as made and show success message
		if (amount == null) {
			amount = 0.0;
		}
		session.setAttribute("succMsg", "Đặt cọc " + String.format("%,.0f", amount) + " VNĐ cho phòng id: " + rid + " thành công.");
		return "redirect:/user/cart";
	}

}
