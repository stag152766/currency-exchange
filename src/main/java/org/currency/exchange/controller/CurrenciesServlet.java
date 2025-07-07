package org.currency.exchange.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;

import org.currency.exchange.dao.CurrencyDAO;
import org.currency.exchange.model.Currency;
import org.currency.exchange.util.ResponseUtil;

@WebServlet(urlPatterns = { "/currencies/*" })
public class CurrenciesServlet extends HttpServlet {
     private final CurrencyDAO currencyDAO;

     public CurrenciesServlet() {
          this.currencyDAO = new CurrencyDAO();
     }

     // Constructor for testing
     public CurrenciesServlet(CurrencyDAO currencyDAO) {
          this.currencyDAO = currencyDAO;
     }

     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
          String pathInfo = req.getPathInfo();

          if (pathInfo == null || pathInfo.equals("/")) {
               getAllCurrencies(resp);
          } else {
               ResponseUtil.sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid path for /currencies");
          }
     }

     private void getAllCurrencies(HttpServletResponse resp) throws IOException {
          try {
               Collection<Currency> currencies = currencyDAO.getAllCurrencies();
               ResponseUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, currencies);
          } catch (SQLException e) {
               ResponseUtil.sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                         "Database is unavailable");
          }
     }

     /**
      * Добавление новой валюты в базу
      */
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
          if (!"/currencies".equals(req.getServletPath())) {
               ResponseUtil.sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Not found");
               return;
          }
          addCurrency(req, resp);
     }

     private void addCurrency(HttpServletRequest req, HttpServletResponse resp) throws IOException {
          try {
               String name = req.getParameter("name");
               String code = req.getParameter("code");
               String sign = req.getParameter("sign");

               if (name == null || code == null || sign == null ||
                         name.trim().isEmpty() || code.trim().isEmpty() || sign.trim().isEmpty()) {
                    ResponseUtil.sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                              "Required form fields missing: name, code, sign");
                    return;
               }

               Currency newCurrency = new Currency(code, name, sign);

               if (currencyDAO.findByCode(code) != null) {
                    ResponseUtil.sendErrorResponse(resp, HttpServletResponse.SC_CONFLICT,
                              String.format("Currency with code '%s' already exists", code));
                    return;
               }

               boolean success = currencyDAO.addCurrency(newCurrency);
               if (success) {
                    Currency createdCurrency = currencyDAO.findByCode(code);
                    ResponseUtil.sendJsonResponse(resp, HttpServletResponse.SC_CREATED, createdCurrency);
               } else {
                    ResponseUtil.sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                              "Database is unavailable");
               }
          } catch (Exception e) {
               ResponseUtil.sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                         "Database is unavailable");
          }
     }
}
