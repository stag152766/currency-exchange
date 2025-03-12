package org.currency.exchange.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.currency.exchange.dao.CurrencyDAO;
import org.currency.exchange.model.Currency;

import java.io.IOException;
import java.util.Collection;

@WebServlet("/currencies/*")
public class CurrencyServlet extends HttpServlet {
    private final CurrencyDAO currencyDAO = new CurrencyDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * получить список всех валют
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        // проверка текста после /currencies/
        if (pathInfo == null || pathInfo.equals("/")) {
            getAllCurrencies(resp);
        } else {
            getSpecificCurrency(resp);
        }
    }

    private void getSpecificCurrency(HttpServletResponse resp) throws IOException {
        resp.getWriter().println("GET request /currencies/{id} handle");
    }

    private void getAllCurrencies(HttpServletResponse resp) throws IOException {
        try {
            Collection<Currency> currencies = currencyDAO.getAllCurrencies();
            String result = objectMapper.writeValueAsString(currencies);
            resp.setContentType("application/json");
            resp.getWriter().println(result);
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("Database error: " + e.getMessage());
        }
    }

    /**
     * добавить новую валюту
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }
}
