package com.myhometutor.database;

import org.json.JSONObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
                    status TEXT DEFAULT 'pending',
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
                    status TEXT DEFAULT 'pending',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
            
            // Tuition posts table
            String createTuitionPostsTable = """
                CREATE TABLE IF NOT EXISTS tuition_posts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id INTEGER NOT NULL,
                    data TEXT NOT NULL,
                    status TEXT DEFAULT 'pending',
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
                    reference_id INTEGER,
                    reference_type TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;

            // Reports table
            String createReportsTable = """
                CREATE TABLE IF NOT EXISTS reports (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    reporter_id INTEGER NOT NULL,
                    reporter_type TEXT NOT NULL,
                    reported_id INTEGER NOT NULL,
                    reported_type TEXT NOT NULL,
                    reason TEXT NOT NULL,
                    status TEXT DEFAULT 'pending',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
            
            stmt.execute(createStudentsTable);
            stmt.execute(createTutorsTable);
            stmt.execute(createTuitionPostsTable);
            stmt.execute(createApplicationsTable);
            stmt.execute(createNotificationsTable);
            stmt.execute(createReportsTable);
            
            // Attempt to add is_profile_revealed column if it doesn't exist (for existing DBs)
            try {
                stmt.execute("ALTER TABLE applications ADD COLUMN is_profile_revealed INTEGER DEFAULT 0");
            } catch (SQLException e) {
                // Column likely already exists, ignore
            }
            // Attempt to add is_verified column to tutors if it doesn't exist
            try {
                stmt.execute("ALTER TABLE tutors ADD COLUMN is_verified INTEGER DEFAULT 0");
            } catch (SQLException e) {
                // Column likely already exists
            }

            // Attempt to add status column to students if it doesn't exist
            try {
                stmt.execute("ALTER TABLE students ADD COLUMN status TEXT DEFAULT 'pending'");
            } catch (SQLException e) {
                // Column likely already exists
            }

            // Attempt to add status column to tutors if it doesn't exist
            try {
                stmt.execute("ALTER TABLE tutors ADD COLUMN status TEXT DEFAULT 'pending'");
            } catch (SQLException e) {
                // Column likely already exists
            }

            updateNotificationsTableSchema();
            
            stmt.close();
            System.out.println("Database tables initialized successfully.");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateNotificationsTableSchema() {
        try (Statement stmt = connection.createStatement()) {
            // Check if reference_id column exists
            boolean hasReferenceId = false;
            boolean hasReferenceType = false;
            
            try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(notifications)")) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    if ("reference_id".equals(name)) hasReferenceId = true;
                    if ("reference_type".equals(name)) hasReferenceType = true;
                }
            }
            
            if (!hasReferenceId) {
                stmt.execute("ALTER TABLE notifications ADD COLUMN reference_id INTEGER");
            }
            if (!hasReferenceType) {
                stmt.execute("ALTER TABLE notifications ADD COLUMN reference_type TEXT");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Register a new student
    public boolean registerStudent(String username, String password, JSONObject data) {
        String sql = "INSERT INTO students (username, password, data, status) VALUES (?, ?, ?, 'pending')";
        
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
        String sql = "INSERT INTO tutors (username, password, data, status) VALUES (?, ?, ?, 'pending')";
        
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
        String sql = "SELECT id, data, status FROM students WHERE username = ? AND password = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                JSONObject data = new JSONObject(rs.getString("data"));
                data.put("id", rs.getInt("id"));
                data.put("username", username);
                data.put("status", rs.getString("status"));
                return data;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Authenticate tutor
    public JSONObject authenticateTutor(String username, String password) {
        String sql = "SELECT id, data, status FROM tutors WHERE username = ? AND password = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                JSONObject data = new JSONObject(rs.getString("data"));
                data.put("id", rs.getInt("id"));
                data.put("username", username);
                data.put("status", rs.getString("status"));
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

    // Get user status by username
    public String getUserStatus(String username, String userType) {
        String table = userType.equals("Student") ? "students" : "tutors";
        String sql = "SELECT status FROM " + table + " WHERE username = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("status");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    // Delete user by username (used for re-registration of rejected users)
    public boolean deleteUserByUsername(String username, String userType) {
        String table = userType.equals("Student") ? "students" : "tutors";
        String sql = "DELETE FROM " + table + " WHERE username = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Create a new tuition post
    public boolean createTuitionPost(int studentId, JSONObject postData) {
        String sql = "INSERT INTO tuition_posts (student_id, data, status) VALUES (?, ?, 'pending')";
        
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
                     "WHERE tp.status = 'active' " +
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

    // Get a single tuition post by ID
    public JSONObject getTuitionPost(int postId) {
        String sql = "SELECT tp.id, tp.student_id, tp.data, tp.status, tp.created_at, s.data as student_data " +
                     "FROM tuition_posts tp " +
                     "JOIN students s ON tp.student_id = s.id " +
                     "WHERE tp.id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    JSONObject post = new JSONObject(rs.getString("data"));
                    post.put("id", rs.getInt("id"));
                    post.put("studentId", rs.getInt("student_id"));
                    post.put("status", rs.getString("status"));
                    post.put("createdAt", rs.getString("created_at"));
                    
                    JSONObject studentData = new JSONObject(rs.getString("student_data"));
                    post.put("studentName", studentData.optString("name", "Unknown"));
                    post.put("studentPhone", studentData.optString("phone", ""));
                    post.put("studentGender", studentData.optString("gender", "Unknown"));
                    
                    return post;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
        String sql = "SELECT a.id, a.status, a.created_at, a.is_profile_revealed, tp.data as post_data, s.id as student_id, s.data as student_data " +
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
                
                // Add student ID to student data
                studentData.put("id", rs.getInt("student_id"));
                
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
        createNotification(userId, userType, message, 0, null);
    }

    public void createNotification(int userId, String userType, String message, int referenceId, String referenceType) {
        String sql = "INSERT INTO notifications (user_id, user_type, message, reference_id, reference_type) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, userType);
            pstmt.setString(3, message);
            if (referenceId > 0) {
                pstmt.setInt(4, referenceId);
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            if (referenceType != null) {
                pstmt.setString(5, referenceType);
            } else {
                pstmt.setNull(5, Types.VARCHAR);
            }
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
        String sql = "SELECT id, message, is_read, reference_id, reference_type, created_at FROM notifications " +
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
                notif.put("referenceId", rs.getInt("reference_id"));
                notif.put("referenceType", rs.getString("reference_type"));
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
                JSONObject data = new JSONObject(rs.getString("data"));
                data.put("id", tutorId);
                return data;
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

    // Admin Methods
    
    public int getTotalStudents() {
        String sql = "SELECT COUNT(*) FROM students";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalTutors() {
        String sql = "SELECT COUNT(*) FROM tutors";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalTuitionPosts() {
        String sql = "SELECT COUNT(*) FROM tuition_posts";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getPendingTuitionPostsCount() {
        String sql = "SELECT COUNT(*) FROM tuition_posts WHERE status = 'pending'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getApprovedTuitionPostsCount() {
        String sql = "SELECT COUNT(*) FROM tuition_posts WHERE status IN ('active', 'assigned')";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getPendingStudentVerificationsCount() {
        String sql = "SELECT COUNT(*) FROM students WHERE status = 'pending'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getPendingTutorVerificationsCount() {
        String sql = "SELECT COUNT(*) FROM tutors WHERE status = 'pending'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalConnectionsCount() {
        String sql = "SELECT COUNT(*) FROM applications WHERE status = 'accepted'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getActivePostsCount() {
        String sql = "SELECT COUNT(*) FROM tuition_posts WHERE status = 'active'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getPendingTutorsCount() {
        return getPendingTutorVerificationsCount();
    }

    public void verifyTutor(int tutorId, boolean isApproved) {
        String sql = "UPDATE tutors SET status = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, isApproved ? "active" : "rejected");
            pstmt.setInt(2, tutorId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean updateUserStatus(String tableName, int userId, String status) {
        if (instance == null) return false;
        
        String sql = "UPDATE " + tableName + " SET status = ? WHERE id = ?";
        try (PreparedStatement pstmt = instance.connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, userId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<JSONObject> getPendingTutors() {
        List<JSONObject> tutors = new ArrayList<>();
        String sql = "SELECT id, username, data FROM tutors WHERE is_verified = 0";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                JSONObject tutor = new JSONObject(rs.getString("data"));
                tutor.put("id", rs.getInt("id"));
                tutor.put("username", rs.getString("username"));
                tutors.add(tutor);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tutors;
    }

    public List<JSONObject> getAllConnections() {
        List<JSONObject> connections = new ArrayList<>();
        String sql = "SELECT a.id, a.tuition_post_id, a.tutor_id, a.status, a.created_at, " +
                     "t.data as tutor_data, s.data as student_data " +
                     "FROM applications a " +
                     "JOIN tutors t ON a.tutor_id = t.id " +
                     "JOIN tuition_posts tp ON a.tuition_post_id = tp.id " +
                     "JOIN students s ON tp.student_id = s.id " +
                     "WHERE a.status = 'accepted'";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                JSONObject conn = new JSONObject();
                conn.put("id", rs.getInt("id"));
                conn.put("tuition_post_id", rs.getInt("tuition_post_id"));
                conn.put("tutor_id", rs.getInt("tutor_id"));
                conn.put("status", rs.getString("status"));
                conn.put("created_at", rs.getString("created_at"));
                
                JSONObject tutorData = new JSONObject(rs.getString("tutor_data"));
                conn.put("tutor_name", tutorData.optString("name", "Unknown"));
                
                JSONObject studentData = new JSONObject(rs.getString("student_data"));
                conn.put("student_name", studentData.optString("name", "Unknown"));
                
                connections.add(conn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connections;
    }

    public List<JSONObject> getAllTuitionPostsForAdmin() {
        List<JSONObject> posts = new ArrayList<>();
        String sql = "SELECT tp.id, tp.student_id, tp.data, tp.status, s.data as student_data " +
                     "FROM tuition_posts tp JOIN students s ON tp.student_id = s.id";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                JSONObject post = new JSONObject(rs.getString("data"));
                post.put("id", rs.getInt("id"));
                post.put("student_id", rs.getInt("student_id"));
                post.put("status", rs.getString("status"));
                
                JSONObject studentData = new JSONObject(rs.getString("student_data"));
                post.put("student_name", studentData.optString("name", "Unknown"));
                post.put("student_email", studentData.optString("email", ""));
                
                posts.add(post);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    public boolean updateTuitionPostStatus(int postId, String status) {
        String sql = "UPDATE tuition_posts SET status = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, postId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<JSONObject> getAllUsers() {
        List<JSONObject> users = new ArrayList<>();
        
        // Get Students
        String studentSql = "SELECT id, username, data, status FROM students";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(studentSql)) {
            while (rs.next()) {
                JSONObject user = new JSONObject(rs.getString("data"));
                user.put("id", rs.getInt("id"));
                user.put("username", rs.getString("username"));
                user.put("type", "Student");
                user.put("status", rs.getString("status"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Get Tutors
        String tutorSql = "SELECT id, username, data, status FROM tutors";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(tutorSql)) {
            while (rs.next()) {
                JSONObject user = new JSONObject(rs.getString("data"));
                user.put("id", rs.getInt("id"));
                user.put("username", rs.getString("username"));
                user.put("type", "Tutor");
                user.put("status", rs.getString("status"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return users;
    }

    // Report Methods
    public boolean createReport(int reporterId, String reporterType, int reportedId, String reportedType, String reason) {
        String sql = "INSERT INTO reports (reporter_id, reporter_type, reported_id, reported_type, reason) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, reporterId);
            pstmt.setString(2, reporterType);
            pstmt.setInt(3, reportedId);
            pstmt.setString(4, reportedType);
            pstmt.setString(5, reason);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public org.json.JSONArray getAllReports() {
        org.json.JSONArray reports = new org.json.JSONArray();
        String sql = "SELECT * FROM reports ORDER BY created_at DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                org.json.JSONObject report = new org.json.JSONObject();
                report.put("id", rs.getInt("id"));
                report.put("reporter_id", rs.getInt("reporter_id"));
                report.put("reporter_type", rs.getString("reporter_type"));
                report.put("reported_id", rs.getInt("reported_id"));
                report.put("reported_type", rs.getString("reported_type"));
                report.put("reason", rs.getString("reason"));
                report.put("status", rs.getString("status"));
                report.put("created_at", rs.getString("created_at"));
                reports.put(report);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reports;
    }

    public boolean updateReportStatus(int reportId, String status) {
        String sql = "UPDATE reports SET status = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, reportId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getUserName(int id, String type) {
        String table = type.equalsIgnoreCase("student") ? "students" : "tutors";
        String sql = "SELECT username FROM " + table + " WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    public boolean banUser(int userId, String userType) {
        String table = userType.equalsIgnoreCase("student") ? "students" : "tutors";
        String sql = "UPDATE " + table + " SET status = 'banned' WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<JSONObject> getBannedUsers() {
        List<JSONObject> users = new ArrayList<>();
        String sqlStudent = "SELECT id, username, data, 'Student' as type FROM students WHERE status = 'banned'";
        String sqlTutor = "SELECT id, username, data, 'Tutor' as type FROM tutors WHERE status = 'banned'";
        
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sqlStudent);
            while (rs.next()) {
                JSONObject user = new JSONObject(rs.getString("data"));
                user.put("id", rs.getInt("id"));
                user.put("username", rs.getString("username"));
                user.put("type", "Student");
                user.put("status", "banned");
                users.add(user);
            }
            
            rs = stmt.executeQuery(sqlTutor);
            while (rs.next()) {
                JSONObject user = new JSONObject(rs.getString("data"));
                user.put("id", rs.getInt("id"));
                user.put("username", rs.getString("username"));
                user.put("type", "Tutor");
                user.put("status", "banned");
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean unbanUser(int userId, String userType) {
        String table = userType.equalsIgnoreCase("student") ? "students" : "tutors";
        // Restore to active/verified state
        String newStatus = userType.equalsIgnoreCase("student") ? "active" : "verified";
        String sql = "UPDATE " + table + " SET status = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteReport(int reportId) {
        String sql = "DELETE FROM reports WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, reportId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getUnreadNotificationCount(int userId, String userType) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND user_type = ? AND is_read = 0";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, userType);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void markNotificationsAsRead(int userId, String userType) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE user_id = ? AND user_type = ? AND is_read = 0";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, userType);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
