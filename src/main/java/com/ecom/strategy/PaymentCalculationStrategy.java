package com.ecom.strategy;

import com.ecom.model.MonthlyPayment;
import com.ecom.model.RoomBooking;

/**
 * Strategy Pattern interface for payment calculation
 * Allows different calculation strategies to be swapped
 * Follows Open/Closed Principle - open for extension, closed for modification
 */
public interface PaymentCalculationStrategy {

    /**
     * Calculate total payment amount
     * 
     * @param payment The payment to calculate
     * @param booking The associated booking
     * @return Total amount to be paid
     */
    Double calculateTotalAmount(MonthlyPayment payment, RoomBooking booking);

    /**
     * Get strategy name for logging/debugging
     */
    String getStrategyName();
}
