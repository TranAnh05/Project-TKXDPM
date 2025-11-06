package infrastructure.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * LỚP 1: HELPER KẾT NỐI (Tầng 1)
 * Lớp tiện ích để lấy kết nối JDBC đến MySQL.
 */
public class DatabaseConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/managedevicecomputer";
    private static final String USER = "root";
    private static final String PASS = "130405";

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // 2. Lấy kết nối
            return DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Lỗi nghiêm trọng: Không thể kết nối CSDL!", e);
        }
    }
}
