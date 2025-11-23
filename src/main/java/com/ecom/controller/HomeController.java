package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Cart;
import com.ecom.model.Deposit;
import com.ecom.model.Review;
import com.ecom.model.RoomType;
import com.ecom.model.Room;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.DepositService;
import com.ecom.service.ReviewService;
import com.ecom.service.RoomTypeService;
import com.ecom.service.RoomService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;

import io.micrometer.common.util.StringUtils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	@Autowired
	private RoomTypeService roomTypeService;

	@Autowired
	private RoomService roomService;

	@Autowired
	private UserService userService;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private CartService cartService;

	@Autowired
	private DepositService depositService;

	@Autowired
	private ReviewService reviewService;

	@Autowired
	private com.ecom.service.VoucherService voucherService;

	@Autowired
	private com.ecom.service.GameService gameService;

	@ModelAttribute
	public void getUserDetails(Principal p, Model m, HttpSession session) {
		if (p != null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
			Integer countCart = cartService.getCountCart(userDtls.getId());
			m.addAttribute("countCart", countCart);

			// Đếm số thông báo cho USER: deposit được approved/rejected hôm nay
			if ("ROLE_USER".equals(userDtls.getRole())) {
				// Kiểm tra xem user đã xem thông báo chưa (kiểm tra session)
				Boolean hasViewed = (Boolean) session.getAttribute("depositNotificationsViewed_" + userDtls.getId());

				if (hasViewed == null || !hasViewed) {
					// Chỉ đếm thông báo nếu chưa xem
					List<Deposit> userDeposits = depositService.getDepositsByUser(userDtls.getId());
					java.time.LocalDate today = java.time.LocalDate.now();
					long unreadDepositNotifications = userDeposits.stream()
							.filter(d -> ("APPROVED".equals(d.getStatus()) || "REJECTED".equals(d.getStatus()))
									&& d.getApprovedDate() != null
									&& d.getApprovedDate().equals(today))
							.count();
					m.addAttribute("unreadDepositNotifications", unreadDepositNotifications);
				} else {
					// Đã xem rồi, không hiển thị badge
					m.addAttribute("unreadDepositNotifications", 0L);
				}
			}

			// Đếm số đơn đặt cọc chờ xử lý cho ADMIN/OWNER
			if ("ROLE_ADMIN".equals(userDtls.getRole()) || "ROLE_OWNER".equals(userDtls.getRole())) {
				List<Deposit> deposits = depositService.getDepositsByOwner(userDtls.getId());
				long pendingDepositCount = deposits.stream()
						.filter(d -> "PENDING".equals(d.getStatus()))
						.count();
				m.addAttribute("pendingDepositCount", pendingDepositCount);
			}
		}

		List<RoomType> allActiveRoomType = roomTypeService.getAllActiveRoomType();
		m.addAttribute("roomTypes", allActiveRoomType);
	}

	@GetMapping("/")
	public String index(Model m) {

		List<RoomType> allActiveRoomType = roomTypeService.getAllActiveRoomType().stream()
				.sorted((c1, c2) -> c2.getId().compareTo(c1.getId())).limit(6).toList();
		List<Room> allActiveRooms = roomService.getAllActiveRooms("").stream()
				.sorted((p1, p2) -> p2.getId().compareTo(p1.getId())).limit(8).toList();
		m.addAttribute("roomTypes", allActiveRoomType);
		m.addAttribute("rooms", allActiveRooms);
		return "index";
	}

	@GetMapping("/signin")
	public String login() {
		return "login";
	}

	@GetMapping("/register")
	public String register() {
		return "register";
	}

	@GetMapping("/rooms")
	public String rooms(Model m,
			@RequestParam(value = "roomType", defaultValue = "") String roomType,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "12") Integer pageSize,
			@RequestParam(defaultValue = "") String ch,
			@RequestParam(name = "sortBy", defaultValue = "") String sortBy,
			@RequestParam(name = "city", defaultValue = "") String city,
			@RequestParam(name = "minPrice", required = false) Double minPrice,
			@RequestParam(name = "maxPrice", required = false) Double maxPrice) {

		// Get unique room types and remove duplicates
		List<RoomType> allRoomTypes = roomTypeService.getAllActiveRoomType();
		List<RoomType> uniqueRoomTypes = allRoomTypes.stream()
				.distinct()
				.collect(java.util.stream.Collectors.toList());

		// Get unique cities for filter
		List<String> cities = roomService.getAllActiveRooms("").stream()
				.map(Room::getCity)
				.distinct()
				.sorted()
				.collect(java.util.stream.Collectors.toList());

		m.addAttribute("paramValue", roomType);
		m.addAttribute("roomTypes", uniqueRoomTypes);
		m.addAttribute("cities", cities);
		m.addAttribute("sortBy", sortBy);
		m.addAttribute("selectedCity", city);
		m.addAttribute("minPrice", minPrice);
		m.addAttribute("maxPrice", maxPrice);

		Page<Room> page = null;
		if (StringUtils.isEmpty(ch)) {
			page = roomService.getAllActiveRoomPagination(pageNo, pageSize, roomType, sortBy, city, minPrice, maxPrice);
		} else {
			page = roomService.searchActiveRoomPagination(pageNo, pageSize, roomType, ch, sortBy, city, minPrice,
					maxPrice);
		}

		List<Room> rooms = page.getContent();
		m.addAttribute("rooms", rooms);
		m.addAttribute("roomsSize", rooms.size());

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "room";
	}

	@GetMapping("/room/{id}")
	public String room(@PathVariable int id, Model m, Principal p) {
		Room roomById = roomService.getRoomById(id);
		m.addAttribute("room", roomById);

		// Lấy thông tin review của phòng
		List<Review> reviews = reviewService.getReviewsByRoomId(id);
		Long reviewCount = reviewService.getReviewCountByRoomId(id);
		Double averageRating = reviewService.getAverageRatingByRoomId(id);

		m.addAttribute("reviews", reviews);
		m.addAttribute("reviewCount", reviewCount);
		m.addAttribute("averageRating", averageRating);

		// Kiểm tra user đã review chưa (nếu đã đăng nhập)
		if (p != null) {
			String email = p.getName();
			UserDtls user = userService.getUserByEmail(email);
			Boolean hasReviewed = reviewService.hasUserReviewedRoom(id, user.getId());
			m.addAttribute("hasReviewed", hasReviewed);
			m.addAttribute("currentUserId", user.getId());
		}

		// Kiểm tra trạng thái đặt cọc của phòng
		List<Deposit> roomDeposits = depositService.getDepositsByRoom(id);

		// Thứ tự ưu tiên trạng thái: RENTED > APPROVED > PENDING > AVAILABLE
		String roomStatus = "AVAILABLE"; // Mặc định
		String statusMessage = "";
		String statusIcon = "fa-check-circle";
		String statusClass = "success";
		String contactName = roomById.getContactName();
		String contactPhone = roomById.getContactPhone();

		if (!roomById.getIsAvailable()) {
			// Trạng thái 1: Phòng đã được thuê (ưu tiên cao nhất)
			roomStatus = "RENTED";
			statusMessage = "Phòng đã được thuê";
			statusIcon = "fa-ban";
			statusClass = "danger";
		} else {
			// Kiểm tra APPROVED deposit
			Deposit approvedDeposit = roomDeposits.stream()
					.filter(d -> "APPROVED".equals(d.getStatus()))
					.findFirst()
					.orElse(null);

			if (approvedDeposit != null) {
				// Trạng thái 2: Đã có người đặt cọc được duyệt
				roomStatus = "APPROVED_DEPOSIT";
				statusMessage = "Đã có người đặt cọc được duyệt";
				statusIcon = "fa-lock";
				statusClass = "warning";
				if (approvedDeposit.getUser() != null) {
					contactName = approvedDeposit.getUser().getName();
					contactPhone = approvedDeposit.getUser().getMobileNumber();
				}
			} else {
				// Kiểm tra PENDING deposit
				Deposit pendingDeposit = roomDeposits.stream()
						.filter(d -> "PENDING".equals(d.getStatus()))
						.findFirst()
						.orElse(null);

				if (pendingDeposit != null) {
					// Trạng thái 3: Đang chờ duyệt đặt cọc
					roomStatus = "PENDING_DEPOSIT";
					statusMessage = "Đang chờ duyệt đặt cọc";
					statusIcon = "fa-hourglass-half";
					statusClass = "info";
				}
			}
		}

		m.addAttribute("roomStatus", roomStatus);
		m.addAttribute("statusMessage", statusMessage);
		m.addAttribute("statusIcon", statusIcon);
		m.addAttribute("statusClass", statusClass);
		m.addAttribute("contactName", contactName);
		m.addAttribute("contactPhone", contactPhone);

		return "view_room";
	}

	// Endpoint thêm vào cart từ trang public
	@GetMapping("/addCart")
	public String addToCart(@RequestParam Integer rid, Principal p, HttpSession session) {
		if (p == null) {
			session.setAttribute("errorMsg", "Vui lòng đăng nhập để thêm vào giỏ hàng");
			return "redirect:/signin";
		}

		String email = p.getName();
		UserDtls user = userService.getUserByEmail(email);
		Cart saveCart = cartService.saveCart(rid, user.getId());

		if (ObjectUtils.isEmpty(saveCart)) {
			session.setAttribute("errorMsg", "Không thể thêm vào giỏ hàng");
		} else {
			session.setAttribute("succMsg", "Đã thêm vào danh sách trọ đã xem");
		}
		return "redirect:/room/" + rid;
	}

	@PostMapping("/saveUser")
	public String saveUser(@ModelAttribute UserDtls user,
			@RequestParam(value = "img", required = false) MultipartFile file, Model model, HttpSession session)
			throws IOException {
		Boolean existsEmail = userService.existsEmail(user.getEmail());
		if (existsEmail) {
			model.addAttribute("errorMsg", "Email đã tồn tại!");
			return "register";
		} else {
			String imageName = (file == null || file.isEmpty()) ? "default.jpg" : file.getOriginalFilename();
			user.setProfileImage(imageName);

			// Ensure accountType is set properly - default to renter if not specified
			if (user.getAccountType() == null || user.getAccountType().isEmpty()) {
				user.setAccountType("renter");
			}

			UserDtls saveUser = userService.saveUser(user);
			if (!ObjectUtils.isEmpty(saveUser)) {
				if (file != null && !file.isEmpty()) {
					try {
						File saveFile = new ClassPathResource("static/img").getFile();
						Path path = Paths
								.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
										+ file.getOriginalFilename());
						Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
					} catch (Exception e) {
						// Log error but don't fail registration
						logger.error("Could not save profile image: ", e);
					}
				}

				// Set success message in session and redirect directly to login page
				session.setAttribute("succMsg", "Đăng ký thành công! Vui lòng đăng nhập với tài khoản mới.");
				return "redirect:/signin";
			} else {
				model.addAttribute("errorMsg", "Có lỗi xảy ra trong quá trình đăng ký. Vui lòng thử lại.");
				return "register";
			}
		}
	}

	@GetMapping("/forgot-password")
	public String showForgotPassword() {
		return "forgot_password.html";
	}

	@PostMapping("/forgot-password")
	public String processForgotPassword(@RequestParam String email, HttpSession session, HttpServletRequest request)
			throws UnsupportedEncodingException, MessagingException {

		UserDtls userByEmail = userService.getUserByEmail(email);

		if (ObjectUtils.isEmpty(userByEmail)) {
			session.setAttribute("errorMsg", "Invalid email");
		} else {

			String resetToken = UUID.randomUUID().toString();
			userService.updateUserResetToken(email, resetToken);

			// String url = CommonUtil.generateUrl(request) + "/reset-password?token=" +
			// resetToken;
			// TODO: Send email with url

		}

		return "redirect:/forgot-password";
	}

	@GetMapping("/reset-password")
	public String showResetPassword(@RequestParam String token, HttpSession session, Model m) {

		UserDtls userByToken = userService.getUserByToken(token);

		if (userByToken == null) {
			m.addAttribute("msg", "Your link is invalid or expired !!");
			return "message";
		}
		m.addAttribute("token", token);
		return "reset_password";
	}

	@PostMapping("/reset-password")
	public String resetPassword(@RequestParam String token, @RequestParam String password, HttpSession session,
			Model m) {

		UserDtls userByToken = userService.getUserByToken(token);
		if (userByToken == null) {
			m.addAttribute("errorMsg", "Your link is invalid or expired !!");
			return "message";
		} else {
			userByToken.setPassword(passwordEncoder.encode(password));
			userByToken.setResetToken(null);
			userService.updateUser(userByToken);
			m.addAttribute("msg", "Password change successfully");

			return "message";
		}

	}

	@GetMapping("/search")
	public String searchRoom(@RequestParam String ch, Model m) {
		List<Room> searchRooms = roomService.searchRoom(ch);
		m.addAttribute("rooms", searchRooms);
		List<RoomType> roomTypes = roomTypeService.getAllActiveRoomType();
		m.addAttribute("roomTypes", roomTypes);
		return "product";

	}

	@GetMapping("/register-success")
	public String registerSuccess(@RequestParam(value = "type", defaultValue = "renter") String accountType,
			Model model, HttpSession session) {

		// Get success message from session
		String successMsg = (String) session.getAttribute("succMsg");
		if (successMsg != null) {
			model.addAttribute("succMsg", successMsg);
			session.removeAttribute("succMsg");
		}

		model.addAttribute("accountType", accountType);

		return "register_success";
	}

	// Duck Race Minigame endpoints
	@GetMapping("/minigame/duck-race")
	public String showDuckRace(Model m, Principal p) {
		if (p != null) {
			String email = p.getName();
			UserDtls user = userService.getUserByEmail(email);
			m.addAttribute("user", user);
		}
		return "minigame/duck_race";
	}

	@PostMapping("/minigame/duck-race/play")
	@org.springframework.web.bind.annotation.ResponseBody
	public java.util.Map<String, Object> playDuckRace(
			@org.springframework.web.bind.annotation.RequestBody java.util.Map<String, Integer> request,
			Principal p) {

		java.util.Map<String, Object> response = new java.util.HashMap<>();

		if (p == null) {
			response.put("success", false);
			response.put("message", "Vui lòng đăng nhập");
			return response;
		}

		Integer selectedDuck = request.get("selectedDuck");
		Integer winningDuck = new java.util.Random().nextInt(6) + 1; // Random 1-6

		String email = p.getName();
		UserDtls user = userService.getUserByEmail(email);

		com.ecom.model.GameRecord record = gameService.recordGamePlay(
				Long.valueOf(user.getId()), selectedDuck, winningDuck);

		response.put("success", true);
		response.put("won", selectedDuck.equals(winningDuck));
		response.put("winningDuck", winningDuck);

		if (selectedDuck.equals(winningDuck)) {
			com.ecom.model.Voucher voucher = record.getVoucher();
			response.put("voucher", java.util.Map.of(
					"code", voucher.getCode(),
					"discount", voucher.getDiscountPercent()));
		}

		return response;
	}

}
