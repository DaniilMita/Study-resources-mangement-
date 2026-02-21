package org.example.dao;

import org.example.db.Db;
import org.example.model.ChatMessage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @file chatdao.java
 * @brief acces la tabela chat_history (listare mesaje inserare stergere).
 *
 * permite citirea istoricului de chat pentru un utilizator salvarea unui mesaj nou
 * si stergerea intregului istoric pentru utilizator.
 */
public final class ChatDao {

    /**
     * returneaza lista de mesaje din istoricul conversatiei pentru un utilizator limitata la un numar maxim de randuri.
     * mesajele sunt ordonate crescator dupa id (de la cele mai vechi la cele mai noi).
     *
     * @param userId id-ul utilizatorului.
     * @param limit  numarul maxim de mesaje returnate.
     * @return lista de mesaje
     * @throws RuntimeException daca apare o eroare sql la interogare.
     */
    public List<ChatMessage> list(long userId, int limit) {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "select id, user_id, role, message, timestamp from chat_history where user_id = ? order by id asc limit ?"
             )) {
            ps.setLong(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<ChatMessage> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Listare chat esuata: " + e.getMessage(), e);
        }
    }

    /**
     * insereaza un mesaj nou in istoricul de chat si returneaza id-ul generat.
     *
     * @param userId  id-ul utilizatorului caruia ii apartine mesajul.
     * @param role    rolul autorului mesajului (ex. user, assistant, system).
     * @param message continutul mesajului.
     * @return id-ul generat pentru mesajul inserat.
     * @throws RuntimeException daca apare o eroare sql la inserare.
     */
    public long insert(long userId, String role, String message) {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "insert into chat_history(user_id, role, message) values(?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS
             )) {
            ps.setLong(1, userId);
            ps.setString(2, role);
            ps.setString(3, message);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Inserare chat esuata: " + e.getMessage(), e);
        }
    }

    /**
     * sterge toate mesajele din istoricul de chat pentru un utilizator.
     *
     * @param userId id-ul utilizatorului.
     * @throws RuntimeException daca apare o eroare sql la stergere.
     */
    public void clear(long userId) {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("delete from chat_history where user_id = ?")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Stergere chat esuata: " + e.getMessage(), e);
        }
    }

    /**
     * map-eaza randul curent din ResultSet intr-un obiect ChatMessage.
     *
     * @param rs ResultSet pozitionat pe un rand valid.
     * @return obiect ChatMessage construit din coloanele randului curent.
     * @throws SQLException daca citirea coloanelor esueaza.
     */
    private ChatMessage map(ResultSet rs) throws SQLException {
        return new ChatMessage(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("role"),
                rs.getString("message"),
                rs.getString("timestamp")
        );
    }
}
