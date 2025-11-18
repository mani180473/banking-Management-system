import java.sql.*;
import java.util.Scanner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserService {

    private final Scanner sc;

    // Constructor to reuse Scanner from Main
    public UserService(Scanner sc) {
        this.sc = sc;
    }

    // ---------------- REGISTER ----------------
    public int register() {
        System.out.print("Enter your name: ");
        String name = sc.nextLine().trim();

        String email;
        while (true) {
            System.out.print("Enter your email: ");
            email = sc.nextLine().trim();
            if (email.isEmpty()) {
                System.out.println("❌ Email cannot be empty!");
            } else {
                break;
            }
        }

        double initialDeposit = readDouble("Initial deposit: ");

        if (initialDeposit < 0) {
            System.out.println("❌ Initial deposit must be non-negative!");
            return -1;
        }

        // Password input
        System.out.print("Enter password: ");
        String password = sc.nextLine().trim();
        String hashedPassword = hashPassword(password);

        // Ask for role, default is USER
        String role;
        while (true) {
            System.out.print("Enter role (USER/ADMIN) [default USER]: ");
            role = sc.nextLine().trim().toUpperCase();
            if (role.isEmpty()) {
                role = "USER";
                break;
            }
            if (role.equals("USER") || role.equals("ADMIN")) break;
            else System.out.println("❌ Invalid role! Only USER or ADMIN allowed.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Check if email already exists
            String check = "SELECT id FROM users WHERE email = ?";
            PreparedStatement psCheck = conn.prepareStatement(check);
            psCheck.setString(1, email);
            ResultSet rsCheck = psCheck.executeQuery();
            if (rsCheck.next()) {
                System.out.println("❌ Email already registered!");
                return -1;
            }

            // Insert user
            String insertUser = "INSERT INTO users (name, email, balance, role, password) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement psUser = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS);
            psUser.setString(1, name);
            psUser.setString(2, email);
            psUser.setDouble(3, initialDeposit);
            psUser.setString(4, role);
            psUser.setString(5, hashedPassword);
            psUser.executeUpdate();

            ResultSet rs = psUser.getGeneratedKeys();
            int userId = -1;
            if (rs.next()) {
                userId = rs.getInt(1);
            }

            System.out.println("✅ Registration successful! Your user ID: " + userId + " | Role: " + role);
            return userId;

        } catch (SQLException e) {
            System.out.println("❌ Registration failed: " + e.getMessage());
            return -1;
        }
    }
        // ---------------- LOGIN ----------------
public User login() {
    System.out.print("Enter email: ");
    String email = sc.nextLine().trim();

    System.out.print("Enter password: ");
    String password = sc.nextLine().trim();
    String hashedPassword = hashPassword(password);

    try (Connection conn = DBConnection.getConnection()) {

        // Select all needed columns including password
        String query = "SELECT id, name, email, balance, role, password, failed_attempts, locked FROM users WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    int userId = rs.getInt("id");
                    String name = rs.getString("name");
                    String role = rs.getString("role");
                    double balance = rs.getDouble("balance");
                    String storedPassword = rs.getString("password");
                    int failedAttempts = rs.getInt("failed_attempts");
                    boolean locked = rs.getBoolean("locked");

                    // Check if account is locked
                    if (locked) {
                        System.out.println("❌ Your account is locked due to multiple failed login attempts. Contact admin.");
                        return null;
                    }

                    // Check password
                    if (hashedPassword.equals(storedPassword)) {
                        // Successful login → reset failed attempts
                        resetFailedAttempts(userId, conn);

                        System.out.println("✅ Welcome " + name + "! Your balance: " + balance + " | Role: " + role);
                        return new User(userId, name, email, role);
                    } else {
                        // Wrong password → increment failed attempts
                        incrementFailedAttempts(email, conn);
                        System.out.println("❌ Invalid email or password!");
                        return null;
                    }
                } else {
                    // Email not found
                    System.out.println("❌ Invalid email or password!");
                    return null;
                }
            }
        }

    } catch (SQLException e) {
        System.out.println("❌ Login failed: " + e.getMessage());
        return null;
    }
}

