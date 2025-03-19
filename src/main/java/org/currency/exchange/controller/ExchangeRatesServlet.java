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

    }
}
