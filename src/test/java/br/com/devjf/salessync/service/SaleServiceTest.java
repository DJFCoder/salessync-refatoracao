package br.com.devjf.salessync.service;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.com.devjf.salessync.dao.SaleDAO;
import br.com.devjf.salessync.model.Customer;
import br.com.devjf.salessync.model.Sale;
import br.com.devjf.salessync.model.SaleItem;

import java.time.LocalDateTime;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SaleServiceTest {

  @Mock
  private SaleDAO saleDAO;

  @Mock
  private SaleItemService saleItemService;

  @InjectMocks
  private SaleService saleService;

  private Sale validSale;

  private Customer validCustomer;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    saleService = new SaleService();

    validCustomer = new Customer();
    validCustomer.setId(1);
    validCustomer.setName("Cliente Teste");

    validSale = new Sale();
    validSale.setId(1);
    validSale.setCustomer(validCustomer);
    validSale.setCanceled(false);

    SaleItem item = new SaleItem();
    item.setDescription("Produto de Teste");
    item.setQuantity(1);
    item.setUnitPrice(100.00);
    validSale.addItem(item);
  }

  /**
   * CT004 - Validação do registro de venda com cliente existente
   * Testa se o sistema impede o cancelamento de venda para cliente inexistente.
   */
  @Test
  public void testCancelSale_Success() {
    when(saleDAO.findById(validSale.getId())).thenReturn(validSale);
    when(saleDAO.update(any(Sale.class))).thenReturn(true);

    boolean result = saleService.cancelSale(validSale.getId());

    assertTrue(result);
    verify(saleDAO).update(validSale);
    assertTrue(validSale.isCanceled());
  }

  /**
   * CT005 - Validação dos campos obrigatórios no registro de venda
   * Testa se o sistema valida os campos obrigatórios da venda.
   */
  @Test
  public void testRegisterSale_InvalidSale() {
    validSale.setItems(Collections.emptyList());

    boolean result = saleService.registerSale(validSale);

    assertFalse(result);
    verify(saleDAO, never()).save(any(Sale.class));
  }

  /**
   * CT006 - Cadastro de venda com dados válidos
   * Testa o cadastro bem-sucedido de uma venda com dados válidos.
   */
  @Test
  public void testRegisterSale_Success() {
    when(saleDAO.save(any(Sale.class))).thenReturn(true);

    validSale.setDate(LocalDateTime.now());
    validSale
        .setTotalAmount(
            validSale.getItems().stream().mapToDouble(item -> item.getUnitPrice() * item.getQuantity()).sum());

    boolean result = saleService.registerSale(validSale);

    assertTrue(result);
    verify(saleDAO, times(1)).save(any(Sale.class));
  }
}
