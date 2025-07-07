package org.currency.exchange.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.currency.exchange.dao.CurrencyDAO;
import org.currency.exchange.dao.DatabaseException;
import org.currency.exchange.dao.ExchangeRateDAO;
import org.currency.exchange.dto.ExchangeRateDto;
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

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class ExchangeRatesServletTest {
     private ExchangeRatesServlet exchangeRatesServlet;
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

     private static final Currency BASE = new Currency(1, "US Dollar", "USD", "$"),
               TARGET = new Currency(2, "Euro", "EUR", "€");
     private static final ExchangeRate TEST_RATE = new ExchangeRate(1, BASE, TARGET, 1.23);

     @BeforeEach
     void setUp() throws IOException {
          stringWriter = new StringWriter();
          writer = new PrintWriter(stringWriter);
          when(response.getWriter()).thenReturn(writer);
          exchangeRatesServlet = new ExchangeRatesServlet(exchangeRateDAO);
     }

     // Helper to convert String to ServletInputStream
     private ServletInputStream toServletInputStream(String str) {
          ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(str.getBytes());
          return new ServletInputStream() {
               @Override
               public int read() throws IOException {
                    return byteArrayInputStream.read();
               }

               @Override
               public boolean isFinished() {
                    return byteArrayInputStream.available() == 0;
               }

               @Override
               public boolean isReady() {
                    return true;
               }

               @Override
               public void setReadListener(jakarta.servlet.ReadListener readListener) {
               }
          };
     }

     @Test
     void shouldReturnAllExchangeRates() throws Exception {
          when(request.getServletPath()).thenReturn("/exchangeRates");
          when(exchangeRateDAO.getAllExchangeRates()).thenReturn(List.of(TEST_RATE));

          exchangeRatesServlet.doGet(request, response);

          writer.flush();
          verify(response).setStatus(HttpServletResponse.SC_OK);
          String expectedJson = ObjectMapperUtil.getInstance().writeValueAsString(List.of(TEST_RATE));
          ObjectMapper mapper = new ObjectMapper();
          JsonNode actualJson = mapper.readTree(stringWriter.toString());
          JsonNode expectedJsonNode = mapper.readTree(expectedJson);
          assertEquals(expectedJsonNode, actualJson);
     }

     @Test
     void shouldReturnErrorForInvalidPath() throws Exception {
          when(request.getServletPath()).thenReturn("/invalid");

          exchangeRatesServlet.doGet(request, response);

          writer.flush();
          verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
          assertTrue(stringWriter.toString().contains("Invalid path"));
     }

     @Test
     void shouldAddNewExchangeRate() throws Exception {
          ExchangeRateDto dto = new ExchangeRateDto();
          dto.setBaseCurrencyCode("USD");
          dto.setTargetCurrencyCode("EUR");
          dto.setRate(1.23);
          String json = ObjectMapperUtil.getInstance().writeValueAsString(dto);
          when(request.getInputStream()).thenReturn(toServletInputStream(json));
          when(exchangeRateDAO.getExchangeRateByCodes(any())).thenReturn(null);
          when(exchangeRateDAO.createExchangeRate(any())).thenReturn(1);

          exchangeRatesServlet.doPost(request, response);

          writer.flush();
          verify(response).setStatus(HttpServletResponse.SC_CREATED);
          assertTrue(stringWriter.toString().contains("Exchange rate created successfully"));
     }

     @Test
     void shouldReturnConflictIfExchangeRateExists() throws Exception {
          ExchangeRateDto dto = new ExchangeRateDto();
          dto.setBaseCurrencyCode("USD");
          dto.setTargetCurrencyCode("EUR");
          dto.setRate(1.23);
          String json = ObjectMapperUtil.getInstance().writeValueAsString(dto);
          when(request.getInputStream()).thenReturn(toServletInputStream(json));
          when(exchangeRateDAO.getExchangeRateByCodes(any())).thenReturn(TEST_RATE);

          exchangeRatesServlet.doPost(request, response);

          writer.flush();
          verify(response).setStatus(HttpServletResponse.SC_CONFLICT);
          assertTrue(stringWriter.toString().contains("Exchange rate already exists"));
     }

     @Test
     void shouldReturnNotFoundIfCurrencyMissing() throws Exception {
          ExchangeRateDto dto = new ExchangeRateDto();
          dto.setBaseCurrencyCode("USD");
          dto.setTargetCurrencyCode("EUR");
          dto.setRate(1.23);
          String json = ObjectMapperUtil.getInstance().writeValueAsString(dto);
          when(request.getInputStream()).thenReturn(toServletInputStream(json));
          when(exchangeRateDAO.getExchangeRateByCodes(any())).thenReturn(null);
          when(exchangeRateDAO.createExchangeRate(any())).thenReturn(0);

          exchangeRatesServlet.doPost(request, response);

          writer.flush();
          verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
          assertTrue(stringWriter.toString().contains("One or both currencies not found"));
     }

     @Test
     void shouldReturnBadRequestIfInvalidValue() throws Exception {
          ExchangeRateDto dto = new ExchangeRateDto();
          dto.setBaseCurrencyCode(null);
          dto.setTargetCurrencyCode("EUR");
          dto.setRate(1.23);
          String json = ObjectMapperUtil.getInstance().writeValueAsString(dto);
          when(request.getInputStream()).thenReturn(toServletInputStream(json));

          exchangeRatesServlet.doPost(request, response);

          writer.flush();
          verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
          assertTrue(stringWriter.toString().contains("Required field is missing"));
     }

     @Test
     void shouldReturnInternalServerErrorOnException() throws Exception {
          when(request.getServletPath()).thenReturn("/exchangeRates");
          when(exchangeRateDAO.getAllExchangeRates()).thenThrow(new RuntimeException("DB error"));

          exchangeRatesServlet.doGet(request, response);
          
          writer.flush();
          verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          assertTrue(stringWriter.toString().contains("Database is unavailable"));
     }
}