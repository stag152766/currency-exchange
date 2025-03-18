package org.currency.exchange.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles database connections and initialization
 */
public class DatabaseUtil {
    // jdbc:sqlite:/Users/<user>/.SmartTomcat/currency-exchange/currency-exchange/exchange1.db
    private final static String URL = "jdbc:sqlite:exchange1.db";

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
            smtm.execute("drop table currencies");

            smtm.execute(
                    "CREATE TABLE IF NOT EXISTS currencies (" +
                            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "    code TEXT NOT NULL UNIQUE," +
                            "    fullName name TEXT NOT NULL," +
                            "    sign TEXT NOT NULL" +
                            ");"
            );

            smtm.execute(
                    "CREATE TABLE IF NOT EXISTS exchangeRates (" +
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
            stmt.execute("delete from currencies");

            stmt.execute("INSERT INTO currencies (code, fullName, sign) VALUES " +
                    "('USD', 'US Dollar', '$')," +
                    "('EUR', 'Euro', '€'), " +
                    "('RUR', 'Russian Ruble', '₽'), " +
                    "('GBP', 'British Pound', '£');"
            );

            stmt.execute("delete from exchangeRates");

            stmt.execute("INSERT INTO exchangeRates (base_currency_id, target_currency_id, rate) " +
                    "VALUES  " +
                    "((SELECT ID FROM Currencies WHERE Code = 'USD'), " +
                    "( SELECT ID FROM Currencies WHERE Code = 'EUR'), 0.92) , " +
                    "((SELECT ID FROM Currencies WHERE Code = 'USD'), " +
                    "( SELECT ID FROM Currencies WHERE Code = 'RUR'), 81.66), " +
                    "(( SELECT ID FROM Currencies WHERE Code = 'EUR'), " +
                    "( SELECT ID FROM Currencies WHERE Code = 'RUR'), 89.14)"
            );

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}