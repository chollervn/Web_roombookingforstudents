package com.ecom.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ecom.model.MonthlyPayment;
import com.ecom.model.PaymentNotification;
import com.ecom.repository.MonthlyPaymentRepository;

/**
 * Factory Pattern for creating notifications
 * Centralizes notification creation logic
 * Follows Single Responsibility - only creates notifications
 * Updated to work with existing PaymentNotification structure
 */
@Component
public class NotificationFactory {

    @Autowired
    private MonthlyPaymentRepository paymentRepository;

    /**
     * Create a payment reminder notification
     */
    public PaymentNotification createPaymentReminder(Integer paymentId, String message) {
        PaymentNotification notification = new PaymentNotification();

        // Find payment and set it
        paymentRepository.findById(paymentId).ifPresent(notification::setPayment);

        notification.setType("IN_APP");
        notification.setMessage(message);
        notification.setStatus("SENT");
        return notification;
    }

    /**
     * Create a payment confirmation notification
     */
    public PaymentNotification createPaymentConfirmation(Integer paymentId, Double amount) {
        PaymentNotification notification = new PaymentNotification();

        // Find payment and set it
        paymentRepository.findById(paymentId).ifPresent(notification::setPayment);

        notification.setType("IN_APP");
        notification.setMessage(String.format("Payment của bạn %.0f VNĐ đã được xác nhận", amount));
        notification.setStatus("SENT");
        return notification;
    }

    /**
     * Create an overdue payment notification
     */
    public PaymentNotification createOverdueNotification(Integer paymentId, String dueDate) {
        PaymentNotification notification = new PaymentNotification();

        // Find payment and set it
        paymentRepository.findById(paymentId).ifPresent(notification::setPayment);

        notification.setType("IN_APP");
        notification.setMessage(String.format("Thanh toán quá hạn từ ngày %s. Vui lòng thanh toán sớm!", dueDate));
        notification.setStatus("SENT");
        return notification;
    }

    /**
     * Create a generic notification (not linked to payment)
     */
    public PaymentNotification createNotification(String type, String message) {
        PaymentNotification notification = new PaymentNotification();
        notification.setType(type);
        notification.setMessage(message);
        notification.setStatus("SENT");
        return notification;
    }
}
