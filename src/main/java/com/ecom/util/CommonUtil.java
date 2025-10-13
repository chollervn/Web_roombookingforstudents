package com.ecom.util;

import java.io.UnsupportedEncodingException;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.ecom.model.RoomOrder;
import com.ecom.model.UserDtls;
import com.ecom.service.UserService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CommonUtil {

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private UserService userService;

	public Boolean sendMail(String url, String reciepentEmail) throws UnsupportedEncodingException, MessagingException {

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom("nguyencongphu3020@gmail.com", "Shopping Cart");
		helper.setTo(reciepentEmail);

		String content = "<p>Hello,</p>" + "<p>You have requested to reset your password.</p>"
				+ "<p>Click the link below to change your password:</p>" + "<p><a href=\"" + url
				+ "\">Change my password</a></p>";
		helper.setSubject("Password Reset");
		helper.setText(content, true);

		mailSender.send(message);
		return true;
	}

	public static String generateUrl(HttpServletRequest request) {

		// http://localhost:8080/forgot-password
		String siteUrl = request.getRequestURL().toString();
		return siteUrl.replace(request.getServletPath(), "");
	}

	String msg = null;

	public Boolean sendMailForRoomOrder(RoomOrder order, String status) throws Exception {

		msg = "<p>Hello [[name]],</p>"
				+ "<p>Thank you for booking room with us. Your room booking details are below:</p>"
				+ "<p><b>Room Details:</b></p>"
				+ "<p>Room Name: [[roomName]]</p>"
				+ "<p>Room Type: [[roomType]]</p>"
				+ "<p>Monthly Rent: [[price]]</p>"
				+ "<p>Booking Duration: [[quantity]] months</p>"
				+ "<p>Total Amount: [[totalPrice]]</p>"
				+ "<p>Booking Status: [[orderStatus]]</p>";

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom("nguyencongphu3020@gmail.com", "Room Rental System");
		helper.setTo(order.getOrderAddress().getEmail());

		msg = msg.replace("[[name]]", order.getOrderAddress().getFirstName());
		msg = msg.replace("[[roomName]]", order.getRoom().getRoomName());
		msg = msg.replace("[[roomType]]", order.getRoom().getRoomType());
		msg = msg.replace("[[price]]", order.getPrice().toString());
		msg = msg.replace("[[quantity]]", order.getQuantity().toString());
		msg = msg.replace("[[totalPrice]]", String.valueOf(order.getQuantity() * order.getPrice()));
		msg = msg.replace("[[orderStatus]]", status);

		helper.setSubject("Room Booking Status");
		helper.setText(msg, true);

		mailSender.send(message);
		return true;
	}

	public UserDtls getLoggedInUserDetails(Principal p) {
		String email = p.getName();
		UserDtls userDtls = userService.getUserByEmail(email);
		return userDtls;
	}

}
