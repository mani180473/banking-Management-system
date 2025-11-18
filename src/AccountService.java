import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class AccountService {
    private final Scanner sc;

    // Constructor to reuse Scanner from Main
    public AccountService(Scanner sc) {
        this.sc = sc;
    }

    public void showMenu(int userId) {
        int choice;

        do {
            System.out.println("\n=== Account Menu ===");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. Check Balance");
            System.out.println("4. Transfer Funds");
            System.out.println("5. View Transaction History");
            System.out.println("6. Exit to Main Menu");

            choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1 -> deposit(userId);
                case 2 -> withdraw(userId);
                case 3 -> checkBalance(userId);
                case 4 -> transfer(userId);
                case 5 -> viewTransactions(userId, sc);
                case 6 -> System.out.println("Returning to Main Menu...");
                default -> System.out.println("❌ Invalid choice!");
            }

        } while (choice != 6);
    }

    private void deposit(int userId) {
        double amount = readDouble("Enter amount to deposit: ");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE users SET balance = balance + ? WHERE id = ?")) {

            ps.setDouble(1, amount);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Deposited " + amount);
                recordTransaction(userId, "DEPOSIT", amount, null);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error depositing: " + e.getMessage());
        }
    }

    private void withdraw(int userId) {
        double amount = readDouble("Enter amount to withdraw: ");

        try (Connection conn = DBConnection.getConnection()) {
            double balance = 0;

            try (PreparedStatement ps = conn.prepareStatement("SELECT balance FROM users WHERE id = ?")) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) balance = rs.getDouble("balance");
            }

            if (amount > balance) {
                System.out.println("❌ Insufficient balance!");
                return;
            }

            try (PreparedStatement ps = conn.prepareStatement("UPDATE users SET balance = balance - ? WHERE id = ?")) {
                ps.setDouble(1, amount);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }

            System.out.println("✅ Withdrawn " + amount);
            recordTransaction(userId, "WITHDRAWAL", amount, null);

        } catch (SQLException e) {
            System.out.println("❌ Error withdrawing: " + e.getMessage());
        }
    }

    private void checkBalance(int userId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT balance FROM users WHERE id = ?")) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("💰 Current balance: " + rs.getDouble("balance"));
            }

        } catch (SQLException e) {
            System.out.println("❌ Error fetching balance: " + e.getMessage());
        }
    }

    private void transfer(int userId) {
        System.out.print("Enter recipient email: ");
        String recipientEmail = sc.nextLine().trim();
        double amount = readDouble("Enter amount to transfer: ");

        try (Connection conn = DBConnection.getConnection()) {
            int recipientId;
            double senderBalance;

            // Get recipient info
            try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE email = ?")) {
                ps.setString(1, recipientEmail);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    System.out.println("❌ Recipient not found!");
                    return;
                }
                recipientId = rs.getInt("id");
            }

            // Get sender balance
            try (PreparedStatement ps = conn.prepareStatement("SELECT balance FROM users WHERE id = ?")) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                rs.next();
                senderBalance = rs.getDouble("balance");
            }

            if (amount > senderBalance) {
                System.out.println("❌ Insufficient balance!");
                return;
            }

            // Deduct from sender
            try (PreparedStatement ps = conn.prepareStatement("UPDATE users SET balance = balance - ? WHERE id = ?")) {
                ps.setDouble(1, amount);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }

            // Add to recipient
            try (PreparedStatement ps = conn.prepareStatement("UPDATE users SET balance = balance + ? WHERE id = ?")) {
                ps.setDouble(1, amount);
                ps.setInt(2, recipientId);
                ps.executeUpdate();
            }

            System.out.println("✅ Transferred " + amount + " to " + recipientEmail);
            recordTransaction(userId, "TRANSFER", amount, recipientId);

        } catch (SQLException e) {
            System.out.println("❌ Error transferring funds: " + e.getMessage());
        }
    }

    private void recordTransaction(int userId, String type, double amount, Integer targetUserId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO transactions (user_id, type, amount, target_user_id) VALUES (?, ?, ?, ?)")) {

            ps.setInt(1, userId);
            ps.setString(2, type);
            ps.setDouble(3, amount);
            if (targetUserId != null) ps.setInt(4, targetUserId);
            else ps.setNull(4, java.sql.Types.INTEGER);

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("❌ Transaction record failed: " + e.getMessage());
        }
    }

    public void viewTransactions(int userId, Scanner sc) {
    System.out.println("\n=== View Transactions ===");
    System.out.println("1. All transactions");
    System.out.println("2. By date range");
    System.out.println("3. By type");
    System.out.println("4. By amount range");

    int choice = readInt("Enter filter option: ");

    String query = "SELECT * FROM transactions WHERE user_id = ?";
    String start = null, end = null, type = null;
    double min = 0, max = 0;

    switch (choice) {
        case 1 -> {} // no filter
        case 2 -> {
            System.out.print("Enter start date (YYYY-MM-DD): ");
            start = sc.nextLine().trim();
            System.out.print("Enter end date (YYYY-MM-DD): ");
            end = sc.nextLine().trim();
            query += " AND DATE(transaction_date) BETWEEN ? AND ?";
        }
        case 3 -> {
            System.out.print("Enter type (DEPOSIT/WITHDRAWAL/TRANSFER): ");
            type = sc.nextLine().trim().toUpperCase();
            query += " AND type = ?";
        }
        case 4 -> {
            min = readDouble("Enter min amount: ");
            max = readDouble("Enter max amount: ");
            query += " AND amount BETWEEN ? AND ?";
        }
        default -> {
            System.out.println("❌ Invalid choice!");
            return;
        }
    }

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(query)) {

        int paramIndex = 1;
        ps.setInt(paramIndex++, userId);

        if (choice == 2) {
            ps.setString(paramIndex++, start);
            ps.setString(paramIndex++, end);
        } else if (choice == 3) {
            ps.setString(paramIndex++, type);
        } else if (choice == 4) {
            ps.setDouble(paramIndex++, min);
            ps.setDouble(paramIndex++, max);
        }

        try (ResultSet rs = ps.executeQuery()) {
            System.out.println("\nID | Type | Amount | Date | Target User");
            System.out.println("--------------------------------------------");
            while (rs.next()) {
                System.out.printf("%d | %s | %.2f | %s | %s%n",
                        rs.getInt("transaction_id"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getTimestamp("transaction_date"),
                        rs.getObject("target_user_id") != null ? rs.getInt("target_user_id") : "-");
            }
        }

    } catch (SQLException e) {
        System.out.println("❌ Error fetching transactions: " + e.getMessage());
    }
}


    // Utility method for safe int input
    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input! Please enter a number.");
            }
        }
    }

    // Utility method for safe double input
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
