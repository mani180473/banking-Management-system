// src/main/java/web/BalanceServlet.java
package web;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import util.DBConnection;

public class BalanceServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("email") == null) {
            response.getWriter().println("Please login first.");
            return;
        }

        String email = (String) session.getAttribute("email");

        try (Connection con = DBConnection.getConnection()) {

            String sql = "SELECT balance FROM users WHERE email = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                double balance = rs.getDouble("balance");
                response.getWriter().println(
                    "<h2>Your Current Balance: ₹" + balance + "</h2>"
                );
            } else {
                response.getWriter().println("User not found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Error fetching balance.");
        }
    }
}
