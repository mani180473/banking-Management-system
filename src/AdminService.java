import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

public class AdminService {

    private final Scanner sc;

    // Constructor to reuse scanner from Main
    public AdminService(Scanner sc) {
        this.sc = sc;
    }

    public void showAdminMenu() {
    int choice;
    do {
        System.out.println("\n=== Admin Menu ===");
        System.out.println("1. View All Users");
        System.out.println("2. View All Transactions");
        System.out.println("3. Filter Transactions");
        System.out.println("4. Block User");
        System.out.println("5. Reset User Balance");
        System.out.println("6. Credit Monthly Interest");
        System.out.println("7. Export Transactions");
        System.out.println("8. Exit to Main Menu");
        System.out.print("Enter your choice: ");

        while (true) {
            String input = sc.nextLine();
            try {
                choice = Integer.parseInt(input);
                break;
            } catch (NumberFormatException e) {
                System.out.print("❌ Invalid input! Enter a number 1-8: ");
            }
        }

        switch (choice) {
            case 1 -> viewAllUsers();
            case 2 -> viewAllTransactions();
            case 3 -> filterTransactions(sc);
            case 4 -> blockUser();
            case 5 -> resetUserBalance();
            case 6 -> creditMonthlyInterest();
            case 7 -> {
                System.out.print("Enter User ID to export transactions: ");
                int userId = Integer.parseInt(sc.nextLine());
                exportTransactions(userId);
            }
            case 8 -> System.out.println("Returning to Main Menu...");
            default -> System.out.println("❌ Invalid choice!");
        }
    } while (choice != 8); // exit when 8 is selected
}

    private void viewAllUsers() {
        String query = "SELECT id, name, email, balance, role FROM users";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("\n=== All Users ===");
            while (rs.next()) {
                System.out.printf("ID: %d | Name: %s | Email: %s | Balance: %.2f | Role: %s%n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getDouble("balance"),
                        rs.getString("role"));
            }

        } catch (SQLException e) {
            System.out.println("❌ Error fetching users: " + e.getMessage());
        }
    }

    private void viewAllTransactions() {
        String query = "SELECT * FROM transactions ORDER BY transaction_date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("\n=== All Transactions ===");
            while (rs.next()) {
                System.out.printf("ID: %d | UserID: %d | Type: %s | Amount: %.2f | TargetID: %s | Date: %s%n",
                        rs.getInt("transaction_id"),
                        rs.getInt("user_id"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getObject("target_user_id"),
                        rs.getString("transaction_date"));
            }

        } catch (SQLException e) {
            System.out.println("❌ Error fetching transactions: " + e.getMessage());
        }
    }

    public void filterTransactions(Scanner sc) {
    System.out.println("\n=== Filter Transactions ===");
    System.out.println("1. By User ID");
    System.out.println("2. By Type (DEPOSIT/WITHDRAWAL/TRANSFER)");
    System.out.println("3. By Date Range");
    System.out.println("4. By Amount Range");
    System.out.println("5. Back");

    System.out.print("Enter your choice: ");
    int choice = Integer.parseInt(sc.nextLine());

    String query = "";
    try (Connection conn = DBConnection.getConnection()) {
        PreparedStatement stmt;

        switch (choice) {
            case 1 -> {
                System.out.print("Enter User ID: ");
                int userId = Integer.parseInt(sc.nextLine());
                query = "SELECT * FROM transactions WHERE user_id = ?";
                stmt = conn.prepareStatement(query);
                stmt.setInt(1, userId);
                showTransactions(stmt);
            }
            case 2 -> {
                System.out.print("Enter Type (DEPOSIT/WITHDRAWAL/TRANSFER): ");
                String type = sc.nextLine().toUpperCase();
                query = "SELECT * FROM transactions WHERE type = ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, type);
                showTransactions(stmt);
            }
            case 3 -> {
                System.out.print("Enter Start Date (YYYY-MM-DD): ");
                String start = sc.nextLine();
                System.out.print("Enter End Date (YYYY-MM-DD): ");
                String end = sc.nextLine();
                query = "SELECT * FROM transactions WHERE DATE(transaction_date) BETWEEN ? AND ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, start);
                stmt.setString(2, end);
                showTransactions(stmt);
            }
            case 4 -> {
                System.out.print("Enter min amount: ");
                double min = Double.parseDouble(sc.nextLine());
                System.out.print("Enter max amount: ");
                double max = Double.parseDouble(sc.nextLine());
                query = "SELECT * FROM transactions WHERE amount BETWEEN ? AND ?";
                stmt = conn.prepareStatement(query);
                stmt.setDouble(1, min);
                stmt.setDouble(2, max);
                showTransactions(stmt);
            }
            case 5 -> {
                System.out.println("Returning...");
                return;
            }
            default -> System.out.println("❌ Invalid choice!");
        }

    } catch (SQLException e) {
        System.out.println("❌ Error: " + e.getMessage());
    }
}

