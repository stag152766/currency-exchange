package org.currency.exchange.dao;

import org.currency.exchange.model.Currency;
import org.currency.exchange.util.DatabaseUtil;

import java.sql.Connection;
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
}
