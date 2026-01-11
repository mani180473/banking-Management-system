// src/main/java/web/RegisterServlet.java
package web;

import util.DBConnection;
import util.PasswordUtils;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.security.MessageDigest;

public class RegisterServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (name == null || name.isBlank() || email == null || email.isBlank() || password == null || password.isBlank()) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().println("Missing required fields");
            return;
        }

        String sql = "INSERT INTO users (name, email, balance, role, password) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setDouble(3, 0.0);
            ps.setString(4, "user");
            ps.setString(5, PasswordUtils.hashBcrypt(password));

            ps.executeUpdate();
            res.sendRedirect("login.html");

        } catch (SQLIntegrityConstraintViolationException dup) {
            res.setStatus(HttpServletResponse.SC_CONFLICT);
            res.getWriter().println("Email already registered");
        } catch (Exception e) {
            e.printStackTrace();
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().println("Registration failed");
        }
    }

    private String hash(String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
