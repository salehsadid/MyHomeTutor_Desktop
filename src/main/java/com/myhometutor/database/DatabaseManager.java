package com.myhometutor.database;

import org.json.JSONObject;
import java.sql.*;

public class DatabaseManager {
    
    private static final String DB_URL = "jdbc:sqlite:myhometutor.db";
    private static DatabaseManager instance;
    private Connection connection;
    
    private DatabaseManager() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            initializeTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    private void initializeTables() {
        try {
            Statement stmt = connection.createStatement();
            
            // Students table with JSON data column
            String createStudentsTable = """
                CREATE TABLE IF NOT EXISTS students (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    data TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
            
            // Tutors table with JSON data column
            String createTutorsTable = """
                CREATE TABLE IF NOT EXISTS tutors (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    data TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
            
            // Tuition posts table
            String createTuitionPostsTable = """
                CREATE TABLE IF NOT EXISTS tuition_posts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id INTEGER NOT NULL,
                    data TEXT NOT NULL,
                    status TEXT DEFAULT 'active',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (student_id) REFERENCES students(id)
                )
            """;
            
            // Applications table
            String createApplicationsTable = """
                CREATE TABLE IF NOT EXISTS applications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    tuition_post_id INTEGER NOT NULL,
                    tutor_id INTEGER NOT NULL,
                    status TEXT DEFAULT 'pending',
                    is_profile_revealed INTEGER DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (tuition_post_id) REFERENCES tuition_posts(id),
                    FOREIGN KEY (tutor_id) REFERENCES tutors(id)
                )
            """;

            // Notifications table
            String createNotificationsTable = """
                CREATE TABLE IF NOT EXISTS notifications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    user_type TEXT NOT NULL,
                    message TEXT NOT NULL,
                    is_read INTEGER DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
            
            stmt.execute(createStudentsTable);
            stmt.execute(createTutorsTable);
            stmt.execute(createTuitionPostsTable);
            stmt.execute(createApplicationsTable);
            stmt.execute(createNotificationsTable);
            
            // Attempt to add is_profile_revealed column if it doesn't exist (for existing DBs)
            try {
                stmt.execute("ALTER TABLE applications ADD COLUMN is_profile_revealed INTEGER DEFAULT 0");
            } catch (SQLException e) {
                // Column likely already exists, ignore
            }

            stmt.close();
            System.out.println("Database tables initialized successfully.");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Register a new student
    public boolean registerStudent(String username, String password, JSONObject data) {
        String sql = "INSERT INTO students (username, password, data) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password); // In production, hash the password!
            pstmt.setString(3, data.toString());
            
            pstmt.executeUpdate();
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Register a new tutor
    public boolean registerTutor(String username, String password, JSONObject data) {
        String sql = "INSERT INTO tutors (username, password, data) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password); // In production, hash the password!
            pstmt.setString(3, data.toString());
            
            pstmt.executeUpdate();
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Authenticate student
    public JSONObject authenticateStudent(String username, String password) {
        String sql = "SELECT id, data FROM students WHERE username = ? AND password = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                JSONObject data = new JSONObject(rs.getString("data"));
                data.put("id", rs.getInt("id"));
                data.put("username", username);
                return data;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Authenticate tutor
    public JSONObject authenticateTutor(String username, String password) {
        String sql = "SELECT id, data FROM tutors WHERE username = ? AND password = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                JSONObject data = new JSONObject(rs.getString("data"));
                data.put("id", rs.getInt("id"));
                data.put("username", username);
                return data;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Get student by ID
    public JSONObject getStudentById(int id) {
        String sql = "SELECT username, data FROM students WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                JSONObject data = new JSONObject(rs.getString("data"));
                data.put("id", id);
                data.put("username", rs.getString("username"));
                return data;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Get tutor by ID
    public JSONObject getTutorById(int id) {
        String sql = "SELECT username, data FROM tutors WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                JSONObject data = new JSONObject(rs.getString("data"));
                data.put("id", id);
                data.put("username", rs.getString("username"));
                return data;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Update student profile
    public boolean updateStudent(int id, JSONObject data) {
        String sql = "UPDATE students SET data = ?, username = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, data.toString());
            pstmt.setString(2, data.getString("email"));
            pstmt.setInt(3, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Update tutor profile
    public boolean updateTutor(int id, JSONObject data) {
        String sql = "UPDATE tutors SET data = ?, username = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, data.toString());
            pstmt.setString(2, data.getString("email"));
            pstmt.setInt(3, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Check if username exists
    public boolean usernameExists(String username, String userType) {
        String table = userType.equals("Student") ? "students" : "tutors";
        String sql = "SELECT COUNT(*) FROM " + table + " WHERE username = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Create a new tuition post
    public boolean createTuitionPost(int studentId, JSONObject postData) {
        String sql = "INSERT INTO tuition_posts (student_id, data) VALUES (?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setString(2, postData.toString());
            
            pstmt.executeUpdate();
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Get all active tuition posts
    public org.json.JSONArray getAllTuitionPosts() {
        org.json.JSONArray posts = new org.json.JSONArray();
        String sql = "SELECT tp.id, tp.student_id, tp.data, tp.status, tp.created_at, s.data as student_data " +
                     "FROM tuition_posts tp " +
                     "JOIN students s ON tp.student_id = s.id " +
                     "WHERE tp.status IN ('active', 'assigned') " +
                     "ORDER BY tp.created_at DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                JSONObject post = new JSONObject(rs.getString("data"));
                post.put("id", rs.getInt("id"));
                post.put("studentId", rs.getInt("student_id"));
                post.put("status", rs.getString("status"));
                post.put("createdAt", rs.getString("created_at"));
                
                JSONObject studentData = new JSONObject(rs.getString("student_data"));
                post.put("studentName", studentData.optString("name", "Unknown"));
                post.put("studentPhone", studentData.optString("phone", ""));
                post.put("studentGender", studentData.optString("gender", "Unknown"));
                
                posts.put(post);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return posts;
    }
    
    // Update password with verification
    public boolean updatePassword(int userId, String userType, String currentPassword, String newPassword) {
        String table = userType.equals("Student") ? "students" : "tutors";
        
        // First verify current password
        String verifySql = "SELECT id FROM " + table + " WHERE id = ? AND password = ?";
        try (PreparedStatement verifyStmt = connection.prepareStatement(verifySql)) {
            verifyStmt.setInt(1, userId);
            verifyStmt.setString(2, currentPassword);
            
            ResultSet rs = verifyStmt.executeQuery();
            if (!rs.next()) {
                return false; // Password incorrect
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        // Update to new password
        String updateSql = "UPDATE " + table + " SET password = ? WHERE id = ?";
        try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
            updateStmt.setString(1, newPassword);
            updateStmt.setInt(2, userId);
            
            int rowsAffected = updateStmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Apply for tuition
    public boolean applyForTuition(int tutorId, int postId) {
        // Check if already applied
        String checkSql = "SELECT id FROM applications WHERE tutor_id = ? AND tuition_post_id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, tutorId);
            checkStmt.setInt(2, postId);
            if (checkStmt.executeQuery().next()) {
                return false; // Already applied
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        String sql = "INSERT INTO applications (tutor_id, tuition_post_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, tutorId);
            pstmt.setInt(2, postId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get applications for a tutor
    public org.json.JSONArray getTutorApplications(int tutorId) {
        org.json.JSONArray applications = new org.json.JSONArray();
        String sql = "SELECT a.id, a.status, a.created_at, a.is_profile_revealed, tp.data as post_data, s.data as student_data " +
                     "FROM applications a " +
                     "JOIN tuition_posts tp ON a.tuition_post_id = tp.id " +
                     "JOIN students s ON tp.student_id = s.id " +
                     "WHERE a.tutor_id = ? ORDER BY a.created_at DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, tutorId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                JSONObject app = new JSONObject();
                app.put("id", rs.getInt("id"));
                app.put("status", rs.getString("status"));
                app.put("appliedAt", rs.getString("created_at"));
                app.put("isProfileRevealed", rs.getInt("is_profile_revealed") == 1);
                
                JSONObject postData = new JSONObject(rs.getString("post_data"));
                JSONObject studentData = new JSONObject(rs.getString("student_data"));
                
                postData.put("studentName", studentData.optString("name", "Unknown"));
                postData.put("studentPhone", studentData.optString("phone", ""));
                app.put("post", postData);
                app.put("student", studentData);
                
                applications.put(app);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return applications;
    }

    public boolean cancelApplication(int applicationId) {
        String sql = "DELETE FROM applications WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, applicationId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get posts created by a student
    public org.json.JSONArray getStudentPosts(int studentId) {
        org.json.JSONArray posts = new org.json.JSONArray();
        String sql = "SELECT id, data, status, created_at FROM tuition_posts WHERE student_id = ? ORDER BY created_at DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                JSONObject post = new JSONObject(rs.getString("data"));
                post.put("id", rs.getInt("id"));
                post.put("status", rs.getString("status"));
                post.put("createdAt", rs.getString("created_at"));
                posts.put(post);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    // Get applications for a specific post
    public org.json.JSONArray getPostApplications(int postId) {
        org.json.JSONArray applications = new org.json.JSONArray();
        String sql = "SELECT a.id, a.status, a.created_at, a.is_profile_revealed, t.data as tutor_data, t.id as tutor_id " +
                     "FROM applications a " +
                     "JOIN tutors t ON a.tutor_id = t.id " +
                     "WHERE a.tuition_post_id = ? ORDER BY a.created_at DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                JSONObject app = new JSONObject();
                app.put("id", rs.getInt("id"));
                app.put("status", rs.getString("status"));
                app.put("appliedAt", rs.getString("created_at"));
                app.put("tutorId", rs.getInt("tutor_id"));
                app.put("isProfileRevealed", rs.getInt("is_profile_revealed") == 1);
                
                JSONObject tutorData = new JSONObject(rs.getString("tutor_data"));
                app.put("tutorName", tutorData.optString("name", "Unknown"));
                app.put("tutorPhone", tutorData.optString("phone", ""));
                app.put("tutorInstitution", tutorData.optString("universityName", ""));
                
                applications.put(app);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return applications;
    }

    // Accept an application
    public boolean acceptApplication(int applicationId) {
        String updateAppSql = "UPDATE applications SET status = 'accepted' WHERE id = ?";
        String updatePostSql = "UPDATE tuition_posts SET status = 'assigned' WHERE id = (SELECT tuition_post_id FROM applications WHERE id = ?)";
        
        try {
            connection.setAutoCommit(false);
            
            try (PreparedStatement pstmtApp = connection.prepareStatement(updateAppSql);
                 PreparedStatement pstmtPost = connection.prepareStatement(updatePostSql)) {
                
                pstmtApp.setInt(1, applicationId);
                pstmtApp.executeUpdate();
                
                pstmtPost.setInt(1, applicationId);
                pstmtPost.executeUpdate();
                
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
                return false;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete notification
    public boolean deleteNotification(int notificationId) {
        String sql = "DELETE FROM notifications WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, notificationId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Create notification
    public void createNotification(int userId, String userType, String message) {
        String sql = "INSERT INTO notifications (user_id, user_type, message) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, userType);
            pstmt.setString(3, message);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Reveal profile for an application
    public boolean revealProfile(int applicationId) {
        String sql = "UPDATE applications SET is_profile_revealed = 1 WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, applicationId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Check if profile is revealed
    public boolean isProfileRevealed(int applicationId) {
        String sql = "SELECT is_profile_revealed FROM applications WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, applicationId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("is_profile_revealed") == 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Get user notifications
    public org.json.JSONArray getUserNotifications(int userId, String userType) {
        org.json.JSONArray notifications = new org.json.JSONArray();
        String sql = "SELECT id, message, is_read, created_at FROM notifications " +
                     "WHERE user_id = ? AND user_type = ? ORDER BY created_at DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, userType);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                JSONObject notif = new JSONObject();
                notif.put("id", rs.getInt("id"));
                notif.put("message", rs.getString("message"));
                notif.put("isRead", rs.getInt("is_read") == 1);
                notif.put("createdAt", rs.getString("created_at"));
                notifications.put(notif);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    public JSONObject getTutorProfile(int tutorId) {
        String sql = "SELECT data FROM tutors WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, tutorId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new JSONObject(rs.getString("data"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getApplicationStatus(int tutorId, int postId) {
        String sql = "SELECT status FROM applications WHERE tutor_id = ? AND tuition_post_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, tutorId);
            pstmt.setInt(2, postId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("status");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteTuitionPost(int postId) {
        try {
            connection.setAutoCommit(false);
            
            // Delete applications for this post
            String deleteAppsSql = "DELETE FROM applications WHERE tuition_post_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteAppsSql)) {
                pstmt.setInt(1, postId);
                pstmt.executeUpdate();
            }
            
            // Delete the post
            String deletePostSql = "DELETE FROM tuition_posts WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deletePostSql)) {
                pstmt.setInt(1, postId);
                int rows = pstmt.executeUpdate();
                
                if (rows > 0) {
                    connection.commit();
                    return true;
                } else {
                    connection.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean deleteUser(String username, String userType) {
        try {
            connection.setAutoCommit(false); // Start transaction

            int userId = -1;
            String getIdSql = "SELECT id FROM " + (userType.equals("student") ? "students" : "tutors") + " WHERE username = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(getIdSql)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("id");
                }
            }

            if (userId == -1) {
                connection.rollback();
                return false;
            }

            // Delete related data
            if (userType.equals("student")) {
                // Delete applications for student's posts
                String deleteAppsSql = "DELETE FROM applications WHERE tuition_post_id IN (SELECT id FROM tuition_posts WHERE student_id = ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(deleteAppsSql)) {
                    pstmt.setInt(1, userId);
                    pstmt.executeUpdate();
                }
                
                // Delete tuition posts
                String deletePostsSql = "DELETE FROM tuition_posts WHERE student_id = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(deletePostsSql)) {
                    pstmt.setInt(1, userId);
                    pstmt.executeUpdate();
                }
            } else {
                // Delete applications by tutor
                String deleteAppsSql = "DELETE FROM applications WHERE tutor_id = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(deleteAppsSql)) {
                    pstmt.setInt(1, userId);
                    pstmt.executeUpdate();
                }
            }

            // Delete notifications
            String deleteNotifsSql = "DELETE FROM notifications WHERE user_id = ? AND user_type = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteNotifsSql)) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, userType);
                pstmt.executeUpdate();
            }

            // Delete user
            String deleteUserSql = "DELETE FROM " + (userType.equals("student") ? "students" : "tutors") + " WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteUserSql)) {
                pstmt.setInt(1, userId);
                int rows = pstmt.executeUpdate();
                
                if (rows > 0) {
                    connection.commit();
                    return true;
                } else {
                    connection.rollback();
                    return false;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
