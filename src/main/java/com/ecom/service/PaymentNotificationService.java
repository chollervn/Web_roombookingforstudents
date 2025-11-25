package com.ecom.service;

import java.util.List;

import com.ecom.model.PaymentNotification;

public interface PaymentNotificationService {

	public PaymentNotification saveNotification(PaymentNotification notification);

	public PaymentNotification getNotificationById(Integer id);

	public List<PaymentNotification> getNotificationsByPaymentId(Integer paymentId);

	public List<PaymentNotification> getNotificationsByOwnerId(Integer ownerId);

	public List<PaymentNotification> getNotificationsByType(String type);

	public List<PaymentNotification> getNotificationsByStatus(String status);

	public PaymentNotification sendPaymentReminder(Integer paymentId, String type);

	public void sendBulkReminders(Integer ownerId, String type);

	public Boolean deleteNotification(Integer id);

	public List<PaymentNotification> getNotificationsByUserId(Integer userId);

	public Long countUnreadNotifications(Integer userId);

	public List<PaymentNotification> getRecentNotificationsByUserId(Integer userId);

	public Long countNotificationsByPaymentId(Integer paymentId);
}
