package com.ecom.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.ecom.model.MonthlyPayment;
import com.ecom.model.PaymentNotification;
import com.ecom.repository.MonthlyPaymentRepository;
import com.ecom.repository.PaymentNotificationRepository;
import com.ecom.service.PaymentNotificationService;

@Service
public class PaymentNotificationServiceImpl implements PaymentNotificationService {

	@Autowired
	private PaymentNotificationRepository notificationRepository;

	@Autowired
	private MonthlyPaymentRepository paymentRepository;

	@Override
	public PaymentNotification saveNotification(PaymentNotification notification) {
		return notificationRepository.save(notification);
	}

	@Override
	public PaymentNotification getNotificationById(Integer id) {
		return notificationRepository.findById(id).orElse(null);
	}

	@Override
	public List<PaymentNotification> getNotificationsByPaymentId(Integer paymentId) {
		return notificationRepository.findByPayment_IdOrderBySentDateDesc(paymentId);
	}

	@Override
	public List<PaymentNotification> getNotificationsByOwnerId(Integer ownerId) {
		return notificationRepository.findByOwnerId(ownerId);
	}

	@Override
	public List<PaymentNotification> getNotificationsByType(String type) {
		return notificationRepository.findByType(type);
	}

	@Override
	public List<PaymentNotification> getNotificationsByStatus(String status) {
		return notificationRepository.findByStatus(status);
	}

	@Override
	public PaymentNotification sendPaymentReminder(Integer paymentId, String type) {
		MonthlyPayment payment = paymentRepository.findById(paymentId).orElse(null);
		if (payment == null) {
			return null;
		}

		// Tạo nội dung thông báo
		String message = createReminderMessage(payment);

		PaymentNotification notification = new PaymentNotification();
		notification.setPayment(payment);
		notification.setType(type != null ? type : "IN_APP");
		notification.setSentDate(LocalDateTime.now());
		notification.setMessage(message);
		notification.setStatus("SENT");

		// Nếu là EMAIL, lấy email của người thuê
		if ("EMAIL".equals(type) && payment.getRoomBooking() != null
				&& payment.getRoomBooking().getUser() != null) {
			notification.setRecipientEmail(payment.getRoomBooking().getUser().getEmail());
		}

		// Nếu là SMS, lấy SĐT của người thuê
		if ("SMS".equals(type) && payment.getRoomBooking() != null
				&& payment.getRoomBooking().getUser() != null) {
			notification.setRecipientPhone(payment.getRoomBooking().getUser().getMobileNumber());
		}

		return notificationRepository.save(notification);
	}

	@Override
	public void sendBulkReminders(Integer ownerId, String type) {
		// Lấy tất cả payment PENDING hoặc OVERDUE của owner
		List<MonthlyPayment> pendingPayments = paymentRepository.findByOwnerIdAndStatus(ownerId, "PENDING");
		List<MonthlyPayment> overduePayments = paymentRepository.findByOwnerIdAndStatus(ownerId, "OVERDUE");

		// Gửi nhắc nhở cho tất cả
		for (MonthlyPayment payment : pendingPayments) {
			sendPaymentReminder(payment.getId(), type);
		}

		for (MonthlyPayment payment : overduePayments) {
			sendPaymentReminder(payment.getId(), type);
		}
	}

	@Override
	public Boolean deleteNotification(Integer id) {
		PaymentNotification notification = notificationRepository.findById(id).orElse(null);
		if (!ObjectUtils.isEmpty(notification)) {
			notificationRepository.delete(notification);
			return true;
		}
		return false;
	}

	@Override
	public Long countNotificationsByPaymentId(Integer paymentId) {
		return notificationRepository.countByPayment_Id(paymentId);
	}

	@Override
	public List<PaymentNotification> getNotificationsByUserId(Integer userId) {
		return notificationRepository.findByPayment_RoomBooking_User_IdOrderBySentDateDesc(userId);
	}

	@Override
	public Long countUnreadNotifications(Integer userId) {
		return notificationRepository.countUnreadByUserId(userId);
	}

	@Override
	public List<PaymentNotification> getRecentNotificationsByUserId(Integer userId) {
		return notificationRepository.findRecentByUserId(userId, org.springframework.data.domain.PageRequest.of(0, 3));
	}

	// Helper method để tạo nội dung thông báo
	private String createReminderMessage(MonthlyPayment payment) {
		StringBuilder sb = new StringBuilder();
		sb.append("Nhắc nhở thanh toán tiền thuê phòng\n");
		sb.append("-------------------------------\n");

		if (payment.getRoomBooking() != null && payment.getRoomBooking().getRoom() != null) {
			sb.append("Phòng: ").append(payment.getRoomBooking().getRoom().getRoomName()).append("\n");
		}

		sb.append("Tháng: ").append(payment.getMonth()).append("/").append(payment.getYear()).append("\n");
		sb.append("Số tiền: ").append(String.format("%,.0f", payment.getAmount())).append(" VND\n");
		sb.append("Hạn thanh toán: ").append(payment.getDueDate()).append("\n");

		if ("OVERDUE".equals(payment.getStatus())) {
			sb.append("\n⚠️ ĐÃ QUÁ HẠN THANH TOÁN\n");
		}

		sb.append("\nVui lòng thanh toán trước ngày đến hạn. Xin cảm ơn!");

		return sb.toString();
	}
}
