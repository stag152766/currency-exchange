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
        // check path after /currencies/
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            resp.setContentType("application/json");
            Currency newCurrency = objectMapper.readValue(req.getInputStream(), Currency.class);

            boolean success = currencyDAO.addCurrency(newCurrency);
            if (success) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().println("{\"message\": \"Currency added successfully\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().write("{\"error\": \"Currency already exists\"}");
            }
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"An error occurred while processing the request\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        } else {
            int id = Integer.parseInt(pathInfo.substring(1));
            resp.setContentType("application/json");
            Currency updCurrency = objectMapper.readValue(req.getInputStream(), Currency.class);
            boolean success = currencyDAO.updateBy(id, updCurrency);
            if (success) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().print("Currency was updated successfully");
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("Fail to update currency");
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        } else {
            int id = Integer.parseInt(pathInfo.substring(1));
            boolean success = currencyDAO.deleteById(id);
            if (success) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().print("Currency was deleted successfully");
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("Fail to delete currency");
            }
        }
    }
}
