package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.RoomType;
import com.ecom.model.Room;
import com.ecom.model.RoomOrder;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.DepositService;
import com.ecom.service.RoomTypeService;
import com.ecom.service.OrderService;
import com.ecom.service.RoomBookingService;
import com.ecom.service.RoomService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private RoomTypeService roomTypeService;

	@Autowired
	private RoomService roomService;

	@Autowired
	private UserService userService;

	@Autowired
	private CartService cartService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private DepositService depositService;

	@Autowired
	private RoomBookingService roomBookingService;

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

	@GetMapping("/")
	public String index() {
		return "admin/index";
	}

	@GetMapping("/loadAddRoom")
	public String loadAddRoom(Model m) {
		List<RoomType> roomTypes = roomTypeService.getAllRoomType();
		m.addAttribute("roomTypes", roomTypes);
		return "admin/add_product";
	}

	@GetMapping("/roomtype")
	public String roomType(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
		// Disabled: Room type management is no longer needed
		return "redirect:/admin/";
	}

	@PostMapping("/saveRoomType")
	public String saveRoomType(@ModelAttribute RoomType roomType, @RequestParam("file") MultipartFile file,
			HttpSession session) throws IOException {
		// Disabled: Room type management is no longer needed
		session.setAttribute("errorMsg", "Chức năng này đã bị vô hiệu hóa");
		return "redirect:/admin/";
	}

	@GetMapping("/deleteRoomType/{id}")
	public String deleteRoomType(@PathVariable int id, HttpSession session) {
		// Disabled: Room type management is no longer needed
		session.setAttribute("errorMsg", "Chức năng này đã bị vô hiệu hóa");
		return "redirect:/admin/";
	}

	@GetMapping("/loadEditRoomType/{id}")
	public String loadEditRoomType(@PathVariable int id, Model m) {
		// Disabled: Room type management is no longer needed
		return "redirect:/admin/";
	}

	@PostMapping("/updateRoomType")
	public String updateRoomType(@ModelAttribute RoomType roomType, @RequestParam("file") MultipartFile file,
			HttpSession session) throws IOException {
		// Disabled: Room type management is no longer needed
		session.setAttribute("errorMsg", "Chức năng này đã bị vô hiệu hóa");
		return "redirect:/admin/";
	}

	@PostMapping("/saveRoom")
	public String saveRoom(@ModelAttribute Room room, @RequestParam("file") MultipartFile image,
			HttpSession session, Principal p) throws IOException {

		String imageName = image.isEmpty() ? "default.jpg" : image.getOriginalFilename();

		room.setImage(imageName);
		room.setIsAvailable(true);
		room.setIsActive(true);

		// Set owner ID to current logged-in user
		UserDtls loggedInUser = commonUtil.getLoggedInUserDetails(p);
		room.setOwnerId(loggedInUser.getId());

		Room saveRoom = roomService.saveRoom(room);

		if (!ObjectUtils.isEmpty(saveRoom)) {

			File saveFile = new ClassPathResource("static/img").getFile();

			Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "room_img" + File.separator
					+ image.getOriginalFilename());

			Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

			session.setAttribute("succMsg", "Room Saved Success");
		} else {
			session.setAttribute("errorMsg", "something wrong on server");
		}

		return "redirect:/admin/loadAddRoom";
	}

	@GetMapping("/rooms")
	public String loadViewRoom(Model m, @RequestParam(defaultValue = "") String ch,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
			Principal p) {

		// Get current logged-in owner
		UserDtls loggedInUser = commonUtil.getLoggedInUserDetails(p);

		// Only get rooms belonging to this owner
		List<Room> ownerRooms = roomService.getRoomsByOwnerId(loggedInUser.getId());

		// Apply search filter if needed
		if (ch != null && ch.length() > 0) {
			ownerRooms = ownerRooms.stream()
				.filter(room -> room.getRoomName().toLowerCase().contains(ch.toLowerCase())
					|| room.getRoomType().toLowerCase().contains(ch.toLowerCase()))
				.toList();
		}

		m.addAttribute("rooms", ownerRooms);
		m.addAttribute("pageNo", 0);
		m.addAttribute("pageSize", ownerRooms.size());
		m.addAttribute("totalElements", ownerRooms.size());
		m.addAttribute("totalPages", 1);
		m.addAttribute("isFirst", true);
		m.addAttribute("isLast", true);

		return "admin/rooms";
	}

	@GetMapping("/deleteRoom/{id}")
	public String deleteRoom(@PathVariable int id, HttpSession session) {
		Boolean deleteRoom = roomService.deleteRoom(id);
		if (deleteRoom) {
			session.setAttribute("succMsg", "Room delete success");
		} else {
			session.setAttribute("errorMsg", "Something wrong on server");
		}
		return "redirect:/admin/rooms";
	}

	@GetMapping("/editRoom/{id}")
	public String editRoom(@PathVariable int id, Model m) {
		m.addAttribute("room", roomService.getRoomById(id));
		m.addAttribute("roomTypes", roomTypeService.getAllRoomType());
		return "admin/edit_product";
	}

	@PostMapping("/updateRoom")
	public String updateRoom(@ModelAttribute Room room, @RequestParam("file") MultipartFile image,
			HttpSession session, Model m) {

		Room updateRoom = roomService.updateRoom(room, image);
		if (!ObjectUtils.isEmpty(updateRoom)) {
			session.setAttribute("succMsg", "Room update success");
		} else {
			session.setAttribute("errorMsg", "Something wrong on server");
		}
		return "redirect:/admin/editRoom/" + room.getId();
	}

	@GetMapping("/users")
	public String getAllUsers(Model m, @RequestParam Integer type, Principal p) {
		UserDtls loggedInUser = commonUtil.getLoggedInUserDetails(p);

		List<UserDtls> users = null;
		if (type == 1) {
			// Get all orders for this owner's rooms
			List<RoomOrder> ownerOrders = orderService.getOrdersByOwnerId(loggedInUser.getId());

			// Extract unique users (renters) from these orders
			users = ownerOrders.stream()
				.map(RoomOrder::getUser)
				.distinct()
				.filter(user -> user != null && "ROLE_USER".equals(user.getRole()))
				.toList();
		} else {
			// For admin type, just return the current logged-in admin
			users = List.of(loggedInUser);
		}
		m.addAttribute("userType", type);
		m.addAttribute("users", users);
		return "/admin/users";
	}

	@GetMapping("/updateSts")
	public String updateUserAccountStatus(@RequestParam Boolean status, @RequestParam Integer id,@RequestParam Integer type, HttpSession session) {
		Boolean f = userService.updateAccountStatus(id, status);
		if (f) {
			session.setAttribute("succMsg", "Account Status Updated");
		} else {
			session.setAttribute("errorMsg", "Something wrong on server");
		}
		return "redirect:/admin/users?type="+type;
	}

	@GetMapping("/orders")
	public String getAllOrders(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
			Principal p) {

		// Get current logged-in owner
		UserDtls loggedInUser = commonUtil.getLoggedInUserDetails(p);

		// Only get orders for rooms belonging to this owner
		List<RoomOrder> ownerOrders = orderService.getOrdersByOwnerId(loggedInUser.getId());

		// Lấy thông tin booking để hiển thị
		List<com.ecom.model.RoomBooking> bookings = roomBookingService.getBookingsByOwner(loggedInUser.getId());
		List<com.ecom.model.RoomBooking> activeBookings = bookings.stream()
			.filter(b -> "ACTIVE".equalsIgnoreCase(b.getStatus()))
			.toList();

		m.addAttribute("orders", ownerOrders);
		m.addAttribute("bookings", activeBookings);
		m.addAttribute("srch", false);

		m.addAttribute("pageNo", 0);
		m.addAttribute("pageSize", ownerOrders.size());
		m.addAttribute("totalElements", ownerOrders.size());
		m.addAttribute("totalPages", 1);
		m.addAttribute("isFirst", true);
		m.addAttribute("isLast", true);

		return "/admin/orders";
	}

	@PostMapping("/update-order-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {

		OrderStatus[] values = OrderStatus.values();
		String status = null;

		for (OrderStatus orderSt : values) {
			if (orderSt.getId().equals(st)) {
				status = orderSt.getName();
			}
		}

		RoomOrder updateOrder = orderService.updateOrderStatus(id, status);



		if (!ObjectUtils.isEmpty(updateOrder)) {
			session.setAttribute("succMsg", "Status Updated");
		} else {
			session.setAttribute("errorMsg", "status not updated");
		}
		return "redirect:/admin/orders";
	}

	@GetMapping("/search-order")
	public String searchOrder(@RequestParam String orderId, Model m, HttpSession session,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

		if (orderId != null && orderId.length() > 0) {

			RoomOrder order = orderService.getOrdersByOrderId(orderId.trim());

			if (ObjectUtils.isEmpty(order)) {
				session.setAttribute("errorMsg", "Incorrect orderId");
				m.addAttribute("orderDtls", null);
			} else {
				m.addAttribute("orderDtls", order);
			}

			m.addAttribute("srch", true);
		} else {

			Page<RoomOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);
			m.addAttribute("orders", page);
			m.addAttribute("srch", false);

			m.addAttribute("pageNo", page.getNumber());
			m.addAttribute("pageSize", pageSize);
			m.addAttribute("totalElements", page.getTotalElements());
			m.addAttribute("totalPages", page.getTotalPages());
			m.addAttribute("isFirst", page.isFirst());
			m.addAttribute("isLast", page.isLast());

		}
		return "/admin/orders";

	}

	@GetMapping("/add-admin")
	public String loadAdminAdd() {
		return "/admin/add_admin";
	}

	@PostMapping("/save-admin")
	public String saveAdmin(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session)
			throws IOException {

		String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
		user.setProfileImage(imageName);
		UserDtls saveUser = userService.saveAdmin(user);

		if (!ObjectUtils.isEmpty(saveUser)) {
			if (!file.isEmpty()) {
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
						+ file.getOriginalFilename());

//				System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
			session.setAttribute("succMsg", "Register successfully");
		} else {
			session.setAttribute("errorMsg", "something wrong on server");
		}

		return "redirect:/admin/add-admin";
	}

	@GetMapping("/profile")
	public String profile() {
		return "/admin/profile";
	}

	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute UserDtls user, @RequestParam(required = false) MultipartFile img, HttpSession session) {
		UserDtls updateUserProfile = userService.updateUserProfile(user, img);
		if (ObjectUtils.isEmpty(updateUserProfile)) {
			session.setAttribute("errorMsg", "Profile not updated");
		} else {
			session.setAttribute("succMsg", "Profile Updated");
		}
		return "redirect:/admin/profile";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p,
			HttpSession session) {
		UserDtls loggedInUserDetails = commonUtil.getLoggedInUserDetails(p);

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

		return "redirect:/admin/profile";
	}

	@GetMapping("/chat")
	public String chat(@RequestParam(required = false) String orderId, Model m, HttpSession session) {
		if (orderId != null && !orderId.isEmpty()) {
			RoomOrder order = orderService.getOrdersByOrderId(orderId.trim());
			if (ObjectUtils.isEmpty(order)) {
				session.setAttribute("errorMsg", "Không tìm thấy đơn thuê");
				return "redirect:/admin/orders";
			}
			m.addAttribute("order", order);
		}
		return "/admin/chat";
	}

	@GetMapping("/deposits")
	public String getDeposits(Model m, Principal p) {
		// Get current logged-in owner
		UserDtls loggedInUser = commonUtil.getLoggedInUserDetails(p);

		// Get all deposits for rooms belonging to this owner
		List<com.ecom.model.Deposit> deposits = depositService.getDepositsByOwner(loggedInUser.getId());

		m.addAttribute("deposits", deposits);
		return "/admin/deposits";
	}

	@PostMapping("/update-deposit-status")
	public String updateDepositStatus(@RequestParam Integer id,
									  @RequestParam String status,
									  @RequestParam(required = false) String adminNote,
									  HttpSession session) {

		// Đơn giản hóa: chỉ có 2 trạng thái APPROVED hoặc REJECTED
		if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
			session.setAttribute("errorMsg", "Trạng thái không hợp lệ!");
			return "redirect:/admin/deposits";
		}

		com.ecom.model.Deposit updatedDeposit = depositService.updateDepositStatus(id, status, adminNote);

		if (!ObjectUtils.isEmpty(updatedDeposit)) {
			if ("APPROVED".equals(status)) {
				session.setAttribute("succMsg", "Đã chấp nhận yêu cầu đặt cọc! Phòng trọ đã được đánh dấu là đã thuê.");
			} else if ("REJECTED".equals(status)) {
				session.setAttribute("succMsg", "Đã từ chối yêu cầu đặt cọc!");
			}
		} else {
			session.setAttribute("errorMsg", "Không thể cập nhật trạng thái đặt cọc!");
		}
		return "redirect:/admin/deposits";
	}

	@GetMapping("/delete-deposit/{id}")
	public String deleteDeposit(@PathVariable Integer id, HttpSession session) {
		Boolean deleted = depositService.deleteDeposit(id);
		if (deleted) {
			session.setAttribute("succMsg", "Xóa đơn đặt cọc thành công");
		} else {
			session.setAttribute("errorMsg", "Không thể xóa đơn đặt cọc");
		}
		return "redirect:/admin/deposits";
	}

	@PostMapping("/cancel-rent")
	public String cancelRent(@RequestParam Integer orderId, HttpSession session) {
	    RoomOrder order = orderService.updateOrderStatus(orderId, "CANCELLED");
	    if (order != null && order.getRoom() != null) {
	        order.getRoom().setStatus("ACTIVE");
	        order.getRoom().setIsAvailable(true);
	        roomService.updateRoomStatus(order.getRoom().getId(), "ACTIVE");
	    }
	    session.setAttribute("succMsg", "Đã hủy cho thuê thành công!");
	    return "redirect:/admin/orders";
	}

}
