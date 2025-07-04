package org.currency.exchange.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles database connections and initialization
 */
public class DatabaseUtil {
    private final static String URL = "jdbc:sqlite:/Users/stag/Documents/DB/exchange1.db";

    // register db to avoid an error
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        try (Statement smtm = getConnection().createStatement()) {
            smtm.execute("drop table if exists currencies");

            smtm.execute(
                    "CREATE TABLE currencies (" +
                            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "    code TEXT NOT NULL UNIQUE," +
                            "    fullName name TEXT NOT NULL," +
                            "    sign TEXT NOT NULL" +
                            ");"
            );

            smtm.execute("drop table if exists exchangeRates");

            smtm.execute(
                    "CREATE TABLE exchangeRates (" +
                            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "    base_currency_id INTEGER NOT NULL," +
                            "    target_currency_id INTEGER NOT NULL," +
                            "    rate DECIMAL(6) NOT NULL," +
                            "    FOREIGN KEY (base_currency_id) REFERENCES currencies(id)," +
                            "    FOREIGN KEY (target_currency_id) REFERENCES currencies(id)," +
                            "    UNIQUE (base_currency_id, target_currency_id)" +
                            ");"
            );

            insertInitialData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertInitialData() {
        try (Statement stmt = getConnection().createStatement()) {

            stmt.execute("INSERT INTO currencies (code, fullName, sign) VALUES " +
                    "('USD', 'US Dollar', '$')," +
                    "('EUR', 'Euro', '€'), " +
                    "('RUR', 'Russian Ruble', '₽'), " +
                    "('GBP', 'British Pound', '£');"
            );
            stmt.execute("INSERT INTO exchangeRates (base_currency_id, target_currency_id, rate) " +
                    "VALUES  " +
                    "((SELECT id FROM currencies WHERE code = 'USD'), " +
                    "( SELECT id FROM currencies WHERE code = 'EUR'), 0.92) , " +
                    "((SELECT id FROM currencies WHERE code = 'USD'), " +
                    "( SELECT id FROM currencies WHERE code = 'RUR'), 81.66), " +
                    "(( SELECT id FROM currencies WHERE code = 'EUR'), " +
                    "( SELECT id FROM currencies WHERE code = 'RUR'), 89.14)"
            );

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
