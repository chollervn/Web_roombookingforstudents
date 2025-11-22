package com.ecom.strategy;

import org.springframework.stereotype.Component;

import com.ecom.model.MonthlyPayment;
import com.ecom.model.RoomBooking;

/**
 * Flat rate payment calculation strategy
 * All utilities included in the rent - simpler calculation
 */
@Component
public class FlatRatePaymentCalculation implements PaymentCalculationStrategy {

    @Override
    public Double calculateTotalAmount(MonthlyPayment payment, RoomBooking booking) {
        if (payment == null || booking == null) {
            return 0.0;
        }

        // Just return monthly rent - utilities are included
        return booking.getMonthlyRent() != null ? booking.getMonthlyRent() : 0.0;
    }

    @Override
    public String getStrategyName() {
        return "Flat Rate (All Inclusive)";
    }
}
