package cgx.com.infrastructure.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cgx.com.adapters.ManageCategory.AddNewCategory.AddCategoryPresenter;
import cgx.com.adapters.ManageCategory.AddNewCategory.AddCategoryViewModel;
import cgx.com.adapters.ManageCategory.DeleteCategory.DeleteCategoryPresenter;
import cgx.com.adapters.ManageCategory.DeleteCategory.DeleteCategoryViewModel;
import cgx.com.adapters.ManageCategory.UpdateCategory.UpdateCategoryPresenter;
import cgx.com.adapters.ManageCategory.UpdateCategory.UpdateCategoryViewModel;
import cgx.com.adapters.ManageCategory.ViewAllCategories.ViewAllCategoriesPresenter;
import cgx.com.adapters.ManageCategory.ViewAllCategories.ViewAllCategoriesViewModel;
import cgx.com.usecase.ManageCategory.AddNewCategory.AddCategoryInputBoundary;
import cgx.com.usecase.ManageCategory.AddNewCategory.AddCategoryRequestData;
import cgx.com.usecase.ManageCategory.DeleteCategory.DeleteCategoryInputBoundary;
import cgx.com.usecase.ManageCategory.DeleteCategory.DeleteCategoryRequestData;
import cgx.com.usecase.ManageCategory.UpdateCategory.UpdateCategoryInputBoundary;
import cgx.com.usecase.ManageCategory.UpdateCategory.UpdateCategoryRequestData;
import cgx.com.usecase.ManageCategory.ViewAllCategories.ViewAllCategoriesInputBoundary;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final AddCategoryInputBoundary addCategoryUseCase;
    private final AddCategoryPresenter addCategoryPresenter;
    
    private final UpdateCategoryInputBoundary updateCategoryUseCase;
    private final UpdateCategoryPresenter updateCategoryPresenter;
    
    private final DeleteCategoryInputBoundary deleteCategoryUseCase;
    private final DeleteCategoryPresenter deleteCategoryPresenter;
    
    private final ViewAllCategoriesInputBoundary viewAllUseCase;
    private final ViewAllCategoriesPresenter viewAllPresenter;

    public CategoryController(
            AddCategoryInputBoundary addCategoryUseCase, AddCategoryPresenter addCategoryPresenter,
            UpdateCategoryInputBoundary updateCategoryUseCase, UpdateCategoryPresenter updateCategoryPresenter,
            DeleteCategoryInputBoundary deleteCategoryUseCase, DeleteCategoryPresenter deleteCategoryPresenter,
            ViewAllCategoriesInputBoundary viewAllUseCase, ViewAllCategoriesPresenter viewAllPresenter) {
        this.addCategoryUseCase = addCategoryUseCase;
        this.addCategoryPresenter = addCategoryPresenter;
        this.updateCategoryUseCase = updateCategoryUseCase;
        this.updateCategoryPresenter = updateCategoryPresenter;
        this.deleteCategoryUseCase = deleteCategoryUseCase;
        this.deleteCategoryPresenter = deleteCategoryPresenter;
        this.viewAllUseCase = viewAllUseCase;
        this.viewAllPresenter = viewAllPresenter;
    }

    // 1. Xem tất cả (Public - Không cần token)
    @GetMapping
    public ResponseEntity<ViewAllCategoriesViewModel> getAllCategories() {
        viewAllUseCase.execute();
        return ResponseEntity.ok(viewAllPresenter.getModel());
    }

    // 2. Thêm mới (Admin)
    @PostMapping
    public ResponseEntity<AddCategoryViewModel> addCategory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody AddCategoryRequestJson request
    ) {
        String token = authHeader != null ? authHeader : "";
        AddCategoryRequestData input = new AddCategoryRequestData(
            token, request.name, request.description, request.parentCategoryId
        );
        addCategoryUseCase.execute(input);
        return ResponseEntity.ok(addCategoryPresenter.getModel());
    }

    // 3. Cập nhật (Admin)
    @PutMapping("/{id}")
    public ResponseEntity<UpdateCategoryViewModel> updateCategory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @RequestBody UpdateCategoryRequestJson request
    ) {
        String token = authHeader != null ? authHeader : "";
        UpdateCategoryRequestData input = new UpdateCategoryRequestData(
            token, id, request.name, request.description, request.parentCategoryId
        );
        updateCategoryUseCase.execute(input);
        return ResponseEntity.ok(updateCategoryPresenter.getModel());
    }

    // 4. Xóa (Admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteCategoryViewModel> deleteCategory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id
    ) {
        String token = authHeader != null ? authHeader : "";
        deleteCategoryUseCase.execute(new DeleteCategoryRequestData(token, id));
        return ResponseEntity.ok(deleteCategoryPresenter.getModel());
    }

    // DTOs
    public static class AddCategoryRequestJson {
        public String name;
        public String description;
        public String parentCategoryId;
    }
    public static class UpdateCategoryRequestJson {
        public String name;
        public String description;
        public String parentCategoryId;
    }
}