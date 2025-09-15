// SaleService.java
package br.com.devjf.salessync.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import br.com.devjf.salessync.dao.SaleDAO;
import br.com.devjf.salessync.model.PaymentMethod;
import br.com.devjf.salessync.model.Sale;
import br.com.devjf.salessync.model.User;

public class SaleService {

    private final SaleDAO saleDAO;
    private final SaleItemService saleItemService;

    public SaleService() {
        this.saleDAO = new SaleDAO();
        this.saleItemService = new SaleItemService();
    }

    public boolean registerSale(Sale sale) {
        // Calculate total amount before saving
        sale.calculateTotal();
        boolean isValid = validateSale(sale);
        if (!isValid) {
            return false;
        }
        boolean success = saleDAO.save(sale);

        if (success && sale.getUser() != null) {
            logSaleOperation(sale, "REGISTER", sale.getUser());
        }

        return success;
    }

    public boolean updateSale(Sale sale) {
        Sale existingSale = saleDAO.findById(sale.getId());
        if (existingSale == null) {
            return false;
        }

        // Calculate total amount before updating
        sale.calculateTotal();
        boolean isValid = validateSale(sale);
        if (!isValid) {
            return false;
        }
        boolean success = saleDAO.update(sale);

        if (success && sale.getUser() != null) {
            logSaleOperation(sale, "UPDATE", sale.getUser());
        }

        return success;
    }

    public boolean cancelSale(Integer id) {
        Sale sale = saleDAO.findById(id);
        if (sale == null) {
            return false;
        }

        sale.setCanceled(true);
        boolean success = saleDAO.update(sale);

        if (success && sale.getUser() != null) {
            logSaleOperation(sale, "CANCEL", sale.getUser());
        }

        return success;
    }

    public Sale findSaleById(Integer id) {
        return saleDAO.findById(id);
    }

    public List<Sale> listSales(java.util.Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return saleDAO.findAll();
        }

        if (filters.containsKey("customerId")) {
            Integer customerId = (Integer) filters.get("customerId");
            return saleDAO.findByCustomerId(customerId);
        } else if (filters.containsKey("startDate") && filters.containsKey("endDate")) {
            LocalDateTime startDate = (LocalDateTime) filters.get("startDate");
            LocalDateTime endDate = (LocalDateTime) filters.get("endDate");
            return saleDAO.findByDateRange(startDate, endDate);
        }

        return saleDAO.findAll();
    }

    public Sale applyDiscounts(Sale sale, double discountPercentage) {
        if (discountPercentage <= 0 || discountPercentage > 100) {
            return sale;
        }

        double discountFactor = 1 - (discountPercentage / 100);

        // Delegate discount application to SaleItemService
        saleItemService.applyDiscountsToItems(sale.getItems(), discountFactor);

        // Recalculate total
        sale.calculateTotal();
        boolean isValid = validateSale(sale);
        if (!isValid) {
            return null;
        }

        return sale;
    }

    /**
     * Salva os itens de uma venda delegando para SaleItemService
     */
    public boolean saveSaleItems(Sale sale) {
        boolean isValid = validateSale(sale);
        if (!isValid) {
            return false;
        }
        return saleItemService.saveSaleItems(sale);
    }

    /**
     * Busca vendas por período
     */
    public List<Sale> getSalesByPeriod(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        return saleDAO.findByDateRange(startDateTime, endDateTime);
    }

    /**
     * Processa o pagamento da venda
     */
    public boolean processSalePayment(Sale sale, PaymentMethod paymentMethod) {
        if (sale == null || paymentMethod == null) {
            return false;
        }

        sale.setPaymentMethod(paymentMethod);
        sale.setPaymentDate(LocalDateTime.now());
        boolean isValid = validateSale(sale);
        if (!isValid) {
            return false;
        }
        boolean success = saleDAO.update(sale);

        if (success && sale.getUser() != null) {
            logSaleOperation(sale, "PAYMENT", sale.getUser());
        }

        return success;
    }

    /**
     * Valida os dados da venda antes de salvar
     */
    public boolean validateSale(Sale sale) {
        return sale != null
                && sale.getCustomer() != null
                && sale.getItems() != null
                && !sale.getItems().isEmpty()
                && sale.getItems().stream().allMatch(item -> item.getQuantity() > 0 && item.getUnitPrice() > 0);
    }

    /**
     * Registra log da venda
     */
    private void logSaleOperation(Sale sale, String operation, User user) {
        String details = "Sale " + operation + " - Total: " + sale.getTotalAmount();

        LogService logService = new LogService();
        logService.recordLog(user, operation, details);
    }
}