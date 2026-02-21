package org.example.dao;

import org.example.db.Db;
import org.example.model.User;
import java.sql.*;

public final class UserDao {

    /**
     * cauta un utilizator dupa email.
     * @param email emailul cautat.
     * @return user daca exista altfel null.
     * @throws RuntimeException daca apare o eroare sql la interogare.
     */
    public User findByEmail(String email) {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "select id, email, password_hash, created_at from users where email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new User(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("created_at")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cautare email esuata: " + e.getMessage(), e);
        }
    }

    /**
     * cauta un utilizator dupa id.
     * @param id id-ul utilizatorului.
     * @return user daca exista altfel null.
     * @throws RuntimeException daca apare o eroare sql la interogare.
     */
    public User findById(long id) {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "select id, email, password_hash, created_at from users where id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new User(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("created_at")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cautare dupa id esuata: " + e.getMessage(), e);
        }
    }

    /**
     * insereaza un utilizator nou in tabela users si returneaza id-ul generat.
     * @param email emailul utilizatorului.
     * @param passwordHash hash-ul parolei (nu parola in clar).
     * @return id-ul generat pentru utilizatorul inserat.
     * @throws RuntimeException daca apare o eroare sql la inserare.
     */
    public long insert(String email, String passwordHash) {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "insert into users(email, password_hash) values(?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, email);
            ps.setString(2, passwordHash);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Creare utilizator esuata, nu s-a obtinut id.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Inserare utilizator esuata: " + e.getMessage(), e);
        }
    }
}
