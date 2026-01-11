// src/main/java/web/BalanceServlet.java
package web;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import util.DBConnection;

public class DepositServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("email") == null) {
            response.getWriter().println("Please login first.");
            return;
        }

        String email = (String) session.getAttribute("email");
        double depositAmount = Double.parseDouble(request.getParameter("amount"));

        try (Connection con = DBConnection.getConnection()) {

            String sql =
                "UPDATE users SET balance = balance + ? WHERE email = ?";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setDouble(1, depositAmount);
            ps.setString(2, email);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                response.getWriter().println(
                    "<h3>Deposit successful!</h3>" +
                    "<p>Deposited Amount: ₹" + depositAmount + "</p>"
                );
            } else {
                response.getWriter().println(
                    "Deposit failed. User not found."
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Error during deposit.");
        }
    }
}

