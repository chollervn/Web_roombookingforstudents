package com.ecom.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.dto.LandlordContactDTO;
import com.ecom.dto.PaymentDTO;
import com.ecom.dto.RentalStatusDTO;
import com.ecom.dto.RoomDTO;
import com.ecom.dto.TenantDashboardDTO;
import com.ecom.model.Conversation;
import com.ecom.model.MonthlyPayment;
import com.ecom.model.Room;
import com.ecom.model.RoomBooking;
import com.ecom.model.UserDtls;
import com.ecom.repository.ConversationRepository;
import com.ecom.repository.UserRepository;
import com.ecom.service.MonthlyPaymentService;
import com.ecom.service.RoomBookingService;
import com.ecom.service.TenantDashboardService;

/**
 * Implementation of TenantDashboardService
 * Aggregates data from multiple services for the tenant dashboard
 * Follows Single Responsibility - only aggregates tenant dashboard data
 */
@Service
public class TenantDashboardServiceImpl implements TenantDashboardService {

    @Autowired
    private RoomBookingService bookingService;

    @Autowired
    private MonthlyPaymentService paymentService;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public TenantDashboardDTO getDashboardData(Integer tenantId) {
        // Get active booking
        List<RoomBooking> bookings = bookingService.getBookingsByUser(tenantId);
        RoomBooking activeBooking = bookings.stream()
                .filter(b -> "ACTIVE".equals(b.getStatus()))
                .findFirst()
                .orElse(null);

        if (activeBooking == null) {
            // No active rental
            return TenantDashboardDTO.builder()
                    .currentRoom(null)
                    .rentalStatus(null)
                    .build();
        }

        // Convert room to DTO
        RoomDTO roomDTO = toRoomDTO(activeBooking.getRoom());

        // Convert rental status to DTO
        RentalStatusDTO statusDTO = toRentalStatusDTO(activeBooking);

        // Get payments for this booking
        List<MonthlyPayment> payments = paymentService.getPaymentsByBookingId(activeBooking.getId());

        // Find next upcoming payment
        PaymentDTO nextPayment = payments.stream()
                .filter(p -> "PENDING".equals(p.getStatus()) || "OVERDUE".equals(p.getStatus()))
                .map(this::toPaymentDTO)
                .min((p1, p2) -> p1.getDueDate().compareTo(p2.getDueDate()))
                .orElse(null);

        // Get upcoming payments (next 3 months)
        List<PaymentDTO> upcomingPayments = payments.stream()
                .filter(p -> "PENDING".equals(p.getStatus()) || "OVERDUE".equals(p.getStatus()))
                .map(this::toPaymentDTO)
                .sorted((p1, p2) -> p1.getDueDate().compareTo(p2.getDueDate()))
                .limit(3)
                .collect(Collectors.toList());

        // Get payment history (last 6 months, paid only)
        List<PaymentDTO> paymentHistory = payments.stream()
                .filter(p -> "PAID".equals(p.getStatus()))
                .map(this::toPaymentDTO)
                .sorted((p1, p2) -> p2.getPaidDate().compareTo(p1.getPaidDate()))
                .limit(6)
                .collect(Collectors.toList());

        // Calculate totals
        Double totalPaid = payments.stream()
                .filter(p -> "PAID".equals(p.getStatus()))
                .mapToDouble(p -> p.getPaidAmount() != null ? p.getPaidAmount() : 0.0)
                .sum();

        Double totalPending = payments.stream()
                .filter(p -> !"PAID".equals(p.getStatus()))
                .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0)
                .sum();

        // Get landlord contact info
        LandlordContactDTO landlordContact = null;
        if (activeBooking.getRoom() != null && activeBooking.getRoom().getOwnerId() != null) {
            landlordContact = getLandlordContact(activeBooking.getRoom().getOwnerId());
        }

        // Get conversation info
        Conversation conversation = conversationRepository.findByRoomBookingId(activeBooking.getId());
        Integer conversationId = conversation != null ? conversation.getId() : null;
        Integer unreadMessages = conversation != null ? conversation.getUnreadTenantCount() : 0;

        // Build and return dashboard DTO
        return TenantDashboardDTO.builder()
                .currentRoom(roomDTO)
                .rentalStatus(statusDTO)
                .nextPayment(nextPayment)
                .upcomingPayments(upcomingPayments)
                .paymentHistory(paymentHistory)
                .totalPaid(totalPaid)
                .totalPending(totalPending)
                .landlordContact(landlordContact)
                .conversationId(conversationId)
                .unreadMessages(unreadMessages)
                .build();
    }

    @Override
    public boolean hasActiveRental(Integer tenantId) {
        List<RoomBooking> bookings = bookingService.getBookingsByUser(tenantId);
        return bookings.stream().anyMatch(b -> "ACTIVE".equals(b.getStatus()));
    }

    @Override
    public Integer getActiveBookingId(Integer tenantId) {
        List<RoomBooking> bookings = bookingService.getBookingsByUser(tenantId);
        return bookings.stream()
                .filter(b -> "ACTIVE".equals(b.getStatus()))
                .map(RoomBooking::getId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Helper: Convert Room entity to DTO
     */
    private RoomDTO toRoomDTO(Room room) {
        if (room == null) {
            return null;
        }

        return RoomDTO.builder()
                .id(room.getId())
                .roomName(room.getRoomName())
                .description(room.getDescription())
                .roomType(room.getRoomType())
                .monthlyRent(room.getMonthlyRent())
                .address(room.getAddress())
                .fullAddress(room.getFullAddress())
                .district(room.getDistrict())
                .city(room.getCity())
                .area(room.getArea())
                .maxOccupants(room.getMaxOccupants())
                .image(room.getImage())
                .hasWifi(room.getHasWifi())
                .hasAirConditioner(room.getHasAirConditioner())
                .hasParking(room.getHasParking())
                .hasElevator(room.getHasElevator())
                .allowPets(room.getAllowPets())
                .deposit(room.getDeposit())
                .electricityCost(room.getElectricityCost())
                .waterCost(room.getWaterCost())
                .contactPhone(room.getContactPhone())
                .contactName(room.getContactName())
                .latitude(room.getLatitude())
                .longitude(room.getLongitude())
                .status(room.getRoomStatus() != null ? room.getRoomStatus().getDisplayName() : null)
                .isAvailable(room.getIsAvailable())
                .build();
    }

    /**
     * Helper: Convert RoomBooking to RentalStatusDTO
     */
    private RentalStatusDTO toRentalStatusDTO(RoomBooking booking) {
        if (booking == null) {
            return null;
        }

        return RentalStatusDTO.builder()
                .bookingId(booking.getId())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .durationMonths(booking.getDurationMonths())
                .status(booking.getStatus())
                .monthlyRent(booking.getMonthlyRent())
                .depositAmount(booking.getDepositAmount())
                .paymentMethod(booking.getPaymentMethod())
                .note(booking.getNote())
                .build();
    }

    /**
     * Helper: Convert MonthlyPayment to PaymentDTO
     */
    private PaymentDTO toPaymentDTO(MonthlyPayment payment) {
        if (payment == null) {
            return null;
        }

        Double electricityAmount = 0.0;
        Double waterAmount = 0.0;

        if (payment.getRoomBooking() != null && payment.getRoomBooking().getRoom() != null) {
            Room room = payment.getRoomBooking().getRoom();

            if (payment.getElectricityUsed() != null && room.getElectricityCost() != null) {
                electricityAmount = payment.getElectricityUsed() * room.getElectricityCost();
            }

            if (payment.getWaterUsed() != null && room.getWaterCost() != null) {
                waterAmount = payment.getWaterUsed() * room.getWaterCost();
            }
        }

        return PaymentDTO.builder()
                .id(payment.getId())
                .month(payment.getMonth())
                .year(payment.getYear())
                .rentAmount(payment.getRoomBooking() != null ? payment.getRoomBooking().getMonthlyRent() : 0.0)
                .electricityUsage(payment.getElectricityUsed())
                .electricityCost(payment.getRoomBooking() != null && payment.getRoomBooking().getRoom() != null
                        ? payment.getRoomBooking().getRoom().getElectricityCost()
                        : null)
                .electricityAmount(electricityAmount)
                .waterUsage(payment.getWaterUsed())
                .waterCost(payment.getRoomBooking() != null && payment.getRoomBooking().getRoom() != null
                        ? payment.getRoomBooking().getRoom().getWaterCost()
                        : null)
                .waterAmount(waterAmount)
                .additionalFees(payment.getAdditionalFees())
                .totalAmount(payment.getAmount())
                .paidAmount(payment.getPaidAmount())
                .status(payment.getStatus())
                .dueDate(payment.getDueDate())
                .paidDate(payment.getPaidDate())
                .note(payment.getNote())
                .roomId(payment.getRoomBooking() != null && payment.getRoomBooking().getRoom() != null
                        ? payment.getRoomBooking().getRoom().getId()
                        : null)
                .roomName(payment.getRoomBooking() != null && payment.getRoomBooking().getRoom() != null
                        ? payment.getRoomBooking().getRoom().getRoomName()
                        : null)
                .bookingId(payment.getRoomBooking() != null ? payment.getRoomBooking().getId() : null)
                .build();
    }

    /**
     * Helper: Get landlord contact info
     */
    private LandlordContactDTO getLandlordContact(Integer ownerId) {
        UserDtls owner = userRepository.findById(ownerId).orElse(null);
        if (owner == null) {
            return null;
        }

        return LandlordContactDTO.builder()
                .ownerId(owner.getId())
                .name(owner.getName())
                .email(owner.getEmail())
                .mobileNumber(owner.getMobileNumber())
                .profileImage(owner.getProfileImage())
                .bankId(owner.getBankId())
                .accountNo(owner.getAccountNo())
                .build();
    }
}
