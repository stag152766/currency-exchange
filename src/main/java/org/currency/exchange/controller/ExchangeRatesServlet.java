package org.currency.exchange.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.currency.exchange.dao.CurrencyDAO;
import org.currency.exchange.dao.ExchangeRateDAO;
import org.currency.exchange.dto.ExchangeRateDto;
import org.currency.exchange.model.ExchangeRate;
import org.currency.exchange.util.ObjectMapperUtil;

import java.io.IOException;
import java.util.List;

@WebServlet("/exchangeRates/*")
public class ExchangeRatesServlet extends HttpServlet {
    private final ExchangeRateDAO exchangeRateDAO = new ExchangeRateDAO(new CurrencyDAO());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            getAllExchangeRates(resp);
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("Database not available");
        }
    }

    private void getAllExchangeRates(HttpServletResponse resp) throws IOException {
        List<ExchangeRate> rates = exchangeRateDAO.getAllExchangeRates();
        String body = ObjectMapperUtil.getInstance().writer().writeValueAsString(rates);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.getWriter().print(body);
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
            } else if (rateExists(rateParams)) {
                //Валютная пара с таким кодом уже существует - 409
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().print("Exchange rate already exists");
            } else {
                int result = exchangeRateDAO.createExchangeRate(rateParams);
                if (result > 0) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().print("Exchange rate created successfully");
                } else {
                    // Одна (или обе) валюта из валютной пары не существует в БД - 404
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().print("Exchange rate not found");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("Database is not available");
        }
    }

    private boolean rateExists(ExchangeRateDto rateDto) {
        String codes = String.join("", "/", rateDto.getBaseCurrencyCode(),
                rateDto.getTargetCurrencyCode());
        ExchangeRate rate = exchangeRateDAO.getExchangeRateByCodes(codes);
        return rate != null;
    }

    private String parseCodes(ExchangeRate rate) {
        if (rate == null) return "";
        return null;
    }

    private boolean invalidValue(ExchangeRateDto rateDto) throws IOException {
        return rateDto.getBaseCurrencyCode() == null
                || rateDto.getTargetCurrencyCode() == null
                || rateDto.getRate() <= 0d;
    }
}
