package br.com.devjf.salessync.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import br.com.devjf.salessync.dao.SaleDAO;
import br.com.devjf.salessync.model.Customer;
import br.com.devjf.salessync.model.Sale;
import br.com.devjf.salessync.model.SaleItem;
import java.util.Arrays;
import static org.mockito.ArgumentMatchers.any;

@RunWith(MockitoJUnitRunner.class)
public class SaleServiceTest {
    private SaleService saleService;
    @Mock
    private SaleDAO saleDAO;
    @Mock
    private SaleItemService saleItemService;
    private Sale validSale;
    private Customer validCustomer;

    @Before
    public void setUp() {
        saleService = new SaleService(saleDAO, saleItemService);

        validCustomer = new Customer();
        validCustomer.setId(1);
        validCustomer.setName("John Doe");

        validSale = new Sale();
        validSale.setId(1);
        validSale.setCustomer(validCustomer);

        // Usando o construtor correto de SaleItem(description, quantity, unitPrice)
        SaleItem item1 = new SaleItem("Item 1", 2, 10.00);
        item1.setId(1);
        item1.setSale(validSale); // Definindo a referência de volta para a venda

        SaleItem item2 = new SaleItem("Item 2", 3, 5.00);
        item2.setId(2);
        item2.setSale(validSale); // Definindo a referência de volta para a venda

        validSale.setItems(Arrays.asList(item1, item2));
    }

    /**
     * CT004 - Validação do registro de venda com cliente existente Testa se o
     * sistema impede o cancelamento de venda para cliente inexistente.
     */
    @Test
    public void testCancelSale_Success() {
        when(saleDAO.findById(1)).thenReturn(validSale);
        when(saleDAO.update(validSale)).thenReturn(true);
        boolean result = saleService.cancelSale(1);
        assertTrue(result);
        assertTrue(validSale.isCanceled());
        verify(saleDAO).findById(1);
        verify(saleDAO).update(validSale);
    }

    /**
     * CT005 - Validação dos campos obrigatórios no registro de venda Testa se o
     * sistema valida os campos obrigatórios da venda.
     */
    @Test
    public void testRegisterSale_Fail_NullCustomer() {
        validSale.setCustomer(null);
        boolean result = saleService.registerSale(validSale);
        assertFalse(result);
    }

    @Test
    public void testRegisterSale_Fail_NullItems() {
        validSale.setItems(null);
        boolean result = saleService.registerSale(validSale);
        assertFalse(result);
    }

    @Test
    public void testRegisterSale_Fail_EmptyItems() {
        validSale.setItems(new ArrayList<>());
        boolean result = saleService.registerSale(validSale);
        assertFalse(result);
    }

    /**
     * CT006 - Cadastro de venda com dados válidos Testa o cadastro bem-sucedido
     * de uma venda com dados válidos.
     */
    @Test
    public void testRegisterSale_Success() {
        // Arrange: Configura o mock para aceitar qualquer objeto Sale,
        // pois o estado do objeto muda dentro do serviço.
        when(saleDAO.save(any(Sale.class))).thenReturn(true);
        // Act: Executa o método a ser testado.
        boolean result = saleService.registerSale(validSale);
        // Assert: Verifica o resultado e as interações.
        assertTrue(result);
        verify(saleDAO).save(validSale);
    }
}
