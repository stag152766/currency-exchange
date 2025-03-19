package org.currency.exchange.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.currency.exchange.dao.CurrencyDAO;
import org.currency.exchange.model.Currency;
import org.currency.exchange.util.ObjectMapperUtil;

import java.io.IOException;
import java.util.Collection;

@WebServlet("/currencies/*")
public class CurrencyServlet extends HttpServlet {
    private final CurrencyDAO currencyDAO = new CurrencyDAO();

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
            getSpecificCurrency(resp, pathInfo);
        }
    }


    private void getSpecificCurrency(HttpServletResponse resp, String pathInfo) throws IOException {
        try {
            String currencyCode = pathInfo.substring(1);
            Currency currency = currencyDAO.findByCode(currencyCode);

            if (currency == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().print("Currency not found");
            } else {
                resp.setStatus(HttpServletResponse.SC_OK);
                String result = ObjectMapperUtil.getInstance().writeValueAsString(currency);
                resp.setContentType("application/json");
                resp.getWriter().println(result);
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("Database is unavailable");
        }
    }

    private void getAllCurrencies(HttpServletResponse resp) throws IOException {
        try {
            Collection<Currency> currencies = currencyDAO.getAllCurrencies();
            String result = ObjectMapperUtil.getInstance().writeValueAsString(currencies);
            resp.setStatus(HttpServletResponse.SC_OK);
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
        addCurrencyIfNotExists(req, resp);
    }

    private void addCurrencyIfNotExists(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            resp.setContentType("application/json");
            Currency newCurrency = ObjectMapperUtil.getInstance().readValue(req.getInputStream(), Currency.class);
            if (invalidValue(newCurrency)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("Wrong values");
            }
            if (exists(newCurrency)) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().print("Currency already exists");
                return;
            }

            boolean success = currencyDAO.addCurrency(newCurrency);
            if (success) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().println("{\"message\": \"Currency added successfully\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().write(String.format(
                        "{\"error\": \"Currency with code = %s is already exists\"}",
                        newCurrency.getCode()));
            }
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"An error occurred while processing the request\"}");
            e.printStackTrace();
        }
    }

    private boolean exists(Currency currency) {
        return currencyDAO.findByCode(currency.getCode()) != null;
    }

    private boolean invalidValue(Currency currency) {
        assert currency.getCode() != null;
        assert currency.getFullName() != null;
        assert currency.getSign() != null;

        return false;
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        } else {
            int id = Integer.parseInt(pathInfo.substring(1));
            resp.setContentType("application/json");
            Currency updCurrency = ObjectMapperUtil.getInstance().readValue(req.getInputStream(), Currency.class);
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
