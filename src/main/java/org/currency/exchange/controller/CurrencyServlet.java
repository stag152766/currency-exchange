package org.currency.exchange.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.currency.exchange.dao.CurrencyDAO;
import org.currency.exchange.model.Currency;
import org.currency.exchange.util.ObjectMapperUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * REST API for currency operations:
 * - GET /currencies - Get list of all currencies
 * - GET /currency/{code} - Get specific currency by code
 * - POST /currencies - Create new currency
 */
@WebServlet(urlPatterns = {"/currencies/*", "/currency/*"})
public class CurrencyServlet extends HttpServlet {
    private final CurrencyDAO currencyDAO;
    private static final String JSON_CONTENT_TYPE = "application/json";

    public CurrencyServlet() {
        this(new CurrencyDAO());
    }

    // Package-private constructor for testing
    CurrencyServlet(CurrencyDAO currencyDAO) {
        this.currencyDAO = currencyDAO;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String servletPath = req.getServletPath();
        String pathInfo = req.getPathInfo();

        if ("/currencies".equals(servletPath)) {
            if (pathInfo == null || pathInfo.equals("/")) {
                getAllCurrencies(resp);
            } else {
                getSpecificCurrency(resp, pathInfo);
            }
        } else if ("/currency".equals(servletPath)) {
            if (pathInfo == null || pathInfo.equals("/")) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Currency code is required");
            } else {
                getSpecificCurrency(resp, pathInfo);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!"/currencies".equals(req.getServletPath())) {
            return;
        }
        addCurrency(req, resp);
    }

    private void getAllCurrencies(HttpServletResponse resp) throws IOException {
        try {
            Collection<Currency> currencies = currencyDAO.getAllCurrencies();
            sendJsonResponse(resp, HttpServletResponse.SC_OK, currencies);
        } catch (SQLException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database is unavailable");
        }
    }

    private void getSpecificCurrency(HttpServletResponse resp, String pathInfo) throws IOException {
        try {
            String code = pathInfo.substring(1);
            Currency currency = currencyDAO.findByCode(code);
            if (currency == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Currency not found");
            } else {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, currency);
            }
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database is unavailable");
        }
    }

    private void addCurrency(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String name = req.getParameter("name");
            String code = req.getParameter("code");
            String sign = req.getParameter("sign");

            if (name == null || code == null || sign == null || 
                name.trim().isEmpty() || code.trim().isEmpty() || sign.trim().isEmpty()) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Required form fields missing: name, code, sign");
                return;
            }

            Currency newCurrency = new Currency(code, name, sign);

            if (currencyDAO.findByCode(code) != null) {
                sendErrorResponse(resp, HttpServletResponse.SC_CONFLICT, 
                    String.format("Currency with code '%s' already exists", code));
                return;
            }

            boolean success = currencyDAO.addCurrency(newCurrency);
            if (success) {
                Currency createdCurrency = currencyDAO.findByCode(code);
                sendJsonResponse(resp, HttpServletResponse.SC_CREATED, createdCurrency);
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database is unavailable");
            }
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database is unavailable");
        }
    }

    private void sendJsonResponse(HttpServletResponse resp, int status, Object data) throws IOException {
        resp.setStatus(status);
        resp.setContentType(JSON_CONTENT_TYPE);
        String json = ObjectMapperUtil.getInstance().writeValueAsString(data);
        resp.getWriter().println(json);
    }

    private void sendErrorResponse(HttpServletResponse resp, int status, String message) throws IOException {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        sendJsonResponse(resp, status, errorResponse);
    }
}
