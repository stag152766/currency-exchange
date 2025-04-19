package org.currency.exchange.controller;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.currency.exchange.dao.CurrencyDAO;
import org.currency.exchange.model.Currency;
import org.currency.exchange.util.ObjectMapperUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InOrder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyServletTest {
    @InjectMocks
    private CurrencyServlet currencyServlet;
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
    }

    @Test
    void shouldReturnAllCurrenciesWhenGetRequestIsValid() throws IOException, SQLException {
        // Given
        Collection<Currency> currencies = List.of(TEST_USD, TEST_CURRENCY);
        when(currencyDAO.getAllCurrencies()).thenReturn(currencies);
        when(request.getServletPath()).thenReturn("/currencies");
        when(request.getPathInfo()).thenReturn(null);

        // When
        currencyServlet.doGet(request, response);

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
        when(request.getServletPath()).thenReturn("/currencies");
        when(request.getPathInfo()).thenReturn(null);
        when(currencyDAO.getAllCurrencies()).thenThrow(new SQLException("Database connection failed"));

        // When
        currencyServlet.doGet(request, response);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verify(response).setContentType(CONTENT_TYPE_JSON);
        assertTrue(stringWriter.toString().contains("Database is unavailable"),
                "Response should contain error message");
    }

    @Test
    void shouldCreateCurrencyWhenValidDataProvided() throws IOException {
        // Given
        when(request.getServletPath()).thenReturn("/currencies");
        when(request.getParameter("name")).thenReturn("Euro");
        when(request.getParameter("code")).thenReturn("EUR");
        when(request.getParameter("sign")).thenReturn("€");
        
        Currency savedCurrency = new Currency("EUR", "Euro", "€");
        
        // First call returns null (doesn't exist), second call returns the saved currency
        when(currencyDAO.findByCode("EUR"))
            .thenReturn(null)
            .thenReturn(savedCurrency);
        
        when(currencyDAO.addCurrency(any(Currency.class))).thenReturn(true);

        // When
        currencyServlet.doPost(request, response);

        // Then
        InOrder inOrder = inOrder(currencyDAO, response);
        
        // Verify the sequence of operations
        inOrder.verify(currencyDAO).findByCode("EUR"); // First check if exists
        inOrder.verify(currencyDAO).addCurrency(any(Currency.class)); // Then add
        inOrder.verify(currencyDAO).findByCode("EUR"); // Then get the saved currency
        inOrder.verify(response).setStatus(HttpServletResponse.SC_CREATED);
        inOrder.verify(response).setContentType(CONTENT_TYPE_JSON);

        String expectedJson = ObjectMapperUtil.getInstance().writeValueAsString(savedCurrency);
        assertTrue(stringWriter.toString().contains(expectedJson),
                "Response should contain: " + expectedJson + "\nbut contains " + stringWriter.toString());
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
        currencyServlet.doPost(request, response);

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
        currencyServlet.doPost(request, response);

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
        currencyServlet.doPost(request, response);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verify(response).setContentType(CONTENT_TYPE_JSON);
        assertTrue(stringWriter.toString().contains("Database is unavailable"),
                "Response should contain database error message");
    }

    @Test
    void shouldReturnSpecificCurrencyWhenValidCodeProvided() throws IOException {
        // Given
        when(request.getServletPath()).thenReturn("/currency");
        when(request.getPathInfo()).thenReturn("/EUR");
        when(currencyDAO.findByCode("EUR")).thenReturn(TEST_CURRENCY);

        // When
        currencyServlet.doGet(request, response);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).setContentType(CONTENT_TYPE_JSON);
        String expectedJson = ObjectMapperUtil.getInstance().writeValueAsString(TEST_CURRENCY);
        assertTrue(stringWriter.toString().contains(expectedJson),
                "Response should contain: " + expectedJson + "\nBut was: " + stringWriter.toString());
    }

    @Test
    void shouldReturnNotFoundWhenCurrencyCodeDoesNotExist() throws IOException {
        // Given
        when(request.getServletPath()).thenReturn("/currency");
        when(request.getPathInfo()).thenReturn("/XYZ");
        when(currencyDAO.findByCode("XYZ")).thenReturn(null);

        // When
        currencyServlet.doGet(request, response);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(response).setContentType(CONTENT_TYPE_JSON);
        assertTrue(stringWriter.toString().contains("Currency not found"),
                "Response should contain not found message");
    }

    private static class MockServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream inputStream;

        public MockServletInputStream(String content) {
            inputStream = new ByteArrayInputStream(content.getBytes());
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException("not required for test");
        }
    }
}