package org.currency.exchange.controller;

import java.io.IOException;
import java.util.List;

import org.currency.exchange.dao.CurrencyDAO;
import org.currency.exchange.dao.ExchangeRateDAO;
import org.currency.exchange.dto.ExchangeRateDto;
import org.currency.exchange.model.ExchangeRate;
import org.currency.exchange.util.ObjectMapperUtil;
import org.currency.exchange.util.ResponseUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Обменные курсы
 */
@WebServlet(urlPatterns = { "/exchangeRates/*" })
public class ExchangeRatesServlet extends HttpServlet {
    private final ExchangeRateDAO exchangeRateDAO;

    public ExchangeRatesServlet() {
        this.exchangeRateDAO = new ExchangeRateDAO(new CurrencyDAO());
    }

    // Package-private constructor for testing
    ExchangeRatesServlet(ExchangeRateDAO exchangeRateDAO) {
        this.exchangeRateDAO = exchangeRateDAO;
    }

    /**
     * Получение списка всех обменных курсов
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
        
        try {
            if ("/exchangeRates".equals(servletPath)) {
                getAllExchangeRates(resp);
            } else {
                ResponseUtil.sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        "Invalid path");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database is unavailable");
        }
    }

    private void getAllExchangeRates(HttpServletResponse resp) throws IOException {
        List<ExchangeRate> rates = exchangeRateDAO.getAllExchangeRates();
        ResponseUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, rates);
    }

    /**
     * Добавление нового обменного курса в базу
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        addIfNotExists(req, resp);
    }

    private void addIfNotExists(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            ExchangeRateDto rateParams = ObjectMapperUtil.getInstance().readValue(req.getInputStream(),
                    ExchangeRateDto.class);
            if (invalidValue(rateParams)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("Required field is missing");
                return;
            }

            if (rateExists(rateParams)) {
                // Валютная пара с таким кодом уже существует - 409
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().print("Exchange rate already exists");
                return;
            }

            int result = exchangeRateDAO.createExchangeRate(rateParams);
            if (result > 0) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().print("Exchange rate created successfully");
            } else {
                // Одна (или обе) валюта из валютной пары не существует в БД - 404
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().print("One or both currencies not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("Database is unavailable");
        }
    }

    private boolean rateExists(ExchangeRateDto rateDto) {
        String codes = String.join("", "/", rateDto.getBaseCurrencyCode(),
                rateDto.getTargetCurrencyCode());
        ExchangeRate rate = exchangeRateDAO.getExchangeRateByCodes(codes);
        return rate != null;
    }

    private boolean invalidValue(ExchangeRateDto rateDto) throws IOException {
        return rateDto.getBaseCurrencyCode() == null
                || rateDto.getTargetCurrencyCode() == null
                || rateDto.getRate() <= 0d;
    }
}
