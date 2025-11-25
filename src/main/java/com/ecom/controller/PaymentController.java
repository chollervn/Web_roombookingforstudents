package com.ecom.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ecom.model.MonthlyPayment;
import com.ecom.model.UserDtls;
import com.ecom.service.MonthlyPaymentService;
import com.ecom.util.CommonUtil;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/payment")
public class PaymentController {

    @Autowired
    private MonthlyPaymentService monthlyPaymentService;

    @Autowired
    private CommonUtil commonUtil;

    @GetMapping("/{id}/details")
    public String viewPaymentDetails(@PathVariable Integer id, Model m, Principal p, HttpSession session) {
        UserDtls loggedInUser = commonUtil.getLoggedInUserDetails(p);

        MonthlyPayment payment = monthlyPaymentService.getPaymentById(id);

        if (payment == null) {
            session.setAttribute("errorMsg", "Không tìm thấy hóa đơn!");
            return "redirect:/admin/payments";
        }

        // Check permission (owner of the room)
        if (payment.getRoomBooking() != null && payment.getRoomBooking().getRoom() != null
                && !payment.getRoomBooking().getRoom().getOwnerId().equals(loggedInUser.getId())) {
            session.setAttribute("errorMsg", "Bạn không có quyền xem hóa đơn này!");
            return "redirect:/admin/payments";
        }

        m.addAttribute("payment", payment);
        return "/admin/payment_details";
    }
}
