package br.com.devjf.salessync.service;

import br.com.devjf.salessync.dao.SaleItemDAO;
import br.com.devjf.salessync.model.Sale;
import br.com.devjf.salessync.model.SaleItem;

import java.util.List;

public class SaleItemService {

    private final SaleItemDAO saleItemDAO;

    public SaleItemService() {
        this.saleItemDAO = new SaleItemDAO();
    }

    /**
     * Saves the items of a sale
     */
    public boolean saveSaleItems(Sale sale) {
        boolean success = true;
        for (SaleItem item : sale.getItems()) {
            item.setSale(sale);
            success = success && saleItemDAO.save(item);
        }
        return success;
    }

    /**
     * Applies discount factor to each sale item unit price
     */
    public void applyDiscountsToItems(List<SaleItem> items, double discountFactor) {
        for (SaleItem item : items) {
            double discountedPrice = item.getUnitPrice() * discountFactor;
            item.setUnitPrice(discountedPrice);
        }
    }
}