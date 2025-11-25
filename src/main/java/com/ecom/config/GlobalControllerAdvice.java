package com.ecom.config;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.ecom.model.Deposit;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.DepositService;
import com.ecom.service.PaymentNotificationService;
import com.ecom.service.UserService;

import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private DepositService depositService;

    @Autowired
    private PaymentNotificationService paymentNotificationService;

    @ModelAttribute
    public void getUserDetails(Principal p, Model m, HttpSession session) {
        if (p != null) {
            String email = p.getName();
            UserDtls userDtls = userService.getUserByEmail(email);
            m.addAttribute("user", userDtls);

            // Cart count
            Integer countCart = cartService.getCountCart(userDtls.getId());
            m.addAttribute("countCart", countCart);

            if ("ROLE_USER".equals(userDtls.getRole())) {
                // 1. Deposit Notifications
                Boolean hasViewed = (Boolean) session.getAttribute("depositNotificationsViewed_" + userDtls.getId());
                if (hasViewed == null || !hasViewed) {
                    List<Deposit> userDeposits = depositService.getDepositsByUser(userDtls.getId());
                    long unreadDepositCount = userDeposits.stream()
                            .filter(d -> "APPROVED".equals(d.getStatus()) || "REJECTED".equals(d.getStatus()))
                            .filter(d -> d.getApprovedDate() != null
                                    && d.getApprovedDate().equals(java.time.LocalDate.now()))
                            .count();
                    m.addAttribute("unreadDepositNotifications", unreadDepositCount);
                } else {
                    m.addAttribute("unreadDepositNotifications", 0);
                }

                // 2. Payment Notifications (Unread Count)
                Long unreadNotifications = paymentNotificationService.countUnreadNotifications(userDtls.getId());
                m.addAttribute("unreadNotifications", unreadNotifications);

                // 3. Recent Notifications (Top 3) for Dropdown
                List<com.ecom.model.PaymentNotification> recentNotifications = paymentNotificationService
                        .getRecentNotificationsByUserId(userDtls.getId());
                m.addAttribute("recentNotifications", recentNotifications);

                // DEBUG LOG
                System.out.println(
                        "GlobalControllerAdvice: User " + userDtls.getEmail() + " (ID: " + userDtls.getId() + ")");
                System.out.println("Found " + (recentNotifications != null ? recentNotifications.size() : "null")
                        + " recent notifications.");
            }

            // Admin/Owner Deposit Notifications
            if ("ROLE_ADMIN".equals(userDtls.getRole()) || "ROLE_OWNER".equals(userDtls.getRole())) {
                List<Deposit> allDeposits;
                if ("ROLE_ADMIN".equals(userDtls.getRole())) {
                    allDeposits = depositService.getAllDeposits();
                } else {
                    allDeposits = depositService.getDepositsByOwner(userDtls.getId());
                }

                long pendingDepositCount = allDeposits.stream()
                        .filter(d -> "PENDING".equals(d.getStatus()))
                        .count();
                m.addAttribute("pendingDepositCount", pendingDepositCount);
            }
        }
    }
}
