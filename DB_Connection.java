package Hotel_Reservation_System;

import java.sql.Connection;
import java.sql.DriverManager;
import javax.swing.JOptionPane;

public class DB_Connection {
    public static Connection connect() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            String url = "jdbc:mysql://localhost:3307/hotel_db";
            conn = DriverManager.getConnection(url, "root", "");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Database Connection Error: " + e.getMessage());
        }
        return conn;
    }
}
