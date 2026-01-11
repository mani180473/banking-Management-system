// src/main/java/web/WithdrawServlet.java
package web;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import util.DBConnection;

public class WithdrawServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("email") == null) {
            response.getWriter().println("Please login first.");
            return;
        }

        String email = (String) session.getAttribute("email");
        double withdrawAmount = Double.parseDouble(request.getParameter("amount"));

        try (Connection con = DBConnection.getConnection()) {

            // Step 1: Get current balance
            String selectSql = "SELECT balance FROM users WHERE email = ?";
            PreparedStatement selectPs = con.prepareStatement(selectSql);
            selectPs.setString(1, email);

            ResultSet rs = selectPs.executeQuery();

            if (!rs.next()) {
                response.getWriter().println("User not found.");
                return;
            }

            double currentBalance = rs.getDouble("balance");

            // Step 2: Check sufficient balance
            if (withdrawAmount > currentBalance) {
                response.getWriter().println("Insufficient balance.");
                return;
            }

            // Step 3: Update balance
            String updateSql =
                "UPDATE users SET balance = balance - ? WHERE email = ?";
            PreparedStatement updatePs = con.prepareStatement(updateSql);
            updatePs.setDouble(1, withdrawAmount);
            updatePs.setString(2, email);

            int rows = updatePs.executeUpdate();

            if (rows > 0) {
                response.getWriter().println(
                    "<h3>Withdrawal successful!</h3>" +
                    "<p>Remaining Balance: ₹" +
                    (currentBalance - withdrawAmount) + "</p>"
                );
            } else {
                response.getWriter().println("Withdrawal failed.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Error during withdrawal.");
        }
    }
}
