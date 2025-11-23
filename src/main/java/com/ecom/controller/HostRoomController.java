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
import com.ecom.model.RoomStatus;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.DepositService;
import com.ecom.service.MonthlyPaymentService;
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
public class HostRoomController {

	// ==================== CONSTANTS ====================
	private static class Routes {
		static final String REDIRECT_ADMIN = "redirect:/admin/";
		static final String REDIRECT_ROOMS = "redirect:/admin/rooms";
		static final String REDIRECT_ORDERS = "redirect:/admin/orders";
		static final String REDIRECT_PAYMENTS = "redirect:/admin/payments";
		static final String REDIRECT_DEPOSITS = "redirect:/admin/deposits";
		static final String REDIRECT_REVIEWS = "redirect:/admin/reviews";
		static final String REDIRECT_PROFILE = "redirect:/admin/profile";
		static final String REDIRECT_ADD_ADMIN = "redirect:/admin/add-admin";
		static final String REDIRECT_ADD_ROOM = "redirect:/admin/loadAddRoom";
	}

	private static class Views {
		static final String ADMIN_INDEX = "admin/index";
		static final String ADMIN_ROOMS = "admin/rooms";
		static final String ADMIN_PAYMENTS = "/admin/payments";
		static final String ADMIN_REVIEWS = "/admin/reviews";
		static final String ADMIN_DEPOSITS = "/admin/deposits";
		static final String ADMIN_PROFILE = "/admin/profile";
	}

	private static class Messages {
		// Success messages
		static final String SUCCESS_ROOM_SAVED = "Room Saved Success";
		static final String SUCCESS_ROOM_UPDATED = "Chỉnh sửa phòng thành công";
		static final String SUCCESS_ROOM_DELETED = "Room delete success";
		static final String SUCCESS_PAYMENT_RECORDED = "Đã ghi nhận thanh toán thành công!";
		static final String SUCCESS_PAYMENT_CREATED = "Đã tạo hóa đơn thanh toán thành công!";
		static final String SUCCESS_REVIEW_RESPONDED = "Đã gửi phản hồi thành công!";
		static final String SUCCESS_REMINDER_SENT = "Đã gửi nhắc nhở thanh toán thành công!";
		static final String SUCCESS_BULK_REMINDERS_SENT = "Đã gửi nhắc nhở hàng loạt thành công!";

		// Error messages
		static final String ERROR_SERVER = "something wrong on server";
		static final String ERROR_UNAUTHORIZED_ROOM = "Bạn không có quyền xem chi tiết phòng này!";
		static final String ERROR_UNAUTHORIZED_PAYMENT = "Bạn không có quyền ghi nhận thanh toán này!";
		static final String ERROR_UNAUTHORIZED_REVIEW = "Bạn không có quyền phản hồi đánh giá này!";
		static final String ERROR_REVIEW_NOT_FOUND = "Không tìm thấy đánh giá!";
		static final String ERROR_PAYMENT_FAILED = "Không thể ghi nhận thanh toán!";
		static final String ERROR_PAYMENT_CREATE_FAILED = "Không thể tạo hóa đơn. Có thể hóa đơn này đã tồn tại!";
	}

	// ==================== DEPENDENCIES ====================
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

	@Autowired
	private MonthlyPaymentService monthlyPaymentService;

	@Autowired
	private com.ecom.service.ReviewService reviewService;

	@Autowired
	private com.ecom.service.PaymentNotificationService paymentNotificationService;

	// ==================== HELPER METHODS ====================

	/**
	 * Get logged-in user from Principal
	 */
	private UserDtls getLoggedInUser(Principal p) {
		return commonUtil.getLoggedInUserDetails(p);
	}

	/**
	 * Check if user is owner of the room
	 */
	private boolean isRoomOwner(UserDtls user, Room room) {
		return room != null && user != null && room.getOwnerId().equals(user.getId());
	}

	/**
	 * Check if user is owner of the payment
	 */
	private boolean isPaymentOwner(UserDtls user, com.ecom.model.MonthlyPayment payment) {
		return payment != null
				&& payment.getRoomBooking() != null
				&& payment.getRoomBooking().getRoom() != null
				&& isRoomOwner(user, payment.getRoomBooking().getRoom());
	}

	/**
	 * Set success message in session
	 */
	private void setSuccessMessage(HttpSession session, String message) {
		session.setAttribute("succMsg", message);
	}

	/**
	 * Set error message in session
	 */
	private void setErrorMessage(HttpSession session, String message) {
		session.setAttribute("errorMsg", message);
	}

