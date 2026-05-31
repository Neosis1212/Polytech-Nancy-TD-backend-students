package com.example.todoapp.dao;

import com.example.todoapp.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskDao {
    private static final Logger log = LoggerFactory.getLogger(TaskDao.class);
    // On utiliise  SQLite 
    private static final String DB_URL = "jdbc:sqlite:todoapp.db";

    public TaskDao() {
        // Initialisation : création de la table si elle n'existe pas
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS tasks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT NOT NULL," +
                    "description TEXT," +
                    "done BOOLEAN NOT NULL" +
                    ")";
            stmt.execute(sql);
            log.info("Base de données SQLite connectée et table 'tasks' vérifiée/créée.");

        } catch (SQLException e) {
            log.error("Erreur lors de l'initialisation de la BDD", e);
        }
    }

    /**
     * Récupère toutes les tâches de la base de données.
     */
    public List<Task> findAll() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tasks.add(new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getBoolean("done")
                ));
            }
        } catch (SQLException e) {
            log.error("Erreur lors de la récupération de toutes les tâches", e);
        }
        return tasks;
    }

    /**
     * Récupère une tâche spécifique par son ID.
     */
    public Optional<Task> findById(int id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Task(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getBoolean("done")
                    ));
                }
            }
        } catch (SQLException e) {
            log.error("Erreur lors de la récupération de la tâche avec l'ID: " + id, e);
        }
        return Optional.empty();
    }

    /**
     * Sauvegarde une tâche (Insert si id = 0, sinon Update).
     */
    public Task save(Task task) {
        // Si l'ID est 0 (ou null), on crée une nouvelle tâche
        if (task.getId() == 0) {
            String sql = "INSERT INTO tasks (title, description, done) VALUES (?, ?, ?)";

            // RETURN_GENERATED_KEYS permet de récupérer l'ID généré par SQLite
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setString(1, task.getTitle());
                pstmt.setString(2, task.getDescription());
                pstmt.setBoolean(3, task.isDone());
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        task.setId(generatedKeys.getInt(1)); 
                        log.debug("Tâche créée avec l'ID: " + task.getId());
                    }
                }
            } catch (SQLException e) {
                log.error("Erreur lors de l'insertion de la tâche", e);
            }
        } else {
            // Mise à jour d'une tâche existante
            String sql = "UPDATE tasks SET title = ?, description = ?, done = ? WHERE id = ?";

            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, task.getTitle());
                pstmt.setString(2, task.getDescription());
                pstmt.setBoolean(3, task.isDone());
                pstmt.setInt(4, task.getId());
                pstmt.executeUpdate();
                log.debug("Tâche mise à jour pour l'ID: " + task.getId());

            } catch (SQLException e) {
                log.error("Erreur lors de la mise à jour de la tâche avec l'ID: " + task.getId(), e);
            }
        }
        return task;
    }

    /**
     * Supprime une tâche par son ID.
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                log.debug("Tâche supprimée avec succès (ID: " + id + ")");
                return true;
            }
        } catch (SQLException e) {
            log.error("Erreur lors de la suppression de la tâche avec l'ID: " + id, e);
        }
        return false;
    }
}