package cgx.com.usecase.ManageCategory.DeleteCategory;

import cgx.com.Entities.Category;
import cgx.com.Entities.User;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageCategory.CategoryData;
import cgx.com.usecase.ManageCategory.ICategoryRepository;
import cgx.com.usecase.ManageUser.IUserRepository;

public class DeleteCategoryUseCase implements DeleteCategoryInputBoundary{
	private final ICategoryRepository categoryRepository;
    private final IAuthTokenValidator tokenValidator;
    private final DeleteCategoryOutputBoundary outputBoundary;

    public DeleteCategoryUseCase(ICategoryRepository categoryRepository,
                                 IAuthTokenValidator tokenValidator,
                                 DeleteCategoryOutputBoundary outputBoundary) {
        this.categoryRepository = categoryRepository;
        this.tokenValidator = tokenValidator;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(DeleteCategoryRequestData input) {
        DeleteCategoryResponseData output = new DeleteCategoryResponseData();

        try {
            AuthPrincipal principal = tokenValidator.validate(input.authToken);
            User.validateIsAdmin(principal.role);

            Category.validateID(input.categoryId);

            CategoryData existingData = categoryRepository.findById(input.categoryId);
            if (existingData == null) {
                throw new IllegalArgumentException("Không tìm thấy danh mục với ID: " + input.categoryId);
            }

            if (categoryRepository.hasChildren(input.categoryId)) {
                throw new IllegalArgumentException("Không thể xóa danh mục này vì nó đang chứa các danh mục con.");
            }

            if (categoryRepository.hasProducts(input.categoryId)) {
                throw new IllegalArgumentException("Không thể xóa danh mục này vì nó đang chứa sản phẩm.");
            }
            
            categoryRepository.delete(input.categoryId);

            output.success = true;
            output.message = "Xóa danh mục thành công.";
            output.deletedCategoryId = input.categoryId;

        } catch (IllegalArgumentException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (SecurityException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            output.success = false;
            output.message = "Lỗi hệ thống không xác định.";
        }

        outputBoundary.present(output);
    }
}
