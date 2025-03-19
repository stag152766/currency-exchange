package org.currency.exchange.dao;

import org.currency.exchange.model.ExchangeRate;
import org.currency.exchange.util.DatabaseUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ExchangeRateDAO {

    public List<ExchangeRate> getAllExchangeRates() {
        List<ExchangeRate> rates = new ArrayList<>();
        try (Statement stmt = DatabaseUtil.getConnection().createStatement()) {
            String query = "select * from exchangeRates";
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                rates.add(new ExchangeRate(rs.getInt(1),
                        rs.getInt(2),
                        rs.getInt(3),
                        rs.getDouble(4)
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rates;
    }
}
