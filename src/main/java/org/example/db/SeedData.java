package org.example.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.example.util.Crypto;

public final class SeedData {

    private SeedData() {}

    /**
     * insereaza date initiale doar daca tabela users este goala
     */
    public static void apply(Connection c) throws SQLException {
        if (hasAnyUser(c)) return;

        String email = "student@test.com";
        String passHash = Crypto.sha256("test123");

        long userId;
        try (Statement st = c.createStatement()) {
            st.executeUpdate(
                    "INSERT INTO users(email, password_hash) VALUES('" + email + "', '" + passHash + "')"
            );

            try (ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
                rs.next();
                userId = rs.getLong(1);
            }

            st.executeUpdate(
                    "INSERT INTO learning_profiles(user_id, main_subject, level, goal, daily_minutes) " +
                            "VALUES(" + userId + ", 'Mathematics', 'High School', 'Exam Prep', 60)"
            );

            st.executeUpdate(
                    "INSERT INTO study_plans(user_id, plan_type, content) " +
                            "VALUES(" + userId + ", '7-day plan', 'Day 1: Algebra, Day 2: Geometry, Day 3: Calculus')"
            );

            st.executeUpdate(
                    "INSERT INTO study_sessions(user_id, date, minutes_studied, topic, difficulty, notes) " +
                            "VALUES(" + userId + ", date('now'), 45, 'Algebra', 3, 'Focused on equations')"
            );
        }
    }

    /**
     * verifica daca exista cel putin un utilizator
     */
    private static boolean hasAnyUser(Connection c) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT count(*) FROM users")) {
            rs.next();
            return rs.getInt(1) > 0;
        }
    }
}