	/**
	 * Calculate room statistics for dashboard
	 */
	private void calculateRoomStatistics(List<Room> ownerRooms, Model m) {
		long totalRooms = ownerRooms.size();
		// Add null safety for getIsAvailable
		long availableRooms = ownerRooms.stream()
				.filter(r -> r != null && r.getIsAvailable() != null && r.getIsAvailable())
				.count();
		long occupiedRooms = totalRooms - availableRooms;

		m.addAttribute("totalRooms", totalRooms);
		m.addAttribute("availableRooms", availableRooms);
		m.addAttribute("occupiedRooms", occupiedRooms);
	}

	/**
	 * Calculate payment statistics for dashboard
	 */
	private void calculatePaymentStatistics(Integer ownerId, Model m) {
		List<com.ecom.model.MonthlyPayment> allPayments = monthlyPaymentService.getAllPaymentsByOwnerId(ownerId);

		long pendingPayments = allPayments.stream().filter(p -> "PENDING".equals(p.getStatus())).count();
		long overduePayments = allPayments.stream().filter(p -> "OVERDUE".equals(p.getStatus())).count();
		long paidPayments = allPayments.stream().filter(p -> "PAID".equals(p.getStatus())).count();

		Double totalMonthlyRevenue = monthlyPaymentService.getTotalRevenueByOwnerId(ownerId);
		Double expectedRevenue = allPayments.stream()
				.filter(p -> !"PAID".equals(p.getStatus()))
				.mapToDouble(com.ecom.model.MonthlyPayment::getAmount)
				.sum();

		List<com.ecom.model.MonthlyPayment> upcomingPayments = monthlyPaymentService.getUpcomingPayments(7);
		List<com.ecom.model.MonthlyPayment> overduePaymentsList = monthlyPaymentService.getOverduePayments();

		m.addAttribute("pendingPayments", pendingPayments);
		m.addAttribute("overduePayments", overduePayments);
		m.addAttribute("paidPayments", paidPayments);
		m.addAttribute("totalMonthlyRevenue", totalMonthlyRevenue);
		m.addAttribute("expectedRevenue", expectedRevenue);
		m.addAttribute("upcomingPayments", upcomingPayments);
		m.addAttribute("overduePaymentsList", overduePaymentsList);
	}

	/**
	 * Calculate booking statistics for dashboard
	 */
	private void calculateBookingStatistics(List<com.ecom.model.RoomBooking> activeBookings, Model m) {
		java.time.LocalDate today = java.time.LocalDate.now();
		java.time.LocalDate thirtyDaysLater = today.plusDays(30);

		long totalTenants = activeBookings.stream()
				.map(com.ecom.model.RoomBooking::getUser)
				.distinct()
				.count();

		List<com.ecom.model.RoomBooking> expiringBookings = activeBookings.stream()
				.filter(b -> b.getEndDate() != null
						&& b.getEndDate().isAfter(today)
						&& b.getEndDate().isBefore(thirtyDaysLater))
				.sorted((b1, b2) -> b1.getEndDate().compareTo(b2.getEndDate()))
				.limit(5)
				.toList();

		m.addAttribute("totalTenants", totalTenants);
		m.addAttribute("expiringBookings", expiringBookings);
	}

	// ==================== COMMON ATTRIBUTES ====================
	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		if (p != null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
			Integer countCart = cartService.getCountCart(userDtls.getId());
			m.addAttribute("countCart", countCart);

			// Đếm số yêu cầu đặt cọc đang chờ xử lý (pending)
			List<com.ecom.model.Deposit> deposits = depositService.getDepositsByOwner(userDtls.getId());
			long pendingDepositCount = deposits.stream()
					.filter(d -> "PENDING".equals(d.getStatus()))
					.count();
			m.addAttribute("pendingDepositCount", pendingDepositCount);
		}

