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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
    // in-memory container that captures what the servlet would send to the client
    private StringWriter stringWriter;
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws IOException {
        stringWriter = new StringWriter();
        // wraps to provide print methods
        writer = new PrintWriter(stringWriter);
        // exactly what a real HttpServletResponse would provide
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testGetAllCurrencies_Success() throws IOException, SQLException {
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
    void testGetAllCurrencies_Error() throws IOException, SQLException {
        when(request.getPathInfo()).thenReturn(null);
        when(currencyDAO.getAllCurrencies()).thenThrow(new SQLException());

        currencyServlet.doGet(request, response);

        // checks if the method was called with that parameter
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        // assertEquals(500, response.getStatus()); // False because the mock doesn't maintain state
        assertTrue(stringWriter.toString().contains("Database is unavailable"),
                "Response should contain error message");
    }

    @Test
    void testAddCurrency_Success() throws IOException {
        when(currencyDAO.addCurrency(any())).thenReturn(true);
        Currency newCurrency = new Currency(0, "Euro", "EUR", "€");
        String newCurrencyJson = ObjectMapperUtil.getInstance().writeValueAsString(newCurrency);
        when(request.getInputStream()).thenReturn(new MockServletInputStream(newCurrencyJson));

        currencyServlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        verify(response).setContentType("application/json");
        String expectedResult = ObjectMapperUtil.getInstance().writeValueAsString(newCurrency);
        assertTrue(stringWriter.toString().contains(expectedResult),
                "Response should contain: " + expectedResult +
                        "\nbut contains " + stringWriter.toString());
    }

    @Test
    void testAddCurrency_Exist() throws IOException {
        when(currencyDAO.addCurrency(any())).thenReturn(false);
        Currency newCurrency = new Currency(0, "Euro", "EUR", "€");
        String newCurrencyJson = ObjectMapperUtil.getInstance().writeValueAsString(newCurrency);
        when(request.getInputStream()).thenReturn(new MockServletInputStream(newCurrencyJson));

        currencyServlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CONFLICT);
        verify(response).setContentType("application/json");
        String expectedResult = "{\"error\": \"Currency with code \"Euro\" already exists\"}";
        assertTrue(stringWriter.toString().contains(expectedResult),
                "Response should contain: " + expectedResult +
                        "\nbut contains " + stringWriter.toString());
    }

    @Test
    void testAddCurrency_InvalidData() throws IOException {
        Currency newCurrency = new Currency(0, null, "EUR", "€");
        String newCurrencyJson = ObjectMapperUtil.getInstance().writeValueAsString(newCurrency);
        when(request.getInputStream()).thenReturn(new MockServletInputStream(newCurrencyJson));

        currencyServlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(response).getWriter();
        assertEquals("{\"error\": \"Wrong data\"}", stringWriter.toString());
    }

    @Test
    void testAddCurrency_InternalError() throws IOException {
        Currency newCurrency = new Currency(0, "Euro", "EUR", "€");
        String newCurrencyJson = ObjectMapperUtil.getInstance().writeValueAsString(newCurrency);
        when(request.getInputStream()).thenReturn(new MockServletInputStream(newCurrencyJson));
        when(currencyDAO.addCurrency(any())).thenThrow(new RuntimeException());

        currencyServlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verify(response).setContentType("application/json");
        //assertTrue(response.getWriter().toString().contains("An error occurred while processing the request"));
        assertEquals("{\"error\": \"An error occurred while processing the request\"}",
                stringWriter.toString());
    }


    void doPatch() {
    }


    void doDelete() {
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
