package org.currency.exchange.controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Обмен валюты
 */
@WebServlet(urlPatterns = { "/exchange/*" })
public class ExchangeServlet extends HttpServlet {

     /**
      * Расчёт перевода определённого количества средств из одной валюты в другую
      */
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         resp.setContentType("text/plain");
         resp.getWriter().write("Currency exchange endpoint");
     }

}