		List<RoomType> allActiveRoomType = roomTypeService.getAllActiveRoomType();
		m.addAttribute("roomTypes", allActiveRoomType);
	}

	// ==================== DASHBOARD ====================

	@GetMapping("/")
	public String index(Model m, Principal p) {
		UserDtls loggedInUser = getLoggedInUser(p);

		// Lấy dữ liệu cơ bản
		List<Room> ownerRooms = roomService.getRoomsByOwnerId(loggedInUser.getId());
		List<RoomOrder> ownerOrders = orderService.getOrdersByOwnerId(loggedInUser.getId());
		List<com.ecom.model.RoomBooking> bookings = roomBookingService.getBookingsByOwner(loggedInUser.getId());
		List<com.ecom.model.RoomBooking> activeBookings = bookings.stream()
				.filter(b -> "ACTIVE".equalsIgnoreCase(b.getStatus()))
				.toList();
		List<com.ecom.model.Deposit> deposits = depositService.getDepositsByOwner(loggedInUser.getId());

		// Tính thống kê cơ bản
		long pendingDeposits = deposits.stream().filter(d -> "PENDING".equals(d.getStatus())).count();
		long approvedDeposits = deposits.stream().filter(d -> "APPROVED".equals(d.getStatus())).count();

		double totalRevenue = ownerOrders.stream()
				.filter(o -> "SUCCESS".equalsIgnoreCase(o.getStatus()) || "DELIVERED".equalsIgnoreCase(o.getStatus()))
				.mapToDouble(RoomOrder::getPrice)
				.sum();

		double totalDepositReceived = deposits.stream()
				.filter(d -> "APPROVED".equals(d.getStatus()))
				.mapToDouble(com.ecom.model.Deposit::getAmount)
				.sum();

		// Sử dụng helper methods để tính thống kê phức tạp
		calculateRoomStatistics(ownerRooms, m);
		calculatePaymentStatistics(loggedInUser.getId(), m);
		calculateBookingStatistics(activeBookings, m);

		// Recent reviews
		List<com.ecom.model.Review> recentReviews = reviewService.getReviewsByOwnerId(loggedInUser.getId())
				.stream()
				.limit(5)
				.toList();

		// Thêm các thống kê khác vào model
		m.addAttribute("totalRevenue", totalRevenue);
		m.addAttribute("totalDepositReceived", totalDepositReceived);
		m.addAttribute("pendingDeposits", pendingDeposits);
		m.addAttribute("approvedDeposits", approvedDeposits);
		m.addAttribute("totalOrders", ownerOrders.size());
		m.addAttribute("activeBookings", activeBookings);
		m.addAttribute("recentDeposits", deposits.stream().limit(5).toList());
		m.addAttribute("recentOrders", ownerOrders.stream().limit(5).toList());
		m.addAttribute("recentReviews", recentReviews);

		return "admin/index";
	}

	@GetMapping("/loadAddRoom")
	public String loadAddRoom(Model m) {
		List<RoomType> roomTypes = roomTypeService.getAllRoomType();
		m.addAttribute("roomTypes", roomTypes);
		return "admin/add_room";
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
		UserDtls loggedInUser = getLoggedInUser(p);
		room.setOwnerId(loggedInUser.getId());

		Room saveRoom = roomService.saveRoom(room);

		if (!ObjectUtils.isEmpty(saveRoom) && !image.isEmpty()) {
			// Save to external uploads directory (not classpath)
			String uploadsDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator
					+ "room_img";
			Path uploadPath = Paths.get(uploadsDir);

			// Create directory if it doesn't exist
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}

			Path filePath = uploadPath.resolve(image.getOriginalFilename());
			Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

			setSuccessMessage(session, Messages.SUCCESS_ROOM_SAVED);
		} else if (!ObjectUtils.isEmpty(saveRoom)) {
			setSuccessMessage(session, Messages.SUCCESS_ROOM_SAVED);
		} else {
			setErrorMessage(session, Messages.ERROR_SERVER);
		}

		return Routes.REDIRECT_ADD_ROOM;
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

		// Populate transient fields for each room
		java.time.LocalDate today = java.time.LocalDate.now();
		for (Room room : ownerRooms) {
			// Get active bookings for this room
			List<com.ecom.model.RoomBooking> roomBookings = roomBookingService.getBookingsByRoom(room.getId());
			List<com.ecom.model.RoomBooking> activeBookings = roomBookings.stream()
					.filter(b -> "ACTIVE".equalsIgnoreCase(b.getStatus()))
					.toList();

			room.setCurrentBookings(activeBookings);
			room.setOccupancyCount(activeBookings.size());

			// Auto-update room status based on actual occupancy
			if (activeBookings.isEmpty()) {
				// No active bookings = room is available
				room.setRoomStatus(RoomStatus.AVAILABLE);
				room.setIsAvailable(true);
			} else {
				// Has active bookings = room is occupied
				room.setRoomStatus(RoomStatus.OCCUPIED);
				room.setIsAvailable(false);

				// Set room leader (first active booking's user)
				room.setRoomLeader(activeBookings.get(0).getUser());
			}

			// Check for overdue payments
			boolean hasOverdue = false;
			for (com.ecom.model.RoomBooking booking : activeBookings) {
				List<com.ecom.model.MonthlyPayment> payments = monthlyPaymentService
						.getPaymentsByBookingId(booking.getId());
				if (payments.stream().anyMatch(pay -> "OVERDUE".equals(pay.getStatus()))) {
					hasOverdue = true;
					break;
				}
			}
			room.setHasOverduePayments(hasOverdue);
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
			setSuccessMessage(session, Messages.SUCCESS_ROOM_DELETED);
		} else {
			setErrorMessage(session, Messages.ERROR_SERVER);
		}
		return Routes.REDIRECT_ROOMS;
	}

	@GetMapping("/editRoom/{id}")
	public String editRoom(@PathVariable int id, Model m) {
		m.addAttribute("room", roomService.getRoomById(id));
		m.addAttribute("roomTypes", roomTypeService.getAllRoomType());
		return "admin/edit_room";
	}

	@PostMapping("/updateRoom")
	public String updateRoom(@ModelAttribute Room room, @RequestParam("file") MultipartFile image,
			HttpSession session, Model m) {

		Room updateRoom = roomService.updateRoom(room, image);
		if (!ObjectUtils.isEmpty(updateRoom)) {
			setSuccessMessage(session, Messages.SUCCESS_ROOM_UPDATED);
			return Routes.REDIRECT_ROOMS;
		} else {
			setErrorMessage(session, Messages.ERROR_SERVER);
			return "redirect:/admin/editRoom/" + room.getId();
		}
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
	public String updateUserAccountStatus(@RequestParam Boolean status, @RequestParam Integer id,
			@RequestParam Integer type, HttpSession session) {
		Boolean f = userService.updateAccountStatus(id, status);
		if (f) {
			session.setAttribute("succMsg", "Account Status Updated");
		} else {
			session.setAttribute("errorMsg", "Something wrong on server");
		}
		return "redirect:/admin/users?type=" + type;
	}

	@GetMapping("/orders")
	public String manageTenants(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
			Principal p) {

		// Get current logged-in owner
		UserDtls loggedInUser = commonUtil.getLoggedInUserDetails(p);

		// Get all bookings for rooms belonging to this owner
		List<com.ecom.model.RoomBooking> bookings = roomBookingService.getBookingsByOwner(loggedInUser.getId());

		// Calculate status for each booking
		java.time.LocalDate today = java.time.LocalDate.now();
		for (com.ecom.model.RoomBooking b : bookings) {
			if ("CANCELLED".equalsIgnoreCase(b.getStatus()) || "EXPIRED".equalsIgnoreCase(b.getStatus())) {
				b.setRentalStatus("Terminated");
			} else if ("ACTIVE".equalsIgnoreCase(b.getStatus())) {
				// Check for pending payments
				List<com.ecom.model.MonthlyPayment> payments = monthlyPaymentService.getPaymentsByBookingId(b.getId());
				boolean hasPending = payments.stream()
						.anyMatch(pay -> "PENDING".equals(pay.getStatus()) || "OVERDUE".equals(pay.getStatus()));

				if (hasPending) {
					b.setRentalStatus("Pending Payment");
				} else if (b.getEndDate() != null && b.getEndDate().minusDays(7).isBefore(today)) {
					b.setRentalStatus("Expiring");
				} else {
					b.setRentalStatus("Active");
				}
			} else {
				b.setRentalStatus(b.getStatus());
			}
		}

		m.addAttribute("bookings", bookings);
		m.addAttribute("srch", false);

		// Pagination logic (simplified for now as we are fetching all bookings)
		m.addAttribute("pageNo", 0);
		m.addAttribute("pageSize", bookings.size() > 0 ? bookings.size() : 10);
		m.addAttribute("totalElements", bookings.size());
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
				// Save to external uploads directory (not classpath)
				String uploadsDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator
						+ "profile_img";
				Path uploadPath = Paths.get(uploadsDir);

				// Create directory if it doesn't exist
				if (!Files.exists(uploadPath)) {
					Files.createDirectories(uploadPath);
				}

				Path filePath = uploadPath.resolve(file.getOriginalFilename());
				Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
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
	public String updateProfile(@ModelAttribute UserDtls user, @RequestParam(required = false) MultipartFile img,
			HttpSession session) {
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
			// Reset session "đã xem" của user để badge xuất hiện lại
			if (updatedDeposit.getUser() != null) {
				session.removeAttribute("depositNotificationsViewed_" + updatedDeposit.getUser().getId());
			}

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
	public String terminateRental(@RequestParam Integer bookingId, HttpSession session) {
		com.ecom.model.RoomBooking booking = roomBookingService.updateBookingStatus(bookingId, "CANCELLED");

		if (booking != null && booking.getRoom() != null) {
			// Update room status to AVAILABLE
			Room room = booking.getRoom();
			room.setIsAvailable(true);
			room.setRoomStatus(RoomStatus.AVAILABLE);
			roomService.saveRoom(room);
		}
		session.setAttribute("succMsg", "Đã kết thúc hợp đồng thuê thành công!");
		return "redirect:/admin/rooms";
	}

	// ==================== PAYMENT MANAGEMENT ====================

	@GetMapping("/payments")
	public String getAllPayments(Model m, Principal p,
			@RequestParam(required = false) String status) {
		UserDtls loggedInUser = commonUtil.getLoggedInUserDetails(p);

		List<com.ecom.model.MonthlyPayment> payments;
		if (status != null && !status.isEmpty()) {
			payments = monthlyPaymentService.getPaymentsByOwnerAndStatus(loggedInUser.getId(), status);
		} else {
			payments = monthlyPaymentService.getAllPaymentsByOwnerId(loggedInUser.getId());
		}

		// Thống kê
		long pendingCount = payments.stream().filter(p1 -> "PENDING".equals(p1.getStatus())).count();
		long overdueCount = payments.stream().filter(p1 -> "OVERDUE".equals(p1.getStatus())).count();
		long paidCount = payments.stream().filter(p1 -> "PAID".equals(p1.getStatus())).count();

		Double totalExpected = payments.stream()
				.filter(p1 -> !"PAID".equals(p1.getStatus()))
				.mapToDouble(com.ecom.model.MonthlyPayment::getAmount)
				.sum();

		Double totalReceived = monthlyPaymentService.getTotalRevenueByOwnerId(loggedInUser.getId());

		m.addAttribute("payments", payments);
		m.addAttribute("pendingCount", pendingCount);
		m.addAttribute("overdueCount", overdueCount);
		m.addAttribute("paidCount", paidCount);
		m.addAttribute("totalExpected", totalExpected);
		m.addAttribute("totalReceived", totalReceived);
		m.addAttribute("currentStatus", status);

		return "/admin/payments";
	}

	@GetMapping("/room/{roomId}/payments")
	public String getRoomPayments(@PathVariable Integer roomId, Model m, Principal p, HttpSession session) {
		UserDtls loggedInUser = commonUtil.getLoggedInUserDetails(p);

		// Kiểm tra quyền sở hữu phòng
		Room room = roomService.getRoomById(roomId);
		if (room == null || !room.getOwnerId().equals(loggedInUser.getId())) {
			session.setAttribute("errorMsg", "Bạn không có quyền xem thanh toán của phòng này!");
			return "redirect:/admin/rooms";
		}

		List<com.ecom.model.MonthlyPayment> payments = monthlyPaymentService.getPaymentsByRoomId(roomId);

		m.addAttribute("room", room);
		m.addAttribute("payments", payments);

		return "/admin/room_payments";
	}

	@GetMapping("/booking/{bookingId}/payments")
	public String getBookingPayments(@PathVariable Integer bookingId, Model m, Principal p, HttpSession session) {
		UserDtls loggedInUser = commonUtil.getLoggedInUserDetails(p);

		// Kiểm tra quyền (thông qua room)
		com.ecom.model.RoomBooking booking = roomBookingService.getBookingById(bookingId);
		if (booking == null || booking.getRoom() == null
				|| !booking.getRoom().getOwnerId().equals(loggedInUser.getId())) {
			session.setAttribute("errorMsg", "Bạn không có quyền xem thanh toán này!");
			return "redirect:/admin/orders";
		}

		List<com.ecom.model.MonthlyPayment> payments = monthlyPaymentService.getPaymentsByBookingId(bookingId);

		m.addAttribute("booking", booking);
		m.addAttribute("payments", payments);

		return "/admin/booking_payments";
	}

	@PostMapping("/payment/create")
	public String createPayment(@RequestParam Integer bookingId,
			@RequestParam Integer month,
			@RequestParam Integer year,
			HttpSession session,
			Principal p) {
		UserDtls loggedInUser = commonUtil.getLoggedInUserDetails(p);

		// Kiểm tra quyền
		com.ecom.model.RoomBooking booking = roomBookingService.getBookingById(bookingId);
		if (booking == null || booking.getRoom() == null
				|| !booking.getRoom().getOwnerId().equals(loggedInUser.getId())) {
			session.setAttribute("errorMsg", "Bạn không có quyền tạo hóa đơn cho booking này!");
			return "redirect:/admin/orders";
		}

		// Tạo payment
		com.ecom.model.MonthlyPayment payment = monthlyPaymentService.createMonthlyPayment(bookingId, month, year);

		if (payment != null) {
			session.setAttribute("succMsg", "Đã tạo hóa đơn thanh toán thành công!");
		} else {
			session.setAttribute("errorMsg", "Không thể tạo hóa đơn. Có thể hóa đơn này đã tồn tại!");
		}

		return "redirect:/admin/booking/" + bookingId + "/payments";
	}

	@PostMapping("/payment/{paymentId}/record")
	public String recordPayment(@PathVariable Integer paymentId,
			@RequestParam Double amount,
			@RequestParam(required = false) String paidDateStr,
			@RequestParam(required = false) Double electricityUsed,
			@RequestParam(required = false) Double waterUsed,
			@RequestParam(required = false) Double additionalFees,
			HttpSession session,
			Principal p) {
		UserDtls loggedInUser = commonUtil.getLoggedInUserDetails(p);

		// Kiểm tra quyền
		com.ecom.model.MonthlyPayment payment = monthlyPaymentService.getPaymentById(paymentId);
		if (payment == null || payment.getRoomBooking() == null
				|| payment.getRoomBooking().getRoom() == null
				|| !payment.getRoomBooking().getRoom().getOwnerId().equals(loggedInUser.getId())) {
			session.setAttribute("errorMsg", "Bạn không có quyền ghi nhận thanh toán này!");
			return "redirect:/admin/payments";
		}

		// Cập nhật thông tin điện nước nếu có
		if (electricityUsed != null) {
			payment.setElectricityUsed(electricityUsed);
		}
		if (waterUsed != null) {
			payment.setWaterUsed(waterUsed);
		}
		if (additionalFees != null) {
			payment.setAdditionalFees(additionalFees);
		}

		// Tính lại tổng tiền nếu có điện nước
		if (electricityUsed != null || waterUsed != null || additionalFees != null) {
			Room room = payment.getRoomBooking().getRoom();
			Double totalAmount = payment.calculateTotalAmount(
					room.getElectricityCost(),
					room.getWaterCost());
			payment.setAmount(totalAmount);
			monthlyPaymentService.savePayment(payment);
		}

		// Ghi nhận thanh toán
		java.time.LocalDate paidDate = paidDateStr != null && !paidDateStr.isEmpty()
				? java.time.LocalDate.parse(paidDateStr)
				: java.time.LocalDate.now();

		com.ecom.model.MonthlyPayment updatedPayment = monthlyPaymentService.recordPayment(
				paymentId, amount, paidDate);

		if (updatedPayment != null) {
			session.setAttribute("succMsg", "Đã ghi nhận thanh toán thành công!");
		} else {
			session.setAttribute("errorMsg", "Không thể ghi nhận thanh toán!");
		}

		return "redirect:/admin/payments";
	}

	@PostMapping("/payment/{paymentId}/update-status")
	public String updatePaymentStatus(@PathVariable Integer paymentId,
			@RequestParam String status,
			HttpSession session,
			Principal p) {
		UserDtls loggedInUser = commonUtil.getLoggedInUserDetails(p);

		// Kiểm tra quyền
		com.ecom.model.MonthlyPayment payment = monthlyPaymentService.getPaymentById(paymentId);
		if (payment == null || payment.getRoomBooking() == null
				|| payment.getRoomBooking().getRoom() == null
				|| !payment.getRoomBooking().getRoom().getOwnerId().equals(loggedInUser.getId())) {
			session.setAttribute("errorMsg", "Bạn không có quyền cập nhật thanh toán này!");
			return "redirect:/admin/payments";
		}

		com.ecom.model.MonthlyPayment updatedPayment = monthlyPaymentService.updatePaymentStatus(paymentId, status);

		if (updatedPayment != null) {
			session.setAttribute("succMsg", "Đã cập nhật trạng thái thanh toán!");
		} else {
			session.setAttribute("errorMsg", "Không thể cập nhật trạng thái!");
		}

		return "redirect:/admin/payments";
	}

	// ==================== NOTIFICATION MANAGEMENT ====================

	@PostMapping("/payment/{paymentId}/send-reminder")
	public String sendPaymentReminder(@PathVariable Integer paymentId,
			@RequestParam(defaultValue = "IN_APP") String type,
			HttpSession session,
			Principal p) {
		UserDtls loggedInUser = commonUtil.getLoggedInUserDetails(p);

		// Kiểm tra quyền
		com.ecom.model.MonthlyPayment payment = monthlyPaymentService.getPaymentById(paymentId);
		if (payment == null || payment.getRoomBooking() == null
				|| payment.getRoomBooking().getRoom() == null
				|| !payment.getRoomBooking().getRoom().getOwnerId().equals(loggedInUser.getId())) {
			session.setAttribute("errorMsg", "Bạn không có quyền gửi nhắc nhở cho thanh toán này!");
			return "redirect:/admin/payments";
		}

		com.ecom.model.PaymentNotification notification = paymentNotificationService.sendPaymentReminder(
				paymentId, type);

		if (notification != null) {
			session.setAttribute("succMsg", "Đã gửi nhắc nhở thanh toán thành công!");
		} else {
			session.setAttribute("errorMsg", "Không thể gửi nhắc nhở!");
		}

		return "redirect:/admin/payments";
	}

	@PostMapping("/send-bulk-reminders")
	public String sendBulkReminders(@RequestParam(defaultValue = "IN_APP") String type,
			HttpSession session,
			Principal p) {
		UserDtls loggedInUser = commonUtil.getLoggedInUserDetails(p);

		paymentNotificationService.sendBulkReminders(loggedInUser.getId(), type);

		session.setAttribute("succMsg", "Đã gửi nhắc nhở hàng loạt thành công!");

		return "redirect:/admin/payments";
	}

	@GetMapping("/notifications")
	public String getNotifications(Model m, Principal p) {
		UserDtls loggedInUser = commonUtil.getLoggedInUserDetails(p);

		List<com.ecom.model.PaymentNotification> notifications = paymentNotificationService
				.getNotificationsByOwnerId(loggedInUser.getId());

		m.addAttribute("notifications", notifications);

		return "/admin/notifications";
	}

	// ==================== BOOKING DETAILS ====================

	@GetMapping("/room/{roomId}/details")
	public String getRoomDetails(@PathVariable Integer roomId, Model m, Principal p, HttpSession session) {
		UserDtls loggedInUser = commonUtil.getLoggedInUserDetails(p);

		// Kiểm tra quyền
		Room room = roomService.getRoomById(roomId);
		if (room == null || !room.getOwnerId().equals(loggedInUser.getId())) {
			session.setAttribute("errorMsg", "Bạn không có quyền xem chi tiết phòng này!");
			return "redirect:/admin/rooms";
		}

		// Lấy booking hiện tại (nếu có)
		List<com.ecom.model.RoomBooking> bookings = roomBookingService.getBookingsByRoom(roomId);
		com.ecom.model.RoomBooking activeBooking = bookings.stream()
				.filter(b -> "ACTIVE".equalsIgnoreCase(b.getStatus()))
				.findFirst()
				.orElse(null);

		// Lấy reviews
		List<com.ecom.model.Review> reviews = reviewService.getReviewsByRoomId(roomId);
		Double avgRating = reviewService.getAverageRatingByRoomId(roomId);

		// Lấy payments nếu có booking
		List<com.ecom.model.MonthlyPayment> payments = null;
		if (activeBooking != null) {
			payments = monthlyPaymentService.getPaymentsByBookingId(activeBooking.getId());
		}

		m.addAttribute("room", room);
		m.addAttribute("activeBooking", activeBooking);
		m.addAttribute("allBookings", bookings);
		m.addAttribute("reviews", reviews);
		m.addAttribute("avgRating", avgRating);
		m.addAttribute("payments", payments);

		return "/admin/room_details";
	}

	@GetMapping("/bookings")
	public String getAllBookings(Model m, Principal p,
			@RequestParam(required = false) String status) {
		UserDtls loggedInUser = commonUtil.getLoggedInUserDetails(p);

		List<com.ecom.model.RoomBooking> bookings = roomBookingService.getBookingsByOwner(loggedInUser.getId());

		// Filter by status if provided
		if (status != null && !status.isEmpty()) {
			bookings = bookings.stream()
					.filter(b -> status.equalsIgnoreCase(b.getStatus()))
					.toList();
		}

		// Tính thống kê
		long activeCount = bookings.stream().filter(b -> "ACTIVE".equalsIgnoreCase(b.getStatus())).count();
		long expiredCount = bookings.stream().filter(b -> "EXPIRED".equalsIgnoreCase(b.getStatus())).count();
		long cancelledCount = bookings.stream().filter(b -> "CANCELLED".equalsIgnoreCase(b.getStatus())).count();

		// Tìm bookings sắp hết hạn (trong 30 ngày)
		java.time.LocalDate today = java.time.LocalDate.now();
		java.time.LocalDate thirtyDaysLater = today.plusDays(30);
		List<com.ecom.model.RoomBooking> expiringSoon = bookings.stream()
				.filter(b -> "ACTIVE".equalsIgnoreCase(b.getStatus())
						&& b.getEndDate() != null
						&& b.getEndDate().isAfter(today)
						&& b.getEndDate().isBefore(thirtyDaysLater))
				.toList();

		m.addAttribute("bookings", bookings);
		m.addAttribute("activeCount", activeCount);
		m.addAttribute("expiredCount", expiredCount);
		m.addAttribute("cancelledCount", cancelledCount);
		m.addAttribute("expiringSoon", expiringSoon);
		m.addAttribute("currentStatus", status);

		return "/admin/bookings";
	}

	// ==================== REVIEW MANAGEMENT ====================

	/**
	 * Admin Reviews Page - display all reviews for owner's rooms
	 */
	@GetMapping("/reviews")
	public String viewReviews(Model m, Principal p,
			@RequestParam(defaultValue = "") String roomId,
			@RequestParam(defaultValue = "") String status) {
		UserDtls user = getLoggedInUser(p);

		List<com.ecom.model.Review> reviews;

		if ("ROLE_ADMIN".equals(user.getRole())) {
			// Admin sees all reviews
			reviews = reviewService.getAllReviews();
		} else {
			// Owner sees only their rooms' reviews
			reviews = reviewService.getReviewsByOwnerId(user.getId());
		}

		// Filter by room if specified
		if (!roomId.isEmpty()) {
			Integer rid = Integer.parseInt(roomId);
			reviews = reviews.stream()
					.filter(r -> r.getRoomId().equals(rid))
					.toList();
		}

		// Filter by status (with/without response)
		if ("responded".equals(status)) {
			reviews = reviews.stream()
					.filter(r -> r.getOwnerResponse() != null && !r.getOwnerResponse().isEmpty())
					.toList();
		} else if ("pending".equals(status)) {
			reviews = reviews.stream()
					.filter(r -> r.getOwnerResponse() == null || r.getOwnerResponse().isEmpty())
					.toList();
		}

		// Calculate statistics
		long totalReviews = reviews.size();
		long pendingResponse = reviews.stream()
				.filter(r -> r.getOwnerResponse() == null || r.getOwnerResponse().isEmpty())
				.count();
		double averageRating = reviews.isEmpty() ? 0.0
				: reviews.stream().mapToInt(com.ecom.model.Review::getRating).average().orElse(0.0);

		// Get owner's rooms for filtering
		List<Room> ownerRooms = roomService.getRoomsByOwnerId(user.getId());

		m.addAttribute("reviews", reviews);
		m.addAttribute("totalReviews", totalReviews);
		m.addAttribute("pendingResponse", pendingResponse);
		m.addAttribute("averageRating", averageRating);
		m.addAttribute("ownerRooms", ownerRooms);
		m.addAttribute("selectedRoomId", roomId);
		m.addAttribute("selectedStatus", status);

		return "admin/admin_reviews";
	}

	/**
	 * Submit owner response to a review
	 */
	@PostMapping("/review/{reviewId}/respond")
	public String respondToReview(@PathVariable Integer reviewId,
			@RequestParam String response,
			Principal p,
			HttpSession session) {
		UserDtls user = getLoggedInUser(p);

		// Get review and verify ownership
		com.ecom.model.Review review = reviewService.getReviewById(reviewId);
		if (review == null) {
			setErrorMessage(session, Messages.ERROR_REVIEW_NOT_FOUND);
			return Routes.REDIRECT_REVIEWS;
		}

		// Verify that user owns the room
		Room room = roomService.getRoomById(review.getRoomId());
		if (!isRoomOwner(user, room)) {
			setErrorMessage(session, Messages.ERROR_UNAUTHORIZED_REVIEW);
			return Routes.REDIRECT_REVIEWS;
		}

		// Update owner response
		reviewService.updateOwnerResponse(reviewId, response);
		setSuccessMessage(session, Messages.SUCCESS_REVIEW_RESPONDED);

		return Routes.REDIRECT_REVIEWS;
	}

}