// ---------------- HELPER METHODS ----------------
private void incrementFailedAttempts(String email, Connection conn) throws SQLException {
    String incQuery = "UPDATE users SET failed_attempts = failed_attempts + 1 WHERE email = ?";
    try (PreparedStatement ps = conn.prepareStatement(incQuery)) {
        ps.setString(1, email);
        ps.executeUpdate();
    }

    // Lock account if 3 or more failed attempts
    String lockQuery = "UPDATE users SET locked = TRUE WHERE email = ? AND failed_attempts >= 3";
    try (PreparedStatement ps = conn.prepareStatement(lockQuery)) {
        ps.setString(1, email);
        int rows = ps.executeUpdate();
        if (rows > 0) {
            System.out.println("❌ Your account has been locked due to 3 unsuccessful login attempts. Contact admin to unlock.");
        }
    }
}

private void resetFailedAttempts(int userId, Connection conn) throws SQLException {
    String query = "UPDATE users SET failed_attempts = 0 WHERE id = ?";
    try (PreparedStatement ps = conn.prepareStatement(query)) {
        ps.setInt(1, userId);
        ps.executeUpdate();
    }
}



    // ---------------- PASSWORD CHANGE ----------------
    public void changePassword(int userId) {
        System.out.print("Enter your current password: ");
        String currentPass = sc.nextLine().trim();
        String hashedCurrent = hashPassword(currentPass);

        try (Connection conn = DBConnection.getConnection()) {

        // ---------------- VERIFY CURRENT PASSWORD ----------------
        String query = "SELECT password FROM users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (!storedHash.equals(hashedCurrent)) {
                        System.out.println("❌ Current password incorrect!");
                        return;
                    }
                } else {
                    System.out.println("❌ User not found!");
                    return;
                }
            }
        }

            // New password input
            System.out.print("Enter new password: ");
            String newPass = sc.nextLine().trim();
            System.out.print("Confirm new password: ");
            String confirmPass = sc.nextLine().trim();

            if (!newPass.equals(confirmPass)) {
                System.out.println("❌ Passwords do not match!");
                return;
            }

            if (newPass.length() < 6) {
                System.out.println("❌ Password must be at least 6 characters long!");
                return;
            }


            String hashedNew = hashPassword(newPass);

            // Update password
            String update = "UPDATE users SET password = ? WHERE id = ?";
            PreparedStatement psUpdate = conn.prepareStatement(update);
            psUpdate.setString(1, hashedNew);
            psUpdate.setInt(2, userId);
            psUpdate.executeUpdate();

            System.out.println("✅ Password changed successfully!");

        } catch (SQLException e) {
            System.out.println("❌ Error changing password: " + e.getMessage());
        }
    }

    // ---------------- HASH PASSWORD ----------------
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    // ---------------- INITIAL ADMIN ----------------
    public void createInitialAdmin() {
        try (Connection conn = DBConnection.getConnection()) {
            String checkAdmin = "SELECT id FROM users WHERE role = 'ADMIN'";
            PreparedStatement psCheck = conn.prepareStatement(checkAdmin);
            ResultSet rs = psCheck.executeQuery();
            if (rs.next()) return; // admin exists

            String insertAdmin = "INSERT INTO users (name, email, balance, role, password) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement psAdmin = conn.prepareStatement(insertAdmin);
            psAdmin.setString(1, "Admin");
            psAdmin.setString(2, "admin@bank.com");
            psAdmin.setDouble(3, 0.0);
            psAdmin.setString(4, "ADMIN");
            psAdmin.setString(5, hashPassword("admin123"));
            psAdmin.executeUpdate();

            System.out.println("✅ Initial admin created: email=admin@bank.com, password=admin123");

        } catch (SQLException e) {
            System.out.println("❌ Failed to create initial admin: " + e.getMessage());
        }
    }

    // ---------------- HELPER: SAFE DOUBLE INPUT ----------------
    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine();
            try {
                double value = Double.parseDouble(input);
                if (value < 0) {
                    System.out.println("❌ Value cannot be negative!");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input! Please enter a numeric value.");
            }
        }
    }

}
