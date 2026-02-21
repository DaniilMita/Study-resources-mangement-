package org.example.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

public final class Db {
    private static String jdbcUrl;
    private Db() {}

    /**
     * initializeaza baza de date:
     * seteaza jdbcurl deschide o conexiune activeaza foreign keys
     * apoi ruleaza migrarile (creare/actualizare tabele).
     * dupa care adauga un user default daca nu exista
     */
    public static void init() {
        jdbcUrl = "jdbc:sqlite:" + resolveDbPath().toAbsolutePath();
        try (Connection c = getConnection()) {
            try (Statement st = c.createStatement()) {
                st.execute("pragma foreign_keys = on");
            }
            Migrations.apply(c);
            SeedData.apply(c);
        } catch (SQLException e) {
            throw new RuntimeException("Initializare baza de date esuata: " + e.getMessage(), e);
        }
    }

    /**
     * creeaza si returneaza o conexiune noua catre baza de date sqlite.
     * activeaza foreign keys pentru conexiunea returnata.
     * @return o conexiune noua catre baza de date cu foreign keys activate.
     * @throws SQLException daca nu se poate deschide conexiunea.
     */
    public static Connection getConnection() throws SQLException {
        Connection c = DriverManager.getConnection(jdbcUrl);
        try (Statement st = c.createStatement()) {
            st.execute("pragma foreign_keys = on");
        }
        return c;
    }

    /**
     * rezolva calea fisierului db:
     * incearca in src/main/resources iar daca nu se poate
     * foloseste un fisier local in directorul curent.
     * @return calea catre fisierul bazei de date.
     */
    private static Path resolveDbPath() {
        Path preferred = Paths.get("src", "main", "resources", "baza de date.db");
        try {
            Files.createDirectories(preferred.getParent());
            return preferred;
        } catch (Exception ignored) {
            return Paths.get("baza de date.db");
        }
    }
}
