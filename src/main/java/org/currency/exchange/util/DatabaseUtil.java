package org.currency.exchange.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles database connections and initialization
 */
public class DatabaseUtil {
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
        try (Connection conn = getConnection()) {
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS currencies (" +
                            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "    code TEXT NOT NULL UNIQUE," +
                            "    fullName name TEXT NOT NULL," +
                            "    sign TEXT NOT NULL" +
                            ");"
            );
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS exchange_rates (" +
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
        try (Statement stmt = getConnection().createStatement();) {
            stmt.execute("DELETE from currencies");

            stmt.execute(
                    "INSERT INTO currencies (code, fullName, sign) VALUES " +
                            "('USD', 'US Dollar', '$')," +
                    "                ('EUR', 'Euro', '€'), " +
                    "                ('RUB', 'Russian Ruble', '₽'), " +
                    "           ('GBP', 'British Pound', '£');"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}