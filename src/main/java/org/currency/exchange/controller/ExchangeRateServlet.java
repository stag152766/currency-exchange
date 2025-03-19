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

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private final ExchangeRateDAO exchangeRateDAO = new ExchangeRateDAO();

    /**
     * получить все обменные курсы
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("Codes are missing");
        } else {
            getSpecificExchangeRate(resp, pathInfo);
        }
    }

    private void getSpecificExchangeRate(HttpServletResponse resp, String codes) throws IOException {
        ExchangeRate rate = exchangeRateDAO.getExchangeRateByCodes(codes);
        if (rate == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().print("Pair of codes not found");
        } else {
            resp.getWriter().print(ObjectMapperUtil.getInstance().writeValueAsString(rate));
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
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
     * добавить новый обменный курс
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    /**
     * Обновить существующий обменный курс
     */
    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPatch(req, resp);
    }
}
