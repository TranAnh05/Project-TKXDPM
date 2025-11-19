package Category.AddCategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.ManageCategory.AddNewCategory.AddCategoryPresenter;
import cgx.com.adapters.ManageCategory.AddNewCategory.AddCategoryViewModel;
import cgx.com.usecase.ManageCategory.AddNewCategory.AddCategoryResponseData;

import static org.junit.jupiter.api.Assertions.*;

public class AddCategoryPresenterTest {
    
    private AddCategoryPresenter presenter;
    private AddCategoryViewModel viewModel;
    private AddCategoryResponseData responseData;
    
    @BeforeEach
    void setUp() {
        viewModel = new AddCategoryViewModel();
        presenter = new AddCategoryPresenter(viewModel);
        responseData = new AddCategoryResponseData();
    }
    
    /**
     * Test kịch bản THÀNH CÔNG - Danh mục gốc (Root Category)
     * Kiểm tra xem parentCategoryId = null có được chuyển thành chuỗi "null" không.
     */
    @Test
    void test_present_success_rootCategory() {
        // ARRANGE
        responseData.success = true;
        responseData.message = "Thêm danh mục thành công.";
        responseData.categoryId = "cat-123";
        responseData.name = "Laptop";
        responseData.parentCategoryId = null; // Root category
        
        // ACT
        presenter.present(responseData);
        
        // ASSERT
        AddCategoryViewModel resultVM = presenter.getModel();
        
        assertEquals("true", resultVM.success);
        assertEquals("Thêm danh mục thành công.", resultVM.message);
        
        assertNotNull(resultVM.category);
        assertEquals("cat-123", resultVM.category.id);
        assertEquals("Laptop", resultVM.category.name);
        assertEquals("null", resultVM.category.parentId, "Parent ID null phải được chuyển thành chuỗi 'null'");
    }

    /**
     * Test kịch bản THÀNH CÔNG - Danh mục con (Child Category)
     */
    @Test
    void test_present_success_childCategory() {
        // ARRANGE
        responseData.success = true;
        responseData.message = "Thêm danh mục thành công.";
        responseData.categoryId = "cat-456";
        responseData.name = "Gaming Laptop";
        responseData.parentCategoryId = "cat-123"; // Child category
        
        // ACT
        presenter.present(responseData);
        
        // ASSERT
        AddCategoryViewModel resultVM = presenter.getModel();
        
        assertEquals("true", resultVM.success);
        
        assertNotNull(resultVM.category);
        assertEquals("cat-456", resultVM.category.id);
        assertEquals("Gaming Laptop", resultVM.category.name);
        assertEquals("cat-123", resultVM.category.parentId);
    }
    
    /**
     * Test kịch bản THẤT BẠI
     */
    @Test
    void test_present_failure() {
        // ARRANGE
        responseData.success = false;
        responseData.message = "Tên danh mục đã tồn tại.";
        // Các trường dữ liệu khác sẽ là null mặc định
        
        // ACT
        presenter.present(responseData);
        
        // ASSERT
        AddCategoryViewModel resultVM = presenter.getModel();
        
        assertEquals("false", resultVM.success);
        assertEquals("Tên danh mục đã tồn tại.", resultVM.message);
        
        // Quan trọng: DTO Category phải là null khi thất bại
        assertNull(resultVM.category, "Category ViewDTO phải là null khi thất bại");
    }
}