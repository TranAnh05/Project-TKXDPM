package infrastructure.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import Entities.UserRole;
import application.ports.out.ManageUser.UserRepository;
import usecase.ManageUser.UserData;

public class UserRepositoryImpl implements UserRepository{

	/**
     * Hiện thực hóa hàm findAll (Lát cắt 10)
     */
    @Override
    public List<UserData> findAll() {
        String sql = "SELECT * FROM user";
        List<UserData> users = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(mapResultSetToUserData(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi CSDL khi tải danh sách user.", e);
        }
        return users;
    }

    /**
     * Hiện thực hóa hàm searchByEmail (Lát cắt 17)
     */
    @Override
    public List<UserData> searchByEmail(String emailKeyword) {
        String sql = "SELECT * FROM user WHERE email LIKE ?";
        List<UserData> users = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + emailKeyword + "%"); // SQL LIKE
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUserData(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi CSDL khi tìm kiếm user.", e);
        }
        return users;
    }

    /**
     * Hiện thực hóa hàm findById (Lát cắt 11, 12, 13)
     */
    @Override
    public UserData findById(int id) {
    	String sql = "SELECT * FROM user WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUserData(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi CSDL khi tìm user theo ID.", e);
        }
        return null; // Không tìm thấy
    }

    /**
     * Hiện thực hóa hàm findByEmail (Dùng cho logic "Đăng ký")
     */
    @Override
    public UserData findByEmail(String email) {
        String sql = "SELECT * FROM user WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUserData(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi CSDL khi tìm user theo email.", e);
        }
        return null;
    }

    /**
     * Hiện thực hóa hàm update (Lát cắt 11, 12, 13)
     */
    @Override
    public UserData update(UserData userData) {
    	String sql = "UPDATE user SET email = ?, password_hash = ?, full_name = ?, " +
                "address = ?, role = ?, is_blocked = ? WHERE id = ?";
   
   try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
       
       pstmt.setString(1, userData.email);
       pstmt.setString(2, userData.passwordHash);
       pstmt.setString(3, userData.fullName);
       pstmt.setString(4, userData.address);
       pstmt.setString(5, userData.role.name()); // Enum -> String
       pstmt.setBoolean(6, userData.isBlocked); // boolean -> TINYINT(1)
       pstmt.setInt(7, userData.id); // WHERE

       int affectedRows = pstmt.executeUpdate();
       if (affectedRows > 0) {
           return userData; // Trả về DTO đã cập nhật
       }
   } catch (SQLException e) {
       e.printStackTrace();
       throw new RuntimeException("Lỗi CSDL khi cập nhật user.", e);
   }
   return null; // Cập nhật thất bại
    }

    /**
     * Hiện thực hóa hàm save (Dùng cho logic "Đăng ký")
     */
    @Override
    public UserData save(UserData userData) {
        String sql = "INSERT INTO user (email, password_hash, full_name, address, role, is_blocked) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, userData.email);
            pstmt.setString(2, userData.passwordHash);
            pstmt.setString(3, userData.fullName);
            pstmt.setString(4, userData.address);
            pstmt.setString(5, userData.role.name());
            pstmt.setBoolean(6, userData.isBlocked);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newId = generatedKeys.getInt(1);
                        userData.id = newId; 
                        return userData;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi CSDL khi lưu user mới.", e);
        }
        return null;
    }
    
    // --- Hàm Helper (Ánh xạ ResultSet) ---
    private UserData mapResultSetToUserData(ResultSet rs) throws SQLException {
        return new UserData(
            rs.getInt("id"),
            rs.getString("email"),
            rs.getString("password_hash"),
            rs.getString("full_name"),
            rs.getString("address"),
            UserRole.valueOf(rs.getString("role")), // String -> Enum
            rs.getBoolean("is_blocked") // TINYINT(1) -> boolean
        );
    }
}
