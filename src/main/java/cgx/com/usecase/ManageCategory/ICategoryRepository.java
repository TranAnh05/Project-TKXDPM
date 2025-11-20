package cgx.com.usecase.ManageCategory;

import java.util.List;

public interface ICategoryRepository {
	 /**
     * Lưu một danh mục mới (hoặc cập nhật).
     */
    void save(CategoryData categoryData);

    /**
     * Tìm danh mục theo Tên (để kiểm tra trùng lặp).
     */
    CategoryData findByName(String name);

    /**
     * Tìm danh mục theo ID (để kiểm tra Parent ID có tồn tại không).
     */
    CategoryData findById(String id);

    /**
     * Kiểm tra xem danh mục có danh mục con không.
     * @param parentId ID của danh mục cần kiểm tra.
     * @return true nếu có con, false nếu không.
     */
    boolean hasChildren(String parentId);

    /**
     * Kiểm tra xem danh mục có chứa sản phẩm nào không.
     * (Trong thực tế, implementation của hàm này sẽ query bảng Products).
     * @param categoryId ID của danh mục cần kiểm tra.
     * @return true nếu có sản phẩm, false nếu không.
     */
    boolean hasProducts(String categoryId);

    /**
     * Xóa danh mục khỏi CSDL.
     */
    void delete(String id);
    
    /**
     * Lấy tất cả danh mục.
     * @return Danh sách tất cả CategoryData.
     */
    List<CategoryData> findAll();
}
