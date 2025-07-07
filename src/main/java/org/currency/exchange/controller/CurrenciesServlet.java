package org.currency.exchange.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Collection;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;

import org.currency.exchange.dao.CurrencyDAO;
import org.currency.exchange.model.Currency;
import org.currency.exchange.util.ObjectMapperUtil;
import org.currency.exchange.util.ResponseUtil;

@WebServlet(urlPatterns = {"/currencies/*"})
public class CurrenciesServlet extends HttpServlet {
    private final CurrencyDAO currencyDAO = new CurrencyDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            getAllCurrencies(resp);
        } else {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid path for /currencies");
        }
    }

    private void sendErrorResponse(HttpServletResponse resp, int statusCode, String message) throws IOException {
        resp.setStatus(statusCode);
        resp.getWriter().print(message);
    }

    private void getAllCurrencies(HttpServletResponse resp) throws IOException {
     try {
         Collection<Currency> currencies = currencyDAO.getAllCurrencies();
         ResponseUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, currencies);
     } catch (SQLException e) {
         sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database is unavailable");
     }
 }
}
