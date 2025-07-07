package org.currency.exchange.controller;

import java.io.IOException;

import org.currency.exchange.dao.CurrencyDAO;
import org.currency.exchange.dao.ExchangeRateDAO;
import org.currency.exchange.model.ExchangeRate;
import org.currency.exchange.util.ResponseUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Обменные курсы
 */
@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private ExchangeRateDAO exchangeRateDAO;

    public ExchangeRateServlet() {
        this.exchangeRateDAO = new ExchangeRateDAO(new CurrencyDAO());
    }

    // Constructor for testing
    public ExchangeRateServlet(ExchangeRateDAO exchangeRateDAO) {
        this.exchangeRateDAO = exchangeRateDAO;
    }

    /**
     * Получение конкретного обменного курса
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.length() <= 1) {
            ResponseUtil.sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "{\"error\": \"Exchange rate code is required in the path\"}");
            return;
        }

        String code = pathInfo.substring(1); // remove leading slash
        try {
            ExchangeRate rate = exchangeRateDAO.getExchangeRateByCodes(code);

            if (rate == null) {
                ResponseUtil.sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        "{\"error\": \"Exchange rate not found\"}");
                return;
            }

            ResponseUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, rate);
        } catch (Exception e) {
            ResponseUtil.sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "{\"error\": \"Database is unavailable\"}");
        }
    }

    /**
     * Обновление обменного курса
     */
    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (isInvalidPath(pathInfo)) {
            sendBadRequest(resp, "Exchange rate code is required in the path");
            return;
        }

        String code = extractCode(pathInfo);
        if (!isValidCodeFormat(code)) {
            sendBadRequest(resp, "Invalid exchange rate code format");
            return;
        }

        String baseCurrencyCode = code.substring(0, 3);
        String targetCurrencyCode = code.substring(3, 6);

        try {
            Double rate = extractRate(req, resp);
            if (rate == null)
                return;

            int updateResult = exchangeRateDAO.updateExchangeRate(baseCurrencyCode, targetCurrencyCode, rate);
            if (updateResult > 0) {
                ResponseUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK,
                        "Exchange rate has successfully updated");
            } else {
                ResponseUtil.sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Exchange rate not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database is not available");
        }
    }

    private boolean isInvalidPath(String pathInfo) {
        return pathInfo == null || pathInfo.isBlank() || pathInfo.equals("/");
    }

    private String extractCode(String pathInfo) {
        return pathInfo.substring(1); // remove leading slash
    }

    private boolean isValidCodeFormat(String code) {
        return code.length() == 6;
    }

    private void sendBadRequest(HttpServletResponse resp, String message) throws IOException {
        ResponseUtil.sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, message);
    }

    private Double extractRate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String rateValue = req.getParameter("rate");
        try {
            return Double.parseDouble(rateValue);
        } catch (Exception e) {
            sendBadRequest(resp, "Invalid JSON body");
        }
        return null;
    }
}
