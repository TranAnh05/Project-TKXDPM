package infrastructure.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import application.dtos.ManageOrder.OrderData;
import application.ports.out.ManageOrder.OrderRepository;
import domain.entities.OrderStatus;

public class OrderRepositoryImpl implements OrderRepository{

	/**
     * Hiện thực hóa hàm findAll (Lát cắt 14)
     */
    @Override
    public List<OrderData> findAll() {
        // (Chúng ta dùng 'customer_order' vì 'order' là từ khóa SQL)
        String sql = "SELECT * FROM customer_order ORDER BY order_date DESC";
        List<OrderData> orders = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                orders.add(mapResultSetToOrderData(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi CSDL khi tải danh sách order.", e);
        }
        return orders;
    }

    /**
     * Hiện thực hóa hàm isProductInAnyOrder (Dùng cho Lát cắt 9)
     */
    @Override
    public boolean isProductInAnyOrder(int productId) {
        String sql = "SELECT 1 FROM order_detail WHERE product_id = ? LIMIT 1";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return true; // Tìm thấy
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi CSDL khi kiểm tra product trong order_detail.", e);
        }
        return false; // Không tìm thấy
    }

    /**
     * Hiện thực hóa hàm findById (Lát cắt 15)
     */
    @Override
    public OrderData findById(int id) {
        String sql = "SELECT * FROM customer_order WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrderData(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi CSDL khi tìm order theo ID.", e);
        }
        return null;
    }

    /**
     * Hiện thực hóa hàm update (Lát cắt 15)
     */
    @Override
    public OrderData update(OrderData orderData) {
        String sql = "UPDATE customer_order SET user_id = ?, total_amount = ?, status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderData.userId);
            pstmt.setDouble(2, orderData.totalAmount);
            pstmt.setString(3, orderData.status.name()); // Enum -> String
            pstmt.setInt(4, orderData.id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                return orderData;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi CSDL khi cập nhật order.", e);
        }
        return null;
    }
    
    /**
     * Hiện thực hóa hàm findAllByUserIds (Lát cắt 18)
     */
    @Override
    public List<OrderData> findAllByUserIds(List<Integer> userIds) {
        List<OrderData> orders = new ArrayList<>();
        if (userIds == null || userIds.isEmpty()) {
            return orders;
        }
        
        // Xây dựng câu SQL "IN (?, ?, ?)"
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM customer_order WHERE user_id IN (");
        for (int i = 0; i < userIds.size(); i++) {
            sqlBuilder.append("?");
            if (i < userIds.size() - 1) {
                sqlBuilder.append(",");
            }
        }
        sqlBuilder.append(") ORDER BY order_date DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {
            
            for (int i = 0; i < userIds.size(); i++) {
                pstmt.setInt(i + 1, userIds.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrderData(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi CSDL khi tìm order theo User IDs.", e);
        }
        return orders;
    }

    // (Hàm 'save' chưa cần cho Admin, sẽ làm khi có UseCase "Checkout" của Customer)
    @Override
    public OrderData save(OrderData orderData) { /* TODO */ return null; }

    
    // --- Hàm Helper (Ánh xạ ResultSet) ---
    private OrderData mapResultSetToOrderData(ResultSet rs) throws SQLException {
        return new OrderData(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getTimestamp("order_date").toLocalDateTime(), // DATETIME -> LocalDateTime
            rs.getDouble("total_amount"),
            OrderStatus.valueOf(rs.getString("status")) // String -> Enum
        );
    }
}
