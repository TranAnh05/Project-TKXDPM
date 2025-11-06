package infrastructure.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;

import application.dtos.ManageCategory.CategoryData;
import application.ports.out.ManageCategory.CategoryRepository;

public class CategoryRepositoryImpl implements CategoryRepository{

	@Override
	public CategoryData findByName(String name) {
		String sql = "SELECT * FROM category WHERE name = ?";
        
        try (
    		Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Map ResultSet (SQL) -> CategoryData (T3 DTO)
                    return mapResultSetToCategoryData(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // (Nên log lỗi)
            // Ném lỗi hệ thống để UseCase (T3) bắt
            throw new RuntimeException("Lỗi CSDL khi tìm category theo tên.", e);
        }
        return null; // Không tìm thấy
	}

	private CategoryData mapResultSetToCategoryData(ResultSet rs) throws SQLException {
		return new CategoryData(
	            rs.getInt("id"),
	            rs.getString("name"),
	            rs.getString("attribute_template")
	        );
	}

	@Override
	public CategoryData save(CategoryData categoryData) {
		// (categoryData.id == 0 vì đây là "thêm mới")
        String sql = "INSERT INTO category (name, attribute_template) VALUES (?, ?)";
        
        try (
    		Connection conn = DatabaseConnection.getConnection();
            // Yêu cầu CSDL trả về ID tự động tăng (Generated Keys)
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, categoryData.name);
            pstmt.setString(2, categoryData.attributeTemplate);
            
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Lấy ID (key) mà CSDL vừa tạo ra
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newId = generatedKeys.getInt(1);
                        // Trả về DTO mới với ID thật từ CSDL
                        return new CategoryData(newId, categoryData.name, categoryData.attributeTemplate);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // (Nên log lỗi)
            throw new RuntimeException("Lỗi CSDL khi lưu category mới.", e);
        }
        return null; // Thêm mới thất bại
	}

	@Override
	public CategoryData findById(int id) {
		String sql = "SELECT * FROM category WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Map (ánh xạ) sang DTO Tầng 3
                    return mapResultSetToCategoryData(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi CSDL khi tìm category theo ID.", e);
        }
        
        return null; // Không tìm thấy
	}

	@Override
	public List<CategoryData> findAll() {
		String sql = "SELECT * FROM category";
        // Khởi tạo list rỗng ngay lập tức (để tránh lỗi Null)
        List<CategoryData> categories = new ArrayList<>(); 
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            // Lặp qua tất cả các hàng (rows)
            while (rs.next()) {
                // Map (ánh xạ) mỗi hàng sang DTO Tầng 3
                categories.add(mapResultSetToCategoryData(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Ném lỗi hệ thống để Tầng 1 (API) bắt
            throw new RuntimeException("Lỗi CSDL khi tải danh sách category.", e);
        }
        return categories; // Trả về danh sách (có thể rỗng)
	}

	@Override
	public CategoryData update(CategoryData categoryData) {
		String sql = "UPDATE category SET name = ?, attribute_template = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, categoryData.name);
            pstmt.setString(2, categoryData.attributeTemplate);
            pstmt.setInt(3, categoryData.id);
            
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Trả về DTO đã được cập nhật
                return categoryData;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi CSDL khi cập nhật category.", e);
        }
        return null; // Cập nhật thất bại
	}

	@Override
	public void deleteById(int id) {
String sql = "DELETE FROM category WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
            // Ném lỗi Runtime nếu vi phạm khóa ngoại (ví dụ: còn Product)
            // Khớp 100% với script CSDL (ON DELETE RESTRICT)
            throw new RuntimeException("Lỗi CSDL khi xóa category. Có thể do vi phạm khóa ngoại (còn sản phẩm).", e);
        }
	}

	@Override
	public int countProductsByCategoryId(int categoryId) {
		// (Chúng ta dùng 'product' vì đó là tên bảng trong CSDL)
        String sql = "SELECT COUNT(*) FROM product WHERE category_id = ?"; 
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, categoryId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1); // Lấy kết quả COUNT(*)
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi CSDL khi đếm sản phẩm.", e);
        }
        return 0;
	}

}
