package com.ecom.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.ecom.model.Deposit;
import com.ecom.model.Room;
import com.ecom.model.RoomType;
import com.ecom.model.OrderRequest;
import com.ecom.model.RoomOrder;
import com.ecom.model.Review;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.DepositService;
import com.ecom.service.OrderService;
import com.ecom.service.RoomBookingService;
import com.ecom.service.RoomService;
import com.ecom.service.RoomTypeService;
import com.ecom.service.ReviewService;
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

	@Autowired
	private DepositService depositService;

	@Autowired
	private RoomBookingService roomBookingService;

	@Autowired
	private ReviewService reviewService;

	@Autowired
	private com.ecom.service.TenantDashboardService tenantDashboardService;

	@Autowired
	private com.ecom.service.VoucherService voucherService;

	@Autowired
	private com.ecom.service.PaymentNotificationService paymentNotificationService;

	public String addToCart(@RequestParam Integer rid, Principal p, HttpSession session) {
		if (p == null) {
			session.setAttribute("errorMsg", "Vui lòng đăng nhập để thêm vào giỏ hàng");
			return "redirect:/signin";
		}

		UserDtls user = getLoggedInUserDetails(p);
		Cart saveCart = cartService.saveCart(rid, user.getId());

		if (ObjectUtils.isEmpty(saveCart)) {
			session.setAttribute("errorMsg", "Không thể thêm vào giỏ hàng");
		} else {
			session.setAttribute("succMsg", "Đã thêm vào danh sách trọ đã xem");
		}
		return "redirect:/room/" + rid;
	}

	@GetMapping("/cart")
	public String loadCartPage(Principal p, Model m, HttpSession session) {

		// If not logged in, redirect to signin (also avoids NPE when Principal is null)
		if (p == null) {
			return "redirect:/signin";
		}

		UserDtls user = getLoggedInUserDetails(p);

		// Đánh dấu tất cả thông báo là đã xem trong DB
		depositService.markAllAsSeen(user.getId());

		List<Cart> carts = cartService.getCartsByUser(user.getId());
		m.addAttribute("carts", carts);

		// Lấy thông tin đặt cọc của user cho từng phòng
		List<Deposit> userDeposits = depositService.getDepositsByUser(user.getId());
		// Build a map roomId -> latest deposit (based on highest ID = most recent)
		Map<Integer, Deposit> depositMap = new HashMap<>();
		if (userDeposits != null) {
			for (Deposit d : userDeposits) {
				if (d == null || d.getRoom() == null)
					continue;
				Integer rid = d.getRoom().getId();
				Deposit existing = depositMap.get(rid);

				// Always keep the deposit with HIGHER ID (more recent record)
				if (existing == null
						|| (d.getId() != null && existing.getId() != null && d.getId() > existing.getId())) {
					depositMap.put(rid, d);
				}
			}
		}
		m.addAttribute("depositMap", depositMap);

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

	@GetMapping("/deleteCart")
	public String deleteCart(@RequestParam Integer cid, Principal p, HttpSession session) {
		if (p == null) {
			return "redirect:/signin";
		}

		UserDtls user = getLoggedInUserDetails(p);

		// Kiểm tra xem cart này có thuộc về user này không
		Cart cart = cartService.getCartsByUser(user.getId()).stream()
				.filter(c -> c.getId().equals(cid))
				.findFirst()
				.orElse(null);

		if (cart == null) {
			session.setAttribute("errorMsg", "Không tìm thấy trọ trong danh sách");
			return "redirect:/user/cart";
		}

		// Kiểm tra trạng thái đặt cọc - chỉ cho phép xóa nếu chưa cọc hoặc bị từ chối
		List<Deposit> deposits = depositService.getDepositsByUser(user.getId());
		Deposit latestDeposit = null;

		if (deposits != null) {
			for (Deposit d : deposits) {
				if (d.getRoom() != null && d.getRoom().getId().equals(cart.getRoom().getId())) {
					if (latestDeposit == null || (d.getId() != null && latestDeposit.getId() != null
							&& d.getId() > latestDeposit.getId())) {
						latestDeposit = d;
					}
				}
			}
		}

		// Nếu có đặt cọc đang pending hoặc approved thì không cho xóa
		if (latestDeposit != null &&
				(latestDeposit.getStatus().equals("PENDING") || latestDeposit.getStatus().equals("APPROVED"))) {
			session.setAttribute("errorMsg", "Không thể xóa trọ đã đặt cọc hoặc đang chờ duyệt");
			return "redirect:/user/cart";
		}

		// Xóa TẤT CẢ deposit REJECTED của user cho phòng này (để reset hoàn toàn)
		if (deposits != null) {
			for (Deposit d : deposits) {
				if (d.getRoom() != null && d.getRoom().getId().equals(cart.getRoom().getId())
						&& "REJECTED".equals(d.getStatus())) {
					depositService.deleteDeposit(d.getId());
				}
			}
		}

		// Xóa cart
		cartService.deleteCart(cid);
		session.setAttribute("succMsg", "Đã xóa trọ khỏi danh sách");
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
		List<RoomOrder> allOrders = orderService.getOrdersByUser(loginUser.getId());
		List<RoomOrder> rentedOrders = allOrders.stream()
				.filter(o -> "RENTED".equalsIgnoreCase(o.getStatus()))
				.toList();

		// Lấy thông tin booking
		List<com.ecom.model.RoomBooking> bookings = roomBookingService.getBookingsByUser(loginUser.getId());
		List<com.ecom.model.RoomBooking> activeBookings = bookings.stream()
				.filter(b -> "ACTIVE".equalsIgnoreCase(b.getStatus()))
				.toList();

		m.addAttribute("orders", allOrders);
		m.addAttribute("rentedOrders", rentedOrders);
		m.addAttribute("bookings", activeBookings);
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
	public String updateProfile(@ModelAttribute UserDtls user,
			@RequestParam(value = "img", required = false) MultipartFile img, HttpSession session) {
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

	// New: Deposit page for a room (simple flow)
	@GetMapping("/deposit")
	public String depositPage(@RequestParam Integer rid, Model m, Principal p) {
		UserDtls user = getLoggedInUserDetails(p);
		Room room = roomService.getRoomById(rid);
		m.addAttribute("room", room);
		// default deposit amount: if null use 20% of monthly rent
		double suggested = (room.getDeposit() != null) ? room.getDeposit()
				: ((room.getMonthlyRent() != null) ? room.getMonthlyRent() * 0.2 : 0.0);
		m.addAttribute("suggestedDeposit", suggested);

		// Lấy danh sách voucher hợp lệ của user
		List<com.ecom.model.Voucher> validVouchers = voucherService.getUserValidVouchers(user.getId());
		m.addAttribute("vouchers", validVouchers);

		return "/user/deposit";
	}

	@PostMapping("/save-deposit")
	public String saveDeposit(@RequestParam Integer rid,
			@RequestParam(required = false) Double amount,
			@RequestParam(required = false) String paymentMethod,
			@RequestParam(required = false) String note,
			@RequestParam(required = false) String voucherCode,
			Principal p,
			HttpSession session) {
		try {
			UserDtls user = getLoggedInUserDetails(p);
			Room room = roomService.getRoomById(rid);

			// Kiểm tra phòng có tồn tại không
			if (room == null) {
				session.setAttribute("errorMsg", "Phòng trọ không tồn tại!");
				return "redirect:/rooms";
			}

			// Kiểm tra phòng còn khả dụng không - nếu không thì quay lại trang chi tiết
			// phòng với thông báo
			if (!room.getIsAvailable()) {
				session.setAttribute("errorMsg", "Phòng trọ này đã có người thuê! Vui lòng chọn phòng khác.");
				return "redirect:/room/" + rid; // Quay lại trang chi tiết phòng
			}

			// Kiểm tra user đã có yêu cầu đặt cọc pending cho phòng này chưa
			List<Deposit> existingDeposits = depositService.getDepositsByRoom(rid);
			boolean hasPendingDeposit = existingDeposits.stream()
					.anyMatch(d -> "PENDING".equals(d.getStatus()) || "APPROVED".equals(d.getStatus()));

			if (hasPendingDeposit) {
				session.setAttribute("errorMsg",
						"Phòng này đã có yêu cầu đặt cọc đang chờ xử lý hoặc đã được chấp nhận!");
				return "redirect:/room/" + rid; // Quay lại trang chi tiết phòng
			}

			if (amount == null || amount <= 0) {
				session.setAttribute("errorMsg", "Số tiền đặt cọc không hợp lệ");
				return "redirect:/user/deposit?rid=" + rid;
			}

			// Xử lý voucher nếu có
			Double finalAmount = amount;
			Double discountAmount = 0.0;
			com.ecom.model.Voucher appliedVoucher = null;

			if (voucherCode != null && !voucherCode.trim().isEmpty()) {
				com.ecom.model.Voucher voucher = voucherService.getVoucherByCode(voucherCode.trim());

				if (voucher == null) {
					session.setAttribute("errorMsg", "Mã voucher không tồn tại!");
					return "redirect:/user/deposit?rid=" + rid;
				}

				if (!voucher.getUser().getId().equals(user.getId())) {
					session.setAttribute("errorMsg", "Mã voucher này không thuộc về bạn!");
					return "redirect:/user/deposit?rid=" + rid;
				}

				if (!voucher.isValid()) {
					session.setAttribute("errorMsg", "Mã voucher đã hết hạn hoặc đã được sử dụng!");
					return "redirect:/user/deposit?rid=" + rid;
				}

				// Áp dụng giảm giá
				discountAmount = amount * voucher.getDiscountPercent() / 100.0;
				finalAmount = amount - discountAmount;
				appliedVoucher = voucher;

				// Đánh dấu voucher đã sử dụng
				voucherService.applyVoucher(voucherCode.trim(), user.getId());
			}

			// Create deposit record
			Deposit deposit = new Deposit();
			deposit.setUser(user);
			deposit.setRoom(room);
			deposit.setOriginalAmount(amount); // Số tiền gốc
			deposit.setDiscountAmount(discountAmount); // Số tiền giảm
			deposit.setAmount(finalAmount); // Số tiền cuối cùng sau giảm giá
			deposit.setDepositDate(LocalDate.now());
			deposit.setStatus("PENDING"); // Trạng thái chờ chủ trọ xác nhận
			deposit.setPaymentMethod(paymentMethod != null ? paymentMethod : "CASH");
			deposit.setNote(note);
			deposit.setVoucher(appliedVoucher); // Lưu voucher đã sử dụng

			Deposit savedDeposit = depositService.saveDeposit(deposit);

			if (savedDeposit != null) {
				String message = "Yêu cầu đặt cọc đã được gửi! Vui lòng chờ chủ trọ xác nhận.";
				if (discountAmount > 0) {
					message += " Bạn đã tiết kiệm được " + String.format("%,.0f", discountAmount) + " VNĐ với voucher!";
				}
				session.setAttribute("succMsg", message);
				return "redirect:/user/cart";
			} else {
				session.setAttribute("errorMsg", "Có lỗi xảy ra khi gửi yêu cầu đặt cọc!");
				return "redirect:/user/deposit?rid=" + rid;
			}
		} catch (Exception e) {
			session.setAttribute("errorMsg", "Có lỗi xảy ra: " + e.getMessage());
			return "redirect:/user/cart"; // Quay về cart khi có lỗi
		}
	}

	@GetMapping("/deposit-success")
	public String depositSuccess(@RequestParam Integer did, Model m, Principal p) {
		Deposit deposit = depositService.getDepositById(did);
		if (deposit == null) {
			return "redirect:/user/cart";
		}
		m.addAttribute("deposit", deposit);
		return "/user/deposit_success";
	}

	@PostMapping("/save-review")
	public String saveReview(@RequestParam Integer roomId,
			@RequestParam Integer rating,
			@RequestParam String comment,
			Principal p,
			HttpSession session) {
		try {
			if (p == null) {
				session.setAttribute("errorMsg", "Vui lòng đăng nhập để đánh giá");
				return "redirect:/signin";
			}

			UserDtls user = getLoggedInUserDetails(p);

			// Kiểm tra xem user đã đánh giá phòng này chưa
			Boolean hasReviewed = reviewService.hasUserReviewedRoom(roomId, user.getId());
			if (hasReviewed) {
				session.setAttribute("errorMsg", "Bạn đã đánh giá phòng này rồi!");
				return "redirect:/room/" + roomId;
			}

			Review review = new Review();
			review.setRoomId(roomId);
			review.setUserId(user.getId());
			review.setUserName(user.getName());
			review.setRating(rating);
			review.setComment(comment);

			reviewService.saveReview(review);
			session.setAttribute("succMsg", "Đánh giá của bạn đã được gửi thành công!");

		} catch (Exception e) {
			session.setAttribute("errorMsg", "Có lỗi xảy ra: " + e.getMessage());
		}

		return "redirect:/room/" + roomId;
	}

	@GetMapping("/delete-review")
	public String deleteReview(@RequestParam Integer reviewId,
			@RequestParam Integer roomId,
			Principal p,
			HttpSession session) {
		try {
			if (p == null) {
				session.setAttribute("errorMsg", "Vui lòng đăng nhập");
				return "redirect:/signin";
			}

			UserDtls user = getLoggedInUserDetails(p);
			Review review = reviewService.getReviewById(reviewId);

			// Kiểm tra xem review có thuộc về user này không
			if (review == null || !review.getUserId().equals(user.getId())) {
				session.setAttribute("errorMsg", "Không thể xóa đánh giá này");
				return "redirect:/room/" + roomId;
			}

			reviewService.deleteReview(reviewId);
			session.setAttribute("succMsg", "Đã xóa đánh giá thành công");

		} catch (Exception e) {
			session.setAttribute("errorMsg", "Có lỗi xảy ra: " + e.getMessage());
		}

		return "redirect:/room/" + roomId;
	}

	/**
	 * NEW: Tenant Dashboard - My Rental page
	 * Shows comprehensive rental information using TenantDashboardService
	 */
	@GetMapping("/my-rental")
	public String myRental(Model m, Principal p, HttpSession session) {
		if (p == null) {
			return "redirect:/signin";
		}

		UserDtls user = getLoggedInUserDetails(p);

		// Use TenantDashboardService to get aggregated data (Facade pattern)
		com.ecom.dto.TenantDashboardDTO dashboard = tenantDashboardService.getDashboardData(user.getId());

		m.addAttribute("dashboard", dashboard);

		// Check if user has active rental
		if (dashboard.getCurrentRoom() == null) {
			session.setAttribute("errorMsg", "Bạn chưa thuê phòng nào");
		}

		return "/user/my_rental";
	}

	/**
	 * Notification Management
	 */
	@GetMapping("/notifications")
	public String viewNotifications(Model m, Principal p) {
		UserDtls loggedInUser = getLoggedInUserDetails(p);

		List<com.ecom.model.PaymentNotification> notifications = paymentNotificationService
				.getNotificationsByUserId(loggedInUser.getId());
		m.addAttribute("notifications", notifications);

		return "/user/notifications";
	}

	@GetMapping("/notification/delete")
	public String deleteNotification(@RequestParam Integer id, Principal p) {
		paymentNotificationService.deleteNotification(id);
		return "redirect:/user/notifications";
	}

}
