package Category.ViewAllCategories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.ManageCategory.ViewAllCategories.CategoryNodeViewDTO;
import cgx.com.adapters.ManageCategory.ViewAllCategories.ViewAllCategoriesPresenter;
import cgx.com.adapters.ManageCategory.ViewAllCategories.ViewAllCategoriesViewModel;
import cgx.com.usecase.ManageCategory.CategoryData;
import cgx.com.usecase.ManageCategory.ViewAllCategories.ViewAllCategoriesResponseData;

import java.time.Instant;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

public class ViewAllCategoriesPresenterTest {
    
    private ViewAllCategoriesPresenter presenter;
    private ViewAllCategoriesViewModel viewModel;
    
    @BeforeEach
    void setUp() {
        viewModel = new ViewAllCategoriesViewModel();
        presenter = new ViewAllCategoriesPresenter(viewModel);
    }
    
    @Test
    void test_present_success_treeStructure() {
        // ARRANGE: Tạo danh sách phẳng
        // Root 1
        //   |-- Child 1.1
        // Root 2
        CategoryData root1 = new CategoryData("1", "Root1", "", null, Instant.now(), Instant.now());
        CategoryData child1_1 = new CategoryData("2", "Child1.1", "", "1", Instant.now(), Instant.now());
        CategoryData root2 = new CategoryData("3", "Root2", "", null, Instant.now(), Instant.now());
        
        ViewAllCategoriesResponseData response = new ViewAllCategoriesResponseData();
        response.success = true;
        response.message = "OK";
        response.categories = Arrays.asList(root1, child1_1, root2);
        
        // ACT
        presenter.present(response);
        
        // ASSERT
        assertEquals("true", viewModel.success);
        assertNotNull(viewModel.categoryTree);
        assertEquals(2, viewModel.categoryTree.size(), "Phải có 2 node gốc");
        
        // Kiểm tra Root 1
        CategoryNodeViewDTO r1Node = viewModel.categoryTree.stream().filter(n -> n.id.equals("1")).findFirst().orElse(null);
        assertNotNull(r1Node);
        assertEquals("Root1", r1Node.name);
        assertEquals(1, r1Node.children.size(), "Root 1 phải có 1 con");
        assertEquals("Child1.1", r1Node.children.get(0).name);
        
        // Kiểm tra Root 2
        CategoryNodeViewDTO r2Node = viewModel.categoryTree.stream().filter(n -> n.id.equals("3")).findFirst().orElse(null);
        assertNotNull(r2Node);
        assertEquals(0, r2Node.children.size(), "Root 2 không có con");
    }
}