package org.currency.exchange.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
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
class CurrencyServletTest {
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
        currencyServlet = new CurrencyServlet(currencyDAO);
    }

    @Test
    void shouldReturnErrorWhenDatabaseFailsDuringGet() throws IOException, SQLException {
        // Given
        when(request.getServletPath()).thenReturn("/currency");
        when(request.getPathInfo()).thenReturn("/EUR");
        when(currencyDAO.findByCode("EUR")).thenThrow(new RuntimeException("Database connection failed"));

        // When
        currencyServlet.doGet(request, response);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verify(response).setContentType(CONTENT_TYPE_JSON);
        assertTrue(stringWriter.toString().contains("Database is unavailable"),
                "Response should contain error message");
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
}