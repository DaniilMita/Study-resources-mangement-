package org.example.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class Migrations {

    private Migrations() {}

    public static void apply(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {

            st.execute(
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "email TEXT NOT NULL UNIQUE," +
                            "password_hash TEXT NOT NULL," +
                            "created_at TEXT DEFAULT (datetime('now'))" +
                            ");"
            );

            st.execute(
                    "CREATE TABLE IF NOT EXISTS learning_profiles (" +
                            "user_id INTEGER PRIMARY KEY," +
                            "main_subject TEXT," +
                            "level TEXT," +
                            "goal TEXT," +
                            "daily_minutes INTEGER," +
                            "updated_at TEXT DEFAULT (datetime('now'))," +
                            "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE" +
                            ");"
            );

            st.execute(
                    "CREATE TABLE IF NOT EXISTS study_plans (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "user_id INTEGER NOT NULL," +
                            "plan_type TEXT," +
                            "content TEXT," +
                            "created_at TEXT DEFAULT (datetime('now'))," +
                            "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE" +
                            ");"
            );
            st.execute("CREATE INDEX IF NOT EXISTS idx_study_plans_user ON study_plans(user_id);");

            st.execute(
                    "CREATE TABLE IF NOT EXISTS study_sessions (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "user_id INTEGER NOT NULL," +
                            "date TEXT," +
                            "minutes_studied INTEGER," +
                            "topic TEXT," +
                            "difficulty INTEGER," +
                            "notes TEXT," +
                            "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE" +
                            ");"
            );
            st.execute("CREATE INDEX IF NOT EXISTS idx_study_sessions_user ON study_sessions(user_id);");
        }
    }
}

