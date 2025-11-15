package infrastructure.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import application.ports.out.ManageProduct.ProductRepository;
import usecase.ManageProduct.ProductData;

public class ProductRepositoryImpl implements ProductRepository{

	@Override
	public ProductData findByName(String name) {
		String sql = "SELECT * FROM product WHERE name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProductData(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi CSDL khi tìm product theo tên.", e);
        }
        return null;
    }

	@Override
	public ProductData findById(int id) {
		String sql = "SELECT * FROM product WHERE id = ?";
		
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProductData(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi CSDL khi tìm product theo ID.", e);
        }
        return null;
	}

	@Override
	public List<ProductData> findAll() {
		String sql = "SELECT * FROM product";
        List<ProductData> products = new ArrayList<>(); // Khởi tạo list
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                // Map (ánh xạ) mỗi hàng sang DTO Tầng 3
                products.add(mapResultSetToProductData(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi CSDL khi tải danh sách product.", e);
        }
        return products;
	}

	@Override
	public ProductData save(ProductData productData) {
		// (Dùng 'Single Table Inheritance', INSERT tất cả các cột)
        String sql = "INSERT INTO product (category_id, name, description, price, stock_quantity, image_url, " +
                     "cpu, ram, screen_size, connection_type, dpi, switch_type, layout) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Set các trường chung
            pstmt.setInt(1, productData.categoryId);
            pstmt.setString(2, productData.name);
            pstmt.setString(3, productData.description);
            pstmt.setDouble(4, productData.price);
            pstmt.setInt(5, productData.stockQuantity);
            pstmt.setString(6, productData.imageUrl);
            
            // Set các trường riêng (sẽ tự động là NULL nếu không có)
            pstmt.setString(7, productData.cpu);
            pstmt.setInt(8, productData.ram);
            pstmt.setString(9, productData.screenSize);
            pstmt.setString(10, productData.connectionType);
            pstmt.setInt(11, productData.dpi);
            pstmt.setString(12, productData.switchType);
            pstmt.setString(13, productData.layout);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newId = generatedKeys.getInt(1);
                        // Cập nhật ID cho DTO và trả về
                        productData.id = newId; 
                        return productData;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi CSDL khi lưu product mới.", e);
        }
        return null;
	}

	@Override
	public ProductData update(ProductData productData) {
		String sql = "UPDATE product SET category_id = ?, name = ?, description = ?, price = ?, " +
                "stock_quantity = ?, image_url = ?, cpu = ?, ram = ?, screen_size = ?, " +
                "connection_type = ?, dpi = ?, switch_type = ?, layout = ? " +
                "WHERE id = ?";
   
	   try (Connection conn = DatabaseConnection.getConnection();
	        PreparedStatement pstmt = conn.prepareStatement(sql)) {
	       
	       pstmt.setInt(1, productData.categoryId);
	       pstmt.setString(2, productData.name);
	       pstmt.setString(3, productData.description);
	       pstmt.setDouble(4, productData.price);
	       pstmt.setInt(5, productData.stockQuantity);
	       pstmt.setString(6, productData.imageUrl);
	       
	       pstmt.setString(7, productData.cpu);
	       pstmt.setInt(8, productData.ram);
	       pstmt.setString(9, productData.screenSize);
	       pstmt.setString(10, productData.connectionType);
	       pstmt.setInt(11, productData.dpi);
	       pstmt.setString(12, productData.switchType);
	       pstmt.setString(13, productData.layout);
	       
	       pstmt.setInt(14, productData.id); // ID cho mệnh đề WHERE
	
	       int affectedRows = pstmt.executeUpdate();
	       if (affectedRows > 0) {
	           return productData; // Trả về DTO đã cập nhật
	       }
	   } catch (SQLException e) {
	       e.printStackTrace();
	       throw new RuntimeException("Lỗi CSDL khi cập nhật product.", e);
	   }
	   return null;
	}

	@Override
	public void deleteById(int id) {
		String sql = "DELETE FROM product WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi CSDL khi xóa product.", e);
        }
	}

	@Override
	public int countByCategoryId(int categoryId) {
		String sql = "SELECT COUNT(*) FROM product WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi CSDL khi đếm product.", e);
        }
        return 0;
	}

	@Override
	public List<ProductData> searchByName(String keyword) {
		String sql = "SELECT * FROM product WHERE name LIKE ?";
        List<ProductData> products = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + keyword + "%"); // SQL LIKE
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProductData(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi CSDL khi tìm kiếm product.", e);
        }
        return products;
	}
	
	private ProductData mapResultSetToProductData(ResultSet rs) throws SQLException {
		ProductData data = new ProductData();
		
        data.id = rs.getInt("id");
        data.categoryId = rs.getInt("category_id");
        data.name = rs.getString("name");
        data.description = rs.getString("description");
        data.price = rs.getDouble("price");
        data.stockQuantity = rs.getInt("stock_quantity");
        data.imageUrl = rs.getString("image_url");
        
        // Thuộc tính riêng (sẽ là NULL/0 nếu không khớp)
        data.cpu = rs.getString("cpu");
        data.ram = rs.getInt("ram");
        data.screenSize = rs.getString("screen_size");
        data.connectionType = rs.getString("connection_type");
        data.dpi = rs.getInt("dpi");
        data.switchType = rs.getString("switch_type");
        data.layout = rs.getString("layout");
        
        return data;
	}
}
