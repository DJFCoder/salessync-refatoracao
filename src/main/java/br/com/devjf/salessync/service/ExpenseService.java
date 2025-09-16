package br.com.devjf.salessync.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import br.com.devjf.salessync.dao.ExpenseCategoryDAO;
import br.com.devjf.salessync.dao.ExpenseDAO;
import br.com.devjf.salessync.model.Expense;
import br.com.devjf.salessync.model.ExpenseCategory;
import br.com.devjf.salessync.model.RecurrenceType;

public class ExpenseService {

    private ExpenseDAO expenseDAO;
    private ExpenseCategoryDAO categoryDAO;

    public ExpenseService() {
        this.expenseDAO = new ExpenseDAO();
        this.categoryDAO = new ExpenseCategoryDAO();
    }

    public ExpenseService(ExpenseDAO expenseDAO, ExpenseCategoryDAO categoryDAO) {
        this.expenseDAO = expenseDAO;
        this.categoryDAO = categoryDAO;
    }

    public boolean registerExpense(Expense expense) {
        if (!validateExpense(expense)) {
            return false;
        }
        return expenseDAO.save(expense);
    }

    public boolean updateExpense(Expense expense) {
        Expense existingExpense = expenseDAO.findById(expense.getId());
        if (existingExpense == null) {
            return false;
        }

        if (!validateExpense(expense)) {
            return false;
        }

        return expenseDAO.update(expense);
    }

    public boolean deleteExpense(Integer id) {
        return expenseDAO.delete(id);
    }

    public Expense findExpenseById(Integer id) {
        return expenseDAO.findById(id);
    }

    public List<Expense> listExpenses(Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return expenseDAO.findAll();
        }

        // Handle different filter types
        if (filters.containsKey("categoryId")) {
            Integer categoryId = (Integer) filters.get("categoryId");
            ExpenseCategory category = categoryDAO.findById(categoryId);
            return expenseDAO.findByCategory(category);
        } else if (filters.containsKey("startDate") && filters.containsKey("endDate")) {
            LocalDate startDate = (LocalDate) filters.get("startDate");
            LocalDate endDate = (LocalDate) filters.get("endDate");
            return expenseDAO.findByDateRange(startDate, endDate);
        }

        return expenseDAO.findAll();
    }

    public void manageRecurrences(Expense expense) {
        // Skip if it's a daily expense
        if (expense.getRecurrence() == RecurrenceType.WEEKLY) {
            return;
        }

        LocalDate nextDate = calculateNextRecurrenceDate(expense.getDate(), expense.getRecurrence());

        // Create a new expense for the next recurrence
        Expense nextExpense = new Expense();
        nextExpense.setDescription(expense.getDescription());
        nextExpense.setCategory(expense.getCategory());
        nextExpense.setDate(nextDate);
        nextExpense.setAmount(expense.getAmount());
        nextExpense.setRecurrence(expense.getRecurrence());
        nextExpense.setNotes(expense.getNotes());

        expenseDAO.save(nextExpense);
    }

    private LocalDate calculateNextRecurrenceDate(LocalDate currentDate, RecurrenceType recurrenceType) {
        switch (recurrenceType) {
            case DAILY:
                return currentDate.plusDays(1);
            case WEEKLY:
                return currentDate.plusWeeks(1);
            case MONTHLY:
                return currentDate.plusMonths(1);
            case ANNUAL:
                return currentDate.plusYears(1);
            default:
                return currentDate;
        }
    }

    public void categorizeExpense(Expense expense) {
        // This method would contain logic to automatically categorize expenses
        // based on description or other attributes
        // For simplicity, just using the provided category
        if (expense.getCategory() == null) {
            // Set a default category if none is provided
            ExpenseCategory defaultCategory = categoryDAO.findByName("Miscellaneous");
            if (defaultCategory == null) {
                defaultCategory = new ExpenseCategory();
                defaultCategory.setName("Miscellaneous");
                defaultCategory.setDescription("Default category for uncategorized expenses");
                categoryDAO.save(defaultCategory);
            }
            expense.setCategory(defaultCategory);
        }
    }

    private boolean validateExpense(Expense expense) {
        return expense != null &&
                expense.getDescription() != null && !expense.getDescription().trim().isEmpty() &&
                expense.getAmount() != null && expense.getAmount() > 0 &&
                expense.getDate() != null &&
                expense.getCategory() != null && expense.getCategory().getId() != null;
    }
}