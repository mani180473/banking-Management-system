// src/main/java/web/LoginServlet.java
package web;

import util.DBConnection;
import util.PasswordUtils;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;

public class LoginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        String sql = "SELECT id, role, password FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String role = rs.getString("role");
                    String stored = rs.getString("password");

                    if (PasswordUtils.verify(password, stored)) {
                        // If stored password is not bcrypt, migrate it to bcrypt
                        if (!PasswordUtils.isBcrypt(stored)) {
                            String newHash = PasswordUtils.hashBcrypt(password);
                            String upd = "UPDATE users SET password = ? WHERE id = ?";
                            try (PreparedStatement ups = conn.prepareStatement(upd)) {
                                ups.setString(1, newHash);
                                ups.setInt(2, id);
                                ups.executeUpdate();
                            } catch (Exception ignore) {
                                // don't block login on migration failure
                            }
                        }

                        HttpSession session = req.getSession();
                        session.setAttribute("userId", id);
                        session.setAttribute("role", role);
                        session.setAttribute("email", email);
                        res.sendRedirect("dashboard.html");
                        return;
                    }
                }
                res.getWriter().println("Invalid credentials");
            }

        } catch (Exception e) {
            e.printStackTrace();
            res.getWriter().println("Login error");
        }
    }

    
}
