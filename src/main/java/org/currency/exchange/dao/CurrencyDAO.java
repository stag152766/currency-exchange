package org.currency.exchange.dao;

import org.currency.exchange.model.Currency;
import org.currency.exchange.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class CurrencyDAO {

    public Collection<Currency> getAllCurrencies() {
        Collection<Currency> res = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection()) {
            ResultSet rs = conn.prepareStatement("select * from currencies")
                    .executeQuery();
            while (rs.next()) {
                res.add(new Currency(rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("full"),
                        rs.getString("sign")
                ));
            }
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    public boolean addCurrency(Currency currency) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            PreparedStatement prepStmt = conn.prepareStatement("insert into currencies (code, full, sign) " +
                    "values (?, ?, ?)");
            prepStmt.setString(1, currency.getCode());
            prepStmt.setString(2, currency.getFull());
            prepStmt.setString(3, currency.getSign());
            int rowAffected = prepStmt.executeUpdate();
            return rowAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Fail to create currency. ", e);
        }
    }
}
