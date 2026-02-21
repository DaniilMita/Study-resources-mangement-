package org.example.dao;

import org.example.db.Db;
import org.example.model.StudyPlan;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class PlanDao {

    /**
     * returneaza lista de planuri pentru un utilizator ordonate descrescator,
     * dupa data crearii.
     * @param userId id-ul utilizatorului.
     * @return lista de planuri ale utilizatorului
     * @throws RuntimeException daca apare o eroare sql la interogare.
     */
    public List<StudyPlan> list(long userId) {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "select id, user_id, plan_type, content, created_at from study_plans where user_id = ? order by created_at desc, id desc")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<StudyPlan> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Listare planuri esuata: " + e.getMessage(), e);
        }
    }

    /**
     * insereaza un nou plan in tabela plans.
     * @param userId id-ul utilizatorului.
     * @param planType tipul planului.
     * @param content continutul planului.
     * @return id-ul generat pentru planul inserat.
     **/
    public long insert(long userId, String planType, String content) {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "insert into study_plans(user_id, plan_type, content) values(?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS
             )) {
            ps.setLong(1, userId);
            ps.setString(2, planType);
            ps.setString(3, content);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Inserare plan esuata: " + e.getMessage(), e);
        }
    }

    /**
     * sterge un plan din tabela plans.
     * @param planId id-ul planului.
     * @param userId id-ul utilizatorului.
     * @throws RuntimeException daca apare o eroare sql la stergere.
     */
    public void delete(long planId, long userId) {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("delete from study_plans where id = ? and user_id = ?")) {
            ps.setLong(1, planId);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Stergere plan esuata: " + e.getMessage(), e);
        }
    }

    public void update(long planId, long userId, String newType, String newContent) {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE study_plans SET plan_type = ?, content = ? WHERE id = ? AND user_id = ?")) {
            ps.setString(1, newType);
            ps.setString(2, newContent);
            ps.setLong(3, planId);
            ps.setLong(4, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Editarea a esuat: " + e.getMessage(), e);
        }
    }


    /**
     * map-eaza randul curent din resultset intr-un obiect plan.
     *
     * @param rs resultset pozitionat pe un rand valid.
     * @return obiect plan construit din coloanele randului curent.
     * @throws SQLException daca citirea coloanelor esueaza.
     */
    private StudyPlan map(ResultSet rs) throws SQLException {
        return new StudyPlan(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("plan_type"),
                rs.getString("content"),
                rs.getString("created_at")
        );
    }

}
