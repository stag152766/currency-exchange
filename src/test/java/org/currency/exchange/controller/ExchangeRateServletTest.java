package org.currency.exchange.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.currency.exchange.dao.CurrencyDAO;
import org.currency.exchange.dao.ExchangeRateDAO;
import org.currency.exchange.model.Currency;
import org.currency.exchange.model.ExchangeRate;
import org.currency.exchange.util.ObjectMapperUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServletTest {
     private ExchangeRateServlet exchangeRateServlet;
     @Mock
     private HttpServletRequest request;
     @Mock
     private HttpServletResponse response;
     @Mock
     private ExchangeRateDAO exchangeRateDAO;
     @Mock
     private CurrencyDAO currencyDAO;
     private StringWriter stringWriter;
     private PrintWriter writer;

     private static final Currency TEST_BASE_CURRENCY = new Currency(1, "US Dollar", "USD", "$");
     private static final Currency TEST_TARGET_CURRENCY = new Currency(2, "Euro", "EUR", "€");
     private static final ExchangeRate TEST_RATE = new ExchangeRate(1, TEST_BASE_CURRENCY, TEST_TARGET_CURRENCY, 1.23);

     @BeforeEach
     void setUp() throws IOException {
          stringWriter = new StringWriter();
          writer = new PrintWriter(stringWriter);
          when(response.getWriter()).thenReturn(writer);
          exchangeRateServlet = new ExchangeRateServlet(exchangeRateDAO);
     }

     @Test
     void shouldReturnBadRequestWhenPathInfoMissing() throws IOException, jakarta.servlet.ServletException {
          when(request.getPathInfo()).thenReturn(null);

          exchangeRateServlet.doGet(request, response);

          verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
          assertTrue(stringWriter.toString().contains("Exchange rate code is required in the path"));
     }

     @Test
     void shouldReturnNotFoundWhenRateDoesNotExist() throws IOException, jakarta.servlet.ServletException {
          when(request.getPathInfo()).thenReturn("/USDEUR");
          when(exchangeRateDAO.getExchangeRateByCodes("USDEUR")).thenReturn(null);

          exchangeRateServlet.doGet(request, response);

          verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
          assertTrue(stringWriter.toString().contains("Exchange rate not found"));
     }

     @Test
     void shouldReturnExchangeRateWhenExists() throws IOException, jakarta.servlet.ServletException {
          when(request.getPathInfo()).thenReturn("/USDEUR");
          when(exchangeRateDAO.getExchangeRateByCodes("USDEUR")).thenReturn(TEST_RATE);
          String expectedJson = ObjectMapperUtil.getInstance().writer().writeValueAsString(TEST_RATE);

          exchangeRateServlet.doGet(request, response);
          
          writer.flush();
          verify(response).setStatus(HttpServletResponse.SC_OK);
          ObjectMapper mapper = new ObjectMapper();
          JsonNode actualJson = mapper.readTree(stringWriter.toString());
          JsonNode expectedJsonNode = mapper.readTree(expectedJson);
          assertEquals(expectedJsonNode, actualJson);
     }

     @Test
     void shouldReturnInternalServerErrorOnException() throws IOException, jakarta.servlet.ServletException {
          when(request.getPathInfo()).thenReturn("/USDEUR");
          when(exchangeRateDAO.getExchangeRateByCodes(any())).thenThrow(new RuntimeException("DB error"));

          exchangeRateServlet.doGet(request, response);

          verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          assertTrue(stringWriter.toString().contains("Database is unavailable"));
     }

     @Test
     void shouldUpdateExchangeRateSuccessfully() throws Exception {
          when(request.getPathInfo()).thenReturn("/USDEUR");
          when(request.getParameter("rate")).thenReturn("2.34");
          when(exchangeRateDAO.updateExchangeRate("USD", "EUR", 2.34)).thenReturn(1);

          exchangeRateServlet.doPatch(request, response);

          writer.flush();
          verify(response).setStatus(HttpServletResponse.SC_OK);
          assertTrue(stringWriter.toString().contains("Exchange rate has successfully updated"));
     }

     @Test
     void shouldReturnNotFoundWhenUpdateFails() throws Exception {
          when(request.getPathInfo()).thenReturn("/USDEUR");
          when(request.getParameter("rate")).thenReturn("2.34");
          when(exchangeRateDAO.updateExchangeRate("USD", "EUR", 2.34)).thenReturn(0);

          exchangeRateServlet.doPatch(request, response);

          writer.flush();
          verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
          assertTrue(stringWriter.toString().contains("Exchange rate not found"));
     }

     @Test
     void shouldReturnBadRequestWhenRateMissing() throws Exception {
          when(request.getPathInfo()).thenReturn("/USDEUR");
          when(request.getParameter("rate")).thenReturn(null);

          exchangeRateServlet.doPatch(request, response);

          verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
          assertTrue(stringWriter.toString().contains("Invalid JSON body"));
     }

     @Test
     void shouldReturnBadRequestWhenRateIsInvalid() throws Exception {
          when(request.getPathInfo()).thenReturn("/USDEUR");
          when(request.getParameter("rate")).thenReturn("notANumber");

          exchangeRateServlet.doPatch(request, response);

          writer.flush();
          verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
          assertTrue(stringWriter.toString().contains("Invalid JSON body"));
     }

     @Test
     void shouldReturnInternalServerErrorOnPatchException() throws Exception {
          when(request.getPathInfo()).thenReturn("/USDEUR");
          when(request.getParameter("rate")).thenReturn("2.34");
          when(exchangeRateDAO.updateExchangeRate(any(), any(), anyDouble()))
                    .thenThrow(new RuntimeException("DB error"));

          exchangeRateServlet.doPatch(request, response);

          writer.flush();
          verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          assertTrue(stringWriter.toString().contains("Database is not available"));
     }
}
