package com.example.employeesearch;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Builds a throwaway SQLite database using the exact DDL of the legacy app
 * (Database/DatabaseHelper.cs:18-34) and seeds it. Using real SQLite (not H2) gives
 * faithful parity and proves the JPA mapping validates against the real schema (C1).
 */
public final class TestSqliteDb {

    private TestSqliteDb() {
    }

    public static Path createSeeded() throws Exception {
        Path file = Files.createTempFile("employeesearch-test-", ".db");
        Files.deleteIfExists(file); // let SQLite create it fresh
        String url = "jdbc:sqlite:" + file.toAbsolutePath();

        String adminHash = new BCryptPasswordEncoder().encode("admin123");

        try (Connection conn = DriverManager.getConnection(url);
             Statement st = conn.createStatement()) {
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS Users (
                        Id INTEGER PRIMARY KEY AUTOINCREMENT,
                        Username TEXT NOT NULL UNIQUE,
                        PasswordHash TEXT NOT NULL
                    )""");
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS Employees (
                        Id INTEGER PRIMARY KEY AUTOINCREMENT,
                        Name TEXT NOT NULL,
                        Department TEXT NOT NULL,
                        Role TEXT NOT NULL,
                        Status TEXT NOT NULL,
                        Email TEXT,
                        Phone TEXT,
                        HireDate TEXT,
                        Salary REAL
                    )""");
            st.executeUpdate("INSERT INTO Users (Username, PasswordHash) VALUES ('admin', '"
                    + adminHash + "')");

            String[][] rows = {
                    {"Alice Johnson", "IT", "Developer", "Active", "alice@corp.com", "555-0101", "2021-03-15", "85000"},
                    {"Bob Smith", "HR", "Manager", "Active", "bob@corp.com", "555-0102", "2019-07-01", "92000"},
                    {"Carol White", "Finance", "Analyst", "Active", "carol@corp.com", "555-0103", "2020-11-20", "78000"},
                    {"David Brown", "Marketing", "Coordinator", "On Leave", "david@corp.com", "555-0104", "2022-01-10", "65000"},
                    {"Eva Martinez", "IT", "Developer", "Active", "eva@corp.com", "555-0105", "2021-08-05", "88000"},
                    {"Frank Lee", "Operations", "Manager", "Active", "frank@corp.com", "555-0106", "2018-05-22", "95000"},
                    {"Grace Kim", "HR", "Analyst", "Inactive", "grace@corp.com", "555-0107", "2020-04-17", "72000"},
                    {"Henry Davis", "Finance", "Manager", "Active", "henry@corp.com", "555-0108", "2017-09-30", "105000"},
                    {"Irene Wilson", "IT", "Designer", "Active", "irene@corp.com", "555-0109", "2023-02-14", "80000"},
                    {"Jack Taylor", "Marketing", "Manager", "Active", "jack@corp.com", "555-0110", "2019-12-01", "98000"},
                    {"Karen Anderson", "Operations", "Coordinator", "Active", "karen@corp.com", "555-0111", "2022-06-28", "62000"},
                    {"Liam Thompson", "IT", "Analyst", "On Leave", "liam@corp.com", "555-0112", "2021-05-19", "82000"},
                    {"Maya Patel", "Finance", "Coordinator", "Active", "maya@corp.com", "555-0113", "2023-08-07", "60000"},
                    {"Noah Garcia", "Marketing", "Designer", "Active", "noah@corp.com", "555-0114", "2022-10-03", "74000"},
                    {"Olivia Chen", "Operations", "Analyst", "Inactive", "olivia@corp.com", "555-0115", "2020-07-25", "70000"},
            };
            for (String[] r : rows) {
                st.executeUpdate(String.format(
                        "INSERT INTO Employees (Name, Department, Role, Status, Email, Phone, HireDate, Salary) "
                                + "VALUES ('%s','%s','%s','%s','%s','%s','%s',%s)",
                        r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7]));
            }
        }
        file.toFile().deleteOnExit();
        return file;
    }
}
