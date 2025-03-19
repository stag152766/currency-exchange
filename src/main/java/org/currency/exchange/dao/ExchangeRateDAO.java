package org.currency.exchange.dao;

import org.currency.exchange.model.ExchangeRate;
import org.currency.exchange.util.DatabaseUtil;

import java.sql.*;
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

    public ExchangeRate getExchangeRateByCode(String codes) {
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
                return new ExchangeRate(rs.getInt(1),
                        rs.getInt(2),
                        rs.getInt(3),
                        rs.getDouble(4));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
