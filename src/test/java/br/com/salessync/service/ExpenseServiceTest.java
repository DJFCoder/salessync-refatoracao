package br.com.salessync.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.com.devjf.salessync.dao.ExpenseDAO;
import br.com.devjf.salessync.dao.ExpenseCategoryDAO;
import br.com.devjf.salessync.model.Expense;
import br.com.devjf.salessync.model.ExpenseCategory;
import br.com.devjf.salessync.service.ExpenseService;

public class ExpenseServiceTest {

  @Mock
  private ExpenseDAO expenseDAO;

  @Mock
  private ExpenseCategoryDAO categoryDAO;

  @InjectMocks
  private ExpenseService expenseService;

  private Expense validExpense;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    validExpense = new Expense();
    validExpense.setId(1);
    validExpense.setDescription("Despesa de teste");
    validExpense.setAmount(100.0);
    validExpense.setDate(LocalDate.now());
    validExpense.setCategory(new ExpenseCategory());
  }

  /**
   * CT007 - Validação dos campos obrigatórios no registro de despesa
   * Testa se o sistema impede cadastro de despesa com campos obrigatórios vazios.
   */
  @Test
  public void testRegisterExpense_RequiredFieldsValidation() {
    Expense invalidExpense = new Expense();
    invalidExpense.setDescription("");
    invalidExpense.setAmount(0.0);
    invalidExpense.setDate(null);
    invalidExpense.setCategory(null);

    boolean result = expenseService.registerExpense(invalidExpense);

    assertFalse(result);
    verify(expenseDAO, never()).save(any(Expense.class));
  }

  /**
   * CT008 - Cadastro de despesa com dados válidos
   * Testa se o sistema permite o cadastro de despesa com todos os campos
   * obrigatórios preenchidos.
   */
  @Test
  public void testRegisterExpense_Success() {
    when(expenseDAO.save(validExpense)).thenReturn(true);

    boolean result = expenseService.registerExpense(validExpense);

    assertTrue(result);
    verify(expenseDAO).save(validExpense);
  }

  /**
   * CT009 - Atualização de despesa: Testa a atualização de uma despesa existente
   * com sucesso.
   */
  @Test
  public void testUpdateExpense_Success() {
    when(expenseDAO.findById(validExpense.getId())).thenReturn(validExpense);
    when(expenseDAO.update(validExpense)).thenReturn(true);

    boolean result = expenseService.updateExpense(validExpense);

    assertTrue(result);
    verify(expenseDAO).update(validExpense);
  }

  /**
   * CT010 - Atualização de despesa inexistente: Testa a atualização de uma
   * despesa que não existe (deve falhar).
   */
  @Test
  public void testUpdateExpense_NotFound() {
    when(expenseDAO.findById(validExpense.getId())).thenReturn(null);

    boolean result = expenseService.updateExpense(validExpense);

    assertFalse(result);
    verify(expenseDAO, never()).update(any(Expense.class));
  }

}