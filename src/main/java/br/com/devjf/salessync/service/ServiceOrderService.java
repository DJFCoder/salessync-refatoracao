package br.com.devjf.salessync.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import br.com.devjf.salessync.dao.ServiceOrderDAO;
import br.com.devjf.salessync.model.Customer;
import br.com.devjf.salessync.model.ServiceOrder;
import br.com.devjf.salessync.model.ServiceStatus;

public class ServiceOrderService {

    private final ServiceOrderDAO serviceOrderDAO;

    public ServiceOrderService() {
        this.serviceOrderDAO = new ServiceOrderDAO();
    }

    public boolean createServiceOrder(ServiceOrder order) {
        // Set default status if not specified
        if (order.getStatus() == null) {
            order.setStatus(ServiceStatus.PENDING);
        }

        return serviceOrderDAO.save(order);
    }

    public boolean updateStatus(Integer id, ServiceStatus status) {
        ServiceOrder order = serviceOrderDAO.findById(id);
        if (order == null) {
            return false;
        }

        order.setStatus(status);
        return serviceOrderDAO.update(order);
    }

    public List<ServiceOrder> listServiceOrders(Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return serviceOrderDAO.findAll();
        }

        // Handle different filter types
        if (filters.containsKey("customerId")) {
            Customer customer = (Customer) filters.get("customer");
            return serviceOrderDAO.findByCustomer(customer);
        } else if (filters.containsKey("status")) {
            ServiceStatus status = (ServiceStatus) filters.get("status");
            return serviceOrderDAO.findByStatus(status);
        } else if (filters.containsKey("startDate") && filters.containsKey("endDate")) {
            LocalDate startDate = (LocalDate) filters.get("startDate");
            LocalDate endDate = (LocalDate) filters.get("endDate");
            return serviceOrderDAO.findByDateRange(startDate, endDate);
        }

        return serviceOrderDAO.findAll();
    }
}