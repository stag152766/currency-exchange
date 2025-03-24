package org.currency.exchange.controller;

import jakarta.servlet.ServletException;
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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @BeforeEach
    void setUp() throws IOException {
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testGetAllCurrencies_Success() throws ServletException, IOException {
        Collection<Currency> currencies = buildCurrencies();
        when(currencyDAO.getAllCurrencies()).thenReturn(currencies);
        when(request.getPathInfo()).thenReturn(null);

        currencyServlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).setContentType("application/json");
        String expectedJson = ObjectMapperUtil.getInstance().writeValueAsString(currencies);
        assertTrue(stringWriter.toString().contains(expectedJson),
                "Response should contain: " + expectedJson + "\nBut was: " + stringWriter.toString());
    }

    private Collection<Currency> buildCurrencies() {
        return List.of(
                new Currency(0, "United States dollar", "USD", "$"),
                new Currency(1, "Euro", "EUR", "€")
        );
    }

    @Test
    void doPost() {
    }

    @Test
    void doPatch() {
    }

    @Test
    void doDelete() {
    }
}
