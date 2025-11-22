package com.ecom.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.dto.ExpenseDTO;
import com.ecom.dto.FinancialReportDTO;
import com.ecom.facade.FinancialFacade;
import com.ecom.model.Expense;
import com.ecom.model.UserDtls;
import com.ecom.service.ExpenseService;
import com.ecom.util.CommonUtil;

import jakarta.servlet.http.HttpSession;

/**
 * Controller for Financial Management (Income and Expense tracking)
 * Follows Single Responsibility - only handles financial operations
 * Delegates business logic to FinancialFacade (Facade Pattern)
 */
@Controller
@RequestMapping("/admin/financial")
public class FinancialController {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private FinancialFacade financialFacade;

    /**
     * Financial dashboard - overview of income and expenses
     */
    @GetMapping("/")
    public String financialDashboard(Model m, Principal p) {
        UserDtls loggedInUser = commonUtil.getLoggedInUserDetails(p);

        // Get current month summary using Facade
        FinancialReportDTO currentMonth = financialFacade.getCurrentMonthSummary(loggedInUser.getId());

        // Get year-to-date summary
        FinancialReportDTO yearToDate = financialFacade.getYearToDateSummary(loggedInUser.getId());

        m.addAttribute("currentMonth", currentMonth);
        m.addAttribute("yearToDate", yearToDate);

        return "/admin/financial/dashboard";
    }

    /**
     * Income overview page
     */
    @GetMapping("/income")
    public String incomePage(Model m, Principal p,
            @RequestParam(required = false) String period) {
        UserDtls loggedInUser = commonUtil.getLoggedInUserDetails(p);

        FinancialReportDTO report;
        if ("ytd".equals(period)) {
            report = financialFacade.getYearToDateSummary(loggedInUser.getId());
        } else {
            report = financialFacade.getCurrentMonthSummary(loggedInUser.getId());
        }

        m.addAttribute("report", report);
        m.addAttribute("period", period != null ? period : "current");

        return "/admin/financial/income";
    }

    /**
     * Expense management page
     */
    @GetMapping("/expenses")
    public String expensesPage(Model m, Principal p,
            @RequestParam(required = false) String type) {
        UserDtls loggedInUser = commonUtil.getLoggedInUserDetails(p);

        List<Expense> expenses;
        if (type != null && !type.isEmpty()) {
            expenses = expenseService.getExpensesByOwnerIdAndType(loggedInUser.getId(), type);
        } else {
            expenses = expenseService.getExpensesByOwnerId(loggedInUser.getId());
        }

        // Convert to DTOs
        List<ExpenseDTO> expenseDTOs = expenses.stream()
                .map(expenseService::toDTO)
                .toList();

        // Get totals
        Double totalExpenses = expenseService.getTotalExpensesByOwnerId(loggedInUser.getId());

        m.addAttribute("expenses", expenseDTOs);
        m.addAttribute("totalExpenses", totalExpenses);
        m.addAttribute("filterType", type);

        return "/admin/financial/expenses";
    }

    /**
     * Add expense form
     */
    @GetMapping("/add-expense")
    public String addExpenseForm(Model m) {
        m.addAttribute("expense", new ExpenseDTO());
        return "/admin/financial/add_expense";
    }

    /**
     * Save new expense
     */
    @PostMapping("/save-expense")
    public String saveExpense(@ModelAttribute ExpenseDTO expenseDTO,
            @RequestParam(value = "receiptFile", required = false) MultipartFile receiptFile,
            Principal p,
            HttpSession session) {
        try {
            UserDtls loggedInUser = commonUtil.getLoggedInUserDetails(p);

            // Convert DTO to entity
            Expense expense = expenseService.toEntity(expenseDTO);
            expense.setOwnerId(loggedInUser.getId());

            // Handle receipt file upload if provided
            if (receiptFile != null && !receiptFile.isEmpty()) {
                // TODO: Implement file upload logic (similar to room image upload)
                // For now, just set the filename
                expense.setReceiptImage(receiptFile.getOriginalFilename());
            }

            expenseService.saveExpense(expense);
            session.setAttribute("succMsg", "Expense added successfully!");

        } catch (Exception e) {
            session.setAttribute("errorMsg", "Failed to add expense: " + e.getMessage());
        }

        return "redirect:/admin/financial/expenses";
    }

    /**
     * Delete expense
     */
    @GetMapping("/delete-expense")
    public String deleteExpense(@RequestParam Integer id, HttpSession session) {
        Boolean deleted = expenseService.deleteExpense(id);
        if (deleted) {
            session.setAttribute("succMsg", "Expense deleted successfully!");
        } else {
            session.setAttribute("errorMsg", "Failed to delete expense");
        }
        return "redirect:/admin/financial/expenses";
    }

    /**
     * Financial reports page with custom date range
     */
    @GetMapping("/reports")
    public String reportsPage(Model m, Principal p,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        UserDtls loggedInUser = commonUtil.getLoggedInUserDetails(p);

        LocalDate start;
        LocalDate end;

        if (startDate != null && endDate != null) {
            start = LocalDate.parse(startDate);
            end = LocalDate.parse(endDate);
        } else {
            // Default to current month
            LocalDate now = LocalDate.now();
            start = now.withDayOfMonth(1);
            end = now.withDayOfMonth(now.lengthOfMonth());
        }

        FinancialReportDTO report = financialFacade.generateFinancialReport(
                loggedInUser.getId(), start, end);

        m.addAttribute("report", report);
        m.addAttribute("startDate", start.toString());
        m.addAttribute("endDate", end.toString());

        return "/admin/financial/reports";
    }
}
