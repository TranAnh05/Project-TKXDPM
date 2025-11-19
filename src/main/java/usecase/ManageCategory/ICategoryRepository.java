package usecase.ManageCategory;

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
}
