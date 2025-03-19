package org.currency.exchange.dao;

import org.currency.exchange.model.Currency;
import org.currency.exchange.model.ExchangeRate;
import org.currency.exchange.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExchangeRateDAO {

    public List<ExchangeRate> getAllExchangeRates() {
        List<ExchangeRate> rates = new ArrayList<>();
        try (Statement stmt = DatabaseUtil.getConnection().createStatement()) {
            String query = "SELECT er.id, er.rate, bc.id AS base_id, bc.fullname AS base_name, bc.code AS base_code, " +
                    "bc.sign AS base_sign, tc.id AS target_id, tc.fullname AS target_name, " +
                    "tc.code AS target_code, tc.sign AS target_sign FROM exchangerates er " +
                    "JOIN currencies bc ON er.base_currency_id = bc.id " +
                    "JOIN currencies tc ON er.target_currency_id = tc.id;";
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Currency baseCurrency = new Currency(
                        rs.getInt("base_id"),
                        rs.getString("base_name"),
                        rs.getString("base_code"),
                        rs.getString("base_sign")
                );

                Currency targetCurrency = new Currency(
                        rs.getInt("target_id"),
                        rs.getString("target_name"),
                        rs.getString("target_code"),
                        rs.getString("target_sign")
                );

                ExchangeRate exchangeRate = new ExchangeRate(
                        rs.getInt("id"),
                        baseCurrency,
                        targetCurrency,
                        rs.getDouble("rate")
                );

                rates.add(exchangeRate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rates;
    }

    public ExchangeRate getExchangeRateByCodes(String codes) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String baseCode = codes.substring(1, 4);
            String targCode = codes.substring(4);
            String query = "select * from exchangeRates " +
                    "where base_currency_id = (select id from currencies where code = ?) " +
                    "and target_currency_id = (select id from currencies where code = ?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, baseCode);
            ps.setString(2, targCode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // TODO fix
                return new ExchangeRate(rs.getInt(1),
                        rs.getObject(2, Currency.class),
                        rs.getObject(3, Currency.class),
                        rs.getDouble(4));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
