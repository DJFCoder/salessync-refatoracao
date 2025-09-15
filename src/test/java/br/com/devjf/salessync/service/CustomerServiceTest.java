package br.com.devjf.salessync.service;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.com.devjf.salessync.model.Customer;
import br.com.devjf.salessync.dao.CustomerDAO;
import br.com.devjf.salessync.dao.SaleDAO;
import br.com.devjf.salessync.dao.ServiceOrderDAO;

public class CustomerServiceTest {

  @Mock
  private CustomerDAO customerDAO;

  @Mock
  private SaleDAO saleDAO;

  @Mock
  private ServiceOrderDAO serviceOrderDAO;

  @InjectMocks
  private CustomerService customerService;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * CT001 - Validação do cadastro de cliente com CPF/CNPJ único
   * Testa se o sistema impede cadastro com CPF/CNPJ duplicado.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testCreateCustomer_DuplicateTaxId() {
    Customer customer = new Customer();
    customer.setTaxId("12345678901");
    customer.setName("Test Customer");

    when(customerDAO.findByTaxId(customer.getTaxId())).thenReturn(new Customer());

    customerService.createCustomer(customer);
  }

  /**
   * CT002 - Validação dos campos obrigatórios no cadastro de cliente
   * Testa se o sistema impede cadastro de cliente com campos obrigatórios vazios.
   */
  @Test
  public void testCreateCustomer_RequiredFieldsValidation() {
    Customer invalidCustomer = new Customer();
    invalidCustomer.setName("");
    invalidCustomer.setTaxId("");
    invalidCustomer.setRegistrationDate(null);

    boolean result = customerService.createCustomer(invalidCustomer);

    assertFalse(result);
    verify(customerDAO, never()).save(any(Customer.class));
  }

  /**
   * CT003 - Cadastro de cliente com dados válidos
   * Testa o cadastro bem-sucedido de um cliente com CPF/CNPJ único.
   */
  @Test
  public void testCreateCustomer_Success() {
    Customer customer = new Customer();
    customer.setTaxId("12345678901");
    customer.setName("Test Customer");

    when(customerDAO.findByTaxId(customer.getTaxId())).thenReturn(null);
    when(customerDAO.save(any(Customer.class))).thenReturn(true);

    boolean result = customerService.createCustomer(customer);

    assertTrue(result);
    verify(customerDAO).save(customer);
  }

  /**
   * CT003 - Cadastro de cliente com dados válidos
   * Simula falha no banco de dados e espera uma RuntimeException.
   */
  @Test
  public void testCreateCustomer_DatabaseFailure() {
    Customer customer = new Customer();
    customer.setTaxId("12345678901");
    customer.setName("Test Customer");

    when(customerDAO.findByTaxId(customer.getTaxId())).thenReturn(null);
    when(customerDAO.save(any(Customer.class))).thenThrow(new RuntimeException("Database error"));

    try {
      customerService.createCustomer(customer);
      fail("Expected RuntimeException");
    } catch (RuntimeException e) {
      assertEquals("Database error", e.getMessage());
    }
  }
}