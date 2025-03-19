package org.currency.exchange.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.currency.exchange.dao.ExchangeRateDAO;
import org.currency.exchange.model.ExchangeRate;
import org.currency.exchange.util.ObjectMapperUtil;

import java.io.IOException;
import java.util.List;

@WebServlet("/exchangeRates/*")
public class ExchangeRatesServlet extends HttpServlet {
    private final ExchangeRateDAO exchangeRateDAO = new ExchangeRateDAO();

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
            ExchangeRate newRate = ObjectMapperUtil.getInstance().readValue(req.getInputStream(), ExchangeRate.class);
            if (invalidValue(newRate)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("Required field is missing");
            } else if (rateExists(newRate)) {

            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("Database is not available");
        }
    }

    private boolean rateExists(ExchangeRate newRate) {
        String codes = parseCodes(newRate);
        ExchangeRate currRate = exchangeRateDAO.getExchangeRateByCodes(codes);
        return currRate != null;
    }

    private String parseCodes(ExchangeRate rate) {
        if (rate == null) return "";
        return null;
    }

    private boolean invalidValue(ExchangeRate newRate) throws IOException {
        return newRate.getBaseCurrency() == null || newRate.getTargetCurrency() == null;
    }
}
