package org.currency.exchange.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/currencies/*")
public class CurrencyServlet extends HttpServlet {
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

    private static void getSpecificCurrency(HttpServletResponse resp) throws IOException {
        resp.getWriter().println("GET request /currencies/{id} handle");
    }

    private static void getAllCurrencies(HttpServletResponse resp) throws IOException {
        //resp.getWriter().println("GET request /currency/ handle");
        String query = "select * from currencies";
    }

    /**
     * добавить новую валюту
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }
}