// Helper method to display results
private void showTransactions(PreparedStatement stmt) throws SQLException {
    try (ResultSet rs = stmt.executeQuery()) {
        System.out.println("\nID | UserID | Type | Amount | Target User | Date");
        System.out.println("--------------------------------------------------");
        while (rs.next()) {
            System.out.printf("%d | %d | %s | %.2f | %s | %s%n",
                    rs.getInt("transaction_id"),
                    rs.getInt("user_id"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getObject("target_user_id") != null ? rs.getInt("target_user_id") : "-",
                    rs.getTimestamp("transaction_date"));
        }
    }
}
    public void blockUser() {
    System.out.println("\n=== Block User ===");
    System.out.print("Enter User ID: ");
    int userId;
    while (true) {
        try {
            userId = Integer.parseInt(sc.nextLine());
            break;
        } catch (NumberFormatException e) {
            System.out.print("❌ Invalid input! Enter a numeric User ID: ");
        }
    }

    try (Connection conn = DBConnection.getConnection()) {
        // Block user by updating status
        String updateQuery = "UPDATE users SET status = 'BLOCKED' WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateQuery)) {
            ps.setInt(1, userId);
            int rows = ps.executeUpdate();
            if (rows > 0) System.out.println("✅ User blocked successfully!");
            else System.out.println("❌ User not found!");
        }
    } catch (SQLException e) {
        System.out.println("❌ Database error: " + e.getMessage());
    }
}


    private void resetUserBalance() {
        int userId;
        double newBalance;

        System.out.print("Enter user ID to reset balance: ");
        while (true) {
            try {
                userId = Integer.parseInt(sc.nextLine());
                break;
            } catch (NumberFormatException e) {
                System.out.print("❌ Invalid input! Enter a numeric user ID: ");
            }
        }

        System.out.print("Enter new balance: ");
        while (true) {
            try {
                newBalance = Double.parseDouble(sc.nextLine());
                break;
            } catch (NumberFormatException e) {
                System.out.print("❌ Invalid input! Enter a valid balance: ");
            }
        }

        String query = "UPDATE users SET balance = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setDouble(1, newBalance);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();
            if (rows > 0) System.out.println("✅ User balance reset successfully!");
            else System.out.println("❌ User not found!");

        } catch (SQLException e) {
            System.out.println("❌ Error updating balance: " + e.getMessage());
        }
    }

    public void creditMonthlyInterest() {
        String query = "UPDATE users SET balance = balance + (balance * 0.04 / 12) WHERE account_type = 'SAVINGS'";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            int rows = ps.executeUpdate();
            System.out.println("✅ Interest credited to " + rows + " users' savings accounts.");
        } catch (SQLException e) {
            System.out.println("❌ Error crediting interest: " + e.getMessage());
        }
    }

    public void exportTransactions(int userId) {
    String query = "SELECT * FROM transactions WHERE user_id = ? ORDER BY transaction_date DESC";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(query)) {
        ps.setInt(1, userId);
        try (ResultSet rs = ps.executeQuery()) {

            String fileName = "transactions_user_" + userId + ".csv";
            try (FileWriter fw = new FileWriter(fileName)) {
                // Write CSV header
                fw.write("Transaction ID,User ID,Type,Amount,Target User ID,Date\n");

                // Write data
                while (rs.next()) {
                    fw.write(
                        rs.getInt("transaction_id") + "," +
                        rs.getInt("user_id") + "," +
                        rs.getString("type") + "," +
                        rs.getBigDecimal("amount") + "," +
                        (rs.getObject("target_user_id") != null ? rs.getInt("target_user_id") : "") + "," +
                        rs.getTimestamp("transaction_date") + "\n"
                    );
                }
            }
            System.out.println("✅ Transactions exported to " + fileName);

        }
    } catch (SQLException | IOException e) {
        System.out.println("❌ Error exporting transactions: " + e.getMessage());
    }
}

}
