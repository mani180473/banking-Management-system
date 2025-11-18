import java.sql.Connection;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        // Pass the same Scanner to all services
        UserService userService = new UserService(sc);
        userService.createInitialAdmin(); // ensure admin exists
        AccountService accountService = new AccountService(sc); // create once
        AdminService adminService = new AdminService(sc); // create once

        int choice;
        User loggedInUser = null; // store logged-in user

        System.out.println("Testing DB Connection...");
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            System.out.println("✅ Database connection successful!");
        } else {
            System.out.println("❌ Database connection failed!");
        }

        do {
            System.out.println("\n=== Banking Management System ===");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Change Password");
            System.out.println("4. Exit");

            // Safe input loop
            while (true) {
                System.out.print("Enter your choice: ");
                String input = sc.nextLine();
                try {
                    choice = Integer.parseInt(input);
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("❌ Invalid input! Please enter a number between 1-4.");
                }
            }

            switch (choice) {
                case 1 -> userService.register();

                case 2 -> {
                    loggedInUser = userService.login(); // store logged-in user
                    if (loggedInUser != null) {
                        if ("ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
                            System.out.println("🔑 Admin access granted!");
                            adminService.showAdminMenu();
                        } else {
                            accountService.showMenu(loggedInUser.getId());
                        }
                    } else {
                        System.out.println("❌ Login failed! Check your credentials.");
                    }
                }

                case 3 -> {
                    if (loggedInUser != null) {
                        userService.changePassword(loggedInUser.getId());
                    } else {
                        System.out.println("❌ You must login first to change password!");
                    }
                }

                case 4 -> System.out.println("Exiting...");

                default -> System.out.println("❌ Invalid choice, try again.");
            }

        } while (choice != 4);

        sc.close();
    }

    // Optional utility method for safe integer input
    private static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input! Please enter a numeric value.");
            }
        }
    }
}
