package org.currency.exchange.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtils {

    private final static String URL = "jdbc:sqlite::memory:";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS currencies (" +
                            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "    code TEXT NOT NULL UNIQUE," +
                            "    full name TEXT NOT NULL," +
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


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertInitialData(Connection conn) {
        try (Statement smnt = getConnection().createStatement();) {
            smnt.execute("DELETE from currencies");

            smnt.execute(
                    "INSERT INTO currencies (code, name, sign) VALUES ('USD', 'US Dollar', '$'), \n" +
                    "                ('EUR', 'Euro', '€'), " +
                    "                ('RUB', 'Russian Ruble', '₽'), " +
                    "           ('GBP', 'British Pound', '£');"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}