package com.ecom.strategy;

import org.springframework.stereotype.Component;

import com.ecom.model.MonthlyPayment;
import com.ecom.model.RoomBooking;

/**
 * Standard payment calculation strategy
 * Calculates: Rent + Electricity (usage * rate) + Water (usage * rate) +
 * Additional Fees
 */
@Component
public class StandardPaymentCalculation implements PaymentCalculationStrategy {

    @Override
    public Double calculateTotalAmount(MonthlyPayment payment, RoomBooking booking) {
        if (payment == null || booking == null) {
            return 0.0;
        }

        Double total = 0.0;

        // Base rent
        if (booking.getMonthlyRent() != null) {
            total += booking.getMonthlyRent();
        }

        // Electricity cost
        if (payment.getElectricityUsed() != null && booking.getRoom() != null
                && booking.getRoom().getElectricityCost() != null) {
            total += payment.getElectricityUsed() * booking.getRoom().getElectricityCost();
        }

        // Water cost
        if (payment.getWaterUsed() != null && booking.getRoom() != null
                && booking.getRoom().getWaterCost() != null) {
            total += payment.getWaterUsed() * booking.getRoom().getWaterCost();
        }

        // Additional fees (internet, parking, etc.)
        if (payment.getAdditionalFees() != null) {
            total += payment.getAdditionalFees();
        }

        return total;
    }

    @Override
    public String getStrategyName() {
        return "Standard Calculation (Rent + Utilities + Fees)";
    }
}
