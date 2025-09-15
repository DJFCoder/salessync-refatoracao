package br.com.devjf.salessync.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import br.com.devjf.salessync.dao.ExpenseDAO;
import br.com.devjf.salessync.dao.SaleDAO;
import br.com.devjf.salessync.model.Expense;
import br.com.devjf.salessync.model.Sale;
import br.com.devjf.salessync.util.CSVExporter;

public class ReportService {

    private static final String START_DATE = "startDate";
    private static final String END_DATE = "endDate";
    private static final String TOTAL_REVENUE = "totalRevenue";
    private final SaleDAO saleDAO;
    private final ExpenseDAO expenseDAO;

    public ReportService() {
        this.saleDAO = new SaleDAO();
        this.expenseDAO = new ExpenseDAO();
    }

    public Map<String, Object> generateBalanceSheet(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();

        // Extract parameters
        LocalDate startDate = (LocalDate) parameters.getOrDefault(START_DATE, LocalDate.now().withDayOfMonth(1));
        LocalDate endDate = (LocalDate) parameters.getOrDefault(END_DATE, LocalDate.now());

        // Convert LocalDate to LocalDateTime for sales query
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        // Get sales data
        List<Sale> sales = saleDAO.findByDateRange(startDateTime, endDateTime);
        double totalRevenue = sales.stream().mapToDouble(Sale::getTotalAmount).sum();

        // Get expenses data
        List<Expense> expenses = expenseDAO.findByDateRange(startDate, endDate);
        double totalExpenses = expenses.stream().mapToDouble(Expense::getAmount).sum();

        // Calculate profit/loss
        double netProfit = totalRevenue - totalExpenses;

        // Populate result
        result.put(START_DATE, startDate);
        result.put(END_DATE, endDate);
        result.put(TOTAL_REVENUE, totalRevenue);
        result.put("totalExpenses", totalExpenses);
        result.put("netProfit", netProfit);
        result.put("sales", sales);
        result.put("expenses", expenses);

        return result;
    }

    public Map<String, Double> calculateProfitability(String period) {
        Map<String, Double> result = new HashMap<>();

        LocalDate today = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate = today;

        // Determine date range based on period
        switch (period.toLowerCase()) {
            case "daily":
                startDate = today;
                break;
            case "weekly":
                startDate = today.minusWeeks(1);
                break;
            case "monthly":
                startDate = today.withDayOfMonth(1);
                break;
            case "quarterly":
                startDate = today.minusMonths(3).withDayOfMonth(1);
                break;
            case "yearly":
                startDate = today.withDayOfYear(1);
                break;
            default:
                startDate = today.minusMonths(1);
        }

        // Convert LocalDate to LocalDateTime for sales query
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        // Get sales data
        List<Sale> sales = saleDAO.findByDateRange(startDateTime, endDateTime);
        double totalRevenue = sales.stream().mapToDouble(Sale::getTotalAmount).sum();

        // Get expenses data
        List<Expense> expenses = expenseDAO.findByDateRange(startDate, endDate);
        double totalExpenses = expenses.stream().mapToDouble(Expense::getAmount).sum();

        // Calculate metrics
        double netProfit = totalRevenue - totalExpenses;
        double profitMargin = totalRevenue > 0 ? (netProfit / totalRevenue) * 100 : 0;
        double returnOnInvestment = totalExpenses > 0 ? (netProfit / totalExpenses) * 100 : 0;

        // Populate result
        result.put(TOTAL_REVENUE, totalRevenue);
        result.put("totalExpenses", totalExpenses);
        result.put("netProfit", netProfit);
        result.put("profitMargin", profitMargin);
        result.put("roi", returnOnInvestment);

        return result;
    }

    public Map<String, Object> analyzeSalesByPeriod(LocalDate start, LocalDate end) {
        Map<String, Object> result = new HashMap<>();

        // Convert LocalDate to LocalDateTime for sales query
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.plusDays(1).atStartOfDay().minusNanos(1);

        // Get sales data
        List<Sale> sales = saleDAO.findByDateRange(startDateTime, endDateTime);

        // Calculate metrics
        double totalRevenue = sales.stream().mapToDouble(Sale::getTotalAmount).sum();
        int totalSales = sales.size();
        double averageSaleValue = totalSales > 0 ? totalRevenue / totalSales : 0;

        // Group sales by day
        Map<LocalDate, Double> dailySales = new HashMap<>();
        for (Sale sale : sales) {
            LocalDate saleDate = sale.getDate().toLocalDate();
            dailySales.put(saleDate, dailySales.getOrDefault(saleDate, 0.0) + sale.getTotalAmount());
        }

        // Populate result
        result.put(START_DATE, start);
        result.put(END_DATE, end);
        result.put(TOTAL_REVENUE, totalRevenue);
        result.put("totalSales", totalSales);
        result.put("averageSaleValue", averageSaleValue);
        result.put("dailySales", dailySales);

        return result;
    }

    public boolean exportReport(Map<String, Object> data, String format, String path) {
        // Currently only supporting CSV export
        if (!"csv".equalsIgnoreCase(format)) {
            return false;
        }

        // Export data to CSV file
        try {
            CSVExporter exporter = new CSVExporter();
            return exporter.exportToCSV(data, path);
        } catch (Exception e) {
            Logger.getLogger("Error exporting report: " + e.getMessage());
            return false;
        }
    }
}