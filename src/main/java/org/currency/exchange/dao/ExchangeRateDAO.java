package org.currency.exchange.dao;

import org.currency.exchange.dto.ExchangeRateDto;
import org.currency.exchange.model.Currency;
import org.currency.exchange.model.ExchangeRate;
import org.currency.exchange.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExchangeRateDAO {
    private final CurrencyDAO currencyDAO;

    public ExchangeRateDAO(CurrencyDAO currencyDAO) {
        this.currencyDAO = currencyDAO;
    }

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
                Currency baseCurrency = getBaseCurrency(rs);
                Currency targetCurrency = getTargetCurrency(rs);

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
            String query = "SELECT er.id, er.rate, bc.id AS base_id, bc.fullname AS base_name, bc.code AS base_code, " +
                    "bc.sign AS base_sign, tc.id AS target_id, tc.fullname AS target_name, " +
                    "tc.code AS target_code, tc.sign AS target_sign FROM exchangerates er " +
                    "JOIN currencies bc ON er.base_currency_id = bc.id " +
                    "JOIN currencies tc ON er.target_currency_id = tc.id " +
                    "where base_currency_id = (select id from currencies where code = ?) " +
                    "and target_currency_id = (select id from currencies where code = ?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, baseCode);
            ps.setString(2, targCode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new ExchangeRate(
                        rs.getInt("id"),
                        getBaseCurrency(rs),
                        getTargetCurrency(rs),
                        rs.getDouble("rate"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Currency getTargetCurrency(ResultSet rs) throws SQLException {
        return new Currency(
                rs.getInt("target_id"),
                rs.getString("target_name"),
                rs.getString("target_code"),
                rs.getString("target_sign")
        );
    }

    private static Currency getBaseCurrency(ResultSet rs) throws SQLException {
        return new Currency(
                rs.getInt("base_id"),
                rs.getString("base_name"),
                rs.getString("base_code"),
                rs.getString("base_sign"));
    }

    public int createExchangeRate(ExchangeRateDto params) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String baseCode = params.getBaseCurrencyCode();
            Currency baseCurr = currencyDAO.findByCode(baseCode);
            String targetCode = params.getTargetCurrencyCode();
            Currency targetCurr = currencyDAO.findByCode(targetCode);

            if (baseCurr == null || targetCurr == null) {
                return -1;
            }

            String query = "insert into exchangeRates (base_currency_id, target_currency_id, rate) values \n" +
                    "(?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setLong(1, baseCurr.getId());
            ps.setLong(2, targetCurr.getId());
            ps.setDouble(3, params.getRate());
            return ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
