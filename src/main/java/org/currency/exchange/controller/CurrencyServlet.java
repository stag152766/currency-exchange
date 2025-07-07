package org.currency.exchange.controller;

import java.io.IOException;

import org.currency.exchange.dao.CurrencyDAO;
import org.currency.exchange.model.Currency;
import org.currency.exchange.util.ResponseUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Валюты
 */
@WebServlet(urlPatterns = { "/currency/*" })
public class CurrencyServlet extends HttpServlet {
    private final CurrencyDAO currencyDAO;

    public CurrencyServlet() {
        this(new CurrencyDAO());
    }

    // Package-private constructor for testing
    CurrencyServlet(CurrencyDAO currencyDAO) {
        this.currencyDAO = currencyDAO;
    }

    /**
     * Получение конкретной валюты
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String servletPath = req.getServletPath();
        String pathInfo = req.getPathInfo();

        if ("/currency".equals(servletPath)) {
            if (pathInfo == null || pathInfo.equals("/")) {
                ResponseUtil.sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Currency code is required");
            } else {
                getSpecificCurrency(resp, pathInfo);
            }
        } else {
            ResponseUtil.sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Not found");
        }
    }

    private void getSpecificCurrency(HttpServletResponse resp, String pathInfo) throws IOException {
        try {
            String code = pathInfo.substring(1);
            Currency currency = currencyDAO.findByCode(code);
            if (currency == null) {
                ResponseUtil.sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Currency not found");
            } else {
                ResponseUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, currency);
            }
        } catch (Exception e) {
            ResponseUtil.sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database is unavailable");
        }
    }
}
