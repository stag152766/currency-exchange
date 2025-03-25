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

    public Collection<Currency> getAllCurrencies() throws SQLException {
        Collection<Currency> res = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection()) {
            ResultSet rs = conn.prepareStatement("select * from currencies")
                    .executeQuery();
            while (rs.next()) {
                res.add(new Currency(rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("fullName"),
                        rs.getString("sign")
                ));
            }
            return res;
        }
    }

    public boolean addCurrency(Currency currency) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String query = "insert into currencies (code, fullName, sign) values (?, ?, ?)";
            PreparedStatement prepStmt = conn.prepareStatement(query);
            prepStmt.setString(1, currency.getCode());
            prepStmt.setString(2, currency.getFullName());
            prepStmt.setString(3, currency.getSign());
            int rowAffected = prepStmt.executeUpdate();
            return rowAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Fail to create currency. ", e);
        }
    }

    public boolean deleteById(int id) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("delete from currencies where id = ?");
            ps.setInt(1, id);
            int rowAffected = ps.executeUpdate();
            return rowAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Fail to delete currency");
        }
    }

    public boolean updateById(int id, Currency currency) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String query = "update currencies set code=?, fullName=?, sign=? where id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, currency.getCode());
            ps.setString(2, currency.getFullName());
            ps.setString(3, currency.getSign());
            ps.setInt(4, id);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Fail to update currency");
        }
    }

    /**
     * Find currency by code in uppercase
     * @param code currency worldwide code https://www.iban.com/currency-codes
     *
     * @return currency or null
     */
    public Currency findByCode(String code) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String query = "select * from currencies where code = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, code.toUpperCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Currency(rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("fullName"),
                        rs.getString("sign")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Currency not found error");
        }
        return null;
    }
}
