package org.currency.exchange.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.currency.exchange.dao.CurrencyDAO;
import org.currency.exchange.model.Currency;
import org.currency.exchange.util.ObjectMapperUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class CurrenciesServletTest {
     private CurrenciesServlet currenciesServlet;
     @Mock
     private HttpServletRequest request;
     @Mock
     private HttpServletResponse response;
     @Mock
     private CurrencyDAO currencyDAO;
     private StringWriter stringWriter;
     private PrintWriter writer;

     private static final String CONTENT_TYPE_JSON = "application/json";
     private static final Currency TEST_CURRENCY = new Currency("EUR", "Euro", "€");
     private static final Currency TEST_USD = new Currency("USD", "United States dollar", "$");

     @BeforeEach
     void setUp() throws IOException {
          stringWriter = new StringWriter();
          writer = new PrintWriter(stringWriter);
          when(response.getWriter()).thenReturn(writer);
          currenciesServlet = new CurrenciesServlet(currencyDAO);
     }

     @Test
     void shouldReturnAllCurrenciesWhenGetRequestIsValid() throws IOException, SQLException {
          // Given
          Collection<Currency> currencies = List.of(TEST_USD, TEST_CURRENCY);
          when(currencyDAO.getAllCurrencies()).thenReturn(currencies);
          when(request.getPathInfo()).thenReturn(null);

          // When
          currenciesServlet.doGet(request, response);

          // Then
          verify(response).setStatus(HttpServletResponse.SC_OK);
          verify(response).setContentType(CONTENT_TYPE_JSON);
          String expectedJson = ObjectMapperUtil.getInstance().writeValueAsString(currencies);
          assertTrue(stringWriter.toString().contains(expectedJson),
                    "Response should contain: " + expectedJson + "\nBut was: " + stringWriter.toString());
          verify(currencyDAO, times(1)).getAllCurrencies();
     }

     @Test
     void shouldReturnErrorWhenDatabaseFailsDuringGet() throws IOException, SQLException {
          // Given
          when(request.getPathInfo()).thenReturn(null);
          when(currencyDAO.getAllCurrencies()).thenThrow(new SQLException("Database connection failed"));

          // When
          currenciesServlet.doGet(request, response);

          // Then
          verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          verify(response).setContentType(CONTENT_TYPE_JSON);
          assertTrue(stringWriter.toString().contains("Database is unavailable"),
                    "Response should contain error message");
     }

     @Test
     void shouldReturnBadRequestForInvalidPath() throws IOException {
          // Given
          when(request.getPathInfo()).thenReturn("/invalid");

          // When
          currenciesServlet.doGet(request, response);

          // Then
          verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
          assertTrue(stringWriter.toString().contains("Invalid path for /currencies"),
                    "Response should contain bad request message");
     }

     @Test
     void shouldReturnMethodNotAllowedForPost() throws IOException {
          // Given
          when(request.getServletPath()).thenReturn(null);

          // When
          currenciesServlet.doPost(request, response);

          // Then
          verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
          verify(response).setContentType("application/json");
          assertTrue(stringWriter.toString().contains("Not found"));
     }

     @Test
     void shouldCreateCurrencyWhenValidDataProvided() throws IOException {
          // Given
          when(request.getServletPath()).thenReturn("/currencies");
          when(request.getParameter("name")).thenReturn("Euro");
          when(request.getParameter("code")).thenReturn("EUR");
          when(request.getParameter("sign")).thenReturn("€");

          Currency savedCurrency = new Currency("EUR", "Euro", "€");
          when(currencyDAO.findByCode("EUR")).thenReturn(null).thenReturn(savedCurrency);
          when(currencyDAO.addCurrency(any(Currency.class))).thenReturn(true);

          // When
          currenciesServlet.doPost(request, response);

          // Then
          verify(response).setStatus(HttpServletResponse.SC_CREATED);
          verify(response).setContentType("application/json");
          String expectedJson = ObjectMapperUtil.getInstance().writeValueAsString(savedCurrency);
          assertTrue(stringWriter.toString().contains(expectedJson));
     }

     @Test
     void shouldReturnConflictWhenCurrencyAlreadyExists() throws IOException {
          // Given
          when(request.getServletPath()).thenReturn("/currencies");
          when(request.getParameter("name")).thenReturn("Euro");
          when(request.getParameter("code")).thenReturn("EUR");
          when(request.getParameter("sign")).thenReturn("€");
          when(currencyDAO.findByCode("EUR")).thenReturn(TEST_CURRENCY);

          // When
          currenciesServlet.doPost(request, response);

          // Then
          verify(response).setStatus(HttpServletResponse.SC_CONFLICT);
          verify(response).setContentType(CONTENT_TYPE_JSON);
          assertTrue(stringWriter.toString().contains("Currency with code 'EUR' already exists"),
                    "Response should contain error message about existing currency");
     }

     @Test
     void shouldReturnBadRequestWhenInvalidDataProvided() throws IOException {
          // Given
          when(request.getServletPath()).thenReturn("/currencies");
          when(request.getParameter("name")).thenReturn(null);
          when(request.getParameter("code")).thenReturn("EUR");
          when(request.getParameter("sign")).thenReturn("€");

          // When
          currenciesServlet.doPost(request, response);

          // Then
          verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
          verify(response).setContentType(CONTENT_TYPE_JSON);
          assertTrue(stringWriter.toString().contains("Required form fields missing: name, code, sign"),
                    "Response should contain message about missing required fields");
     }

     @Test
     void shouldReturnInternalErrorWhenDatabaseFailsDuringPost() throws IOException {
          // Given
          when(request.getServletPath()).thenReturn("/currencies");
          when(request.getParameter("name")).thenReturn("Euro");
          when(request.getParameter("code")).thenReturn("EUR");
          when(request.getParameter("sign")).thenReturn("€");
          when(currencyDAO.findByCode("EUR")).thenReturn(null);
          when(currencyDAO.addCurrency(any(Currency.class))).thenThrow(new RuntimeException("Database error"));

          // When
          currenciesServlet.doPost(request, response);

          // Then
          verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          verify(response).setContentType(CONTENT_TYPE_JSON);
          assertTrue(stringWriter.toString().contains("Database is unavailable"),
                    "Response should contain database error message");
     }

     @Test
     void shouldReturnAllCurrenciesWhenPathInfoIsSlash() throws IOException, SQLException {
          // Given
          Collection<Currency> currencies = List.of(TEST_USD, TEST_CURRENCY);
          when(currencyDAO.getAllCurrencies()).thenReturn(currencies);
          when(request.getPathInfo()).thenReturn("/");

          // When
          currenciesServlet.doGet(request, response);

          // Then
          verify(response).setStatus(HttpServletResponse.SC_OK);
          verify(response).setContentType(CONTENT_TYPE_JSON);
          String expectedJson = ObjectMapperUtil.getInstance().writeValueAsString(currencies);
          assertTrue(stringWriter.toString().contains(expectedJson));
          verify(currencyDAO, times(1)).getAllCurrencies();
     }

     @Test
     void shouldReturnNotFoundForWrongServletPath() throws IOException {
          // Given
          when(request.getServletPath()).thenReturn("/wrong");

          // When
          currenciesServlet.doPost(request, response);

          // Then
          verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
          verify(response).setContentType("application/json");
          assertTrue(stringWriter.toString().contains("Not found"));
     }
}
