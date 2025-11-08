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
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collector;

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
import com.ecom.model.RoomType;
import com.ecom.model.Room;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.DepositService;
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
	public String rooms(Model m, @RequestParam(value = "roomType", defaultValue = "") String roomType,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "12") Integer pageSize,
			@RequestParam(defaultValue = "") String ch) {

		// Get unique room types and remove duplicates
		List<RoomType> allRoomTypes = roomTypeService.getAllActiveRoomType();
		List<RoomType> uniqueRoomTypes = allRoomTypes.stream()
				.distinct()
				.collect(java.util.stream.Collectors.toList());

		m.addAttribute("paramValue", roomType);
		m.addAttribute("roomTypes", uniqueRoomTypes);

		Page<Room> page = null;
		if (StringUtils.isEmpty(ch)) {
			page = roomService.getAllActiveRoomPagination(pageNo, pageSize, roomType);
		} else {
			page = roomService.searchActiveRoomPagination(pageNo, pageSize, roomType, ch);
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
	public String room(@PathVariable int id, Model m) {
		Room roomById = roomService.getRoomById(id);
		m.addAttribute("room", roomById);

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
	public String saveUser(@ModelAttribute UserDtls user, @RequestParam(value = "img", required = false) MultipartFile file, Model model, HttpSession session)
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
						Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
								+ file.getOriginalFilename());
						Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
					} catch (Exception e) {
						// Log error but don't fail registration
						System.out.println("Could not save profile image: " + e.getMessage());
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

			String url = CommonUtil.generateUrl(request) + "/reset-password?token=" + resetToken;


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

}
