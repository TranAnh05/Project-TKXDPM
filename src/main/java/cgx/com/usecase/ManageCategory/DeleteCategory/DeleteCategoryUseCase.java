package cgx.com.usecase.ManageCategory.DeleteCategory;

import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageCategory.CategoryData;
import cgx.com.usecase.ManageCategory.ICategoryRepository;
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;

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
            // 1. Validate Token & Quyền Admin
            if (input.authToken == null || input.authToken.trim().isEmpty()) {
                throw new SecurityException("Auth Token không được để trống.");
            }
            AuthPrincipal principal = tokenValidator.validate(input.authToken);
            if (principal.role != UserRole.ADMIN) {
                throw new SecurityException("Không có quyền truy cập (Yêu cầu Admin).");
            }

            // 2. Validate Input
            if (input.categoryId == null || input.categoryId.trim().isEmpty()) {
                throw new IllegalArgumentException("ID danh mục không được để trống.");
            }

            // 3. Kiểm tra danh mục có tồn tại không
            CategoryData existingData = categoryRepository.findById(input.categoryId);
            if (existingData == null) {
                throw new IllegalArgumentException("Không tìm thấy danh mục với ID: " + input.categoryId);
            }

            // 4. Quy tắc nghiệp vụ: Không được xóa nếu có danh mục CON
            if (categoryRepository.hasChildren(input.categoryId)) {
                throw new IllegalArgumentException("Không thể xóa danh mục này vì nó đang chứa các danh mục con.");
            }

            // 5. Quy tắc nghiệp vụ: Không được xóa nếu đang chứa SẢN PHẨM
            if (categoryRepository.hasProducts(input.categoryId)) {
                throw new IllegalArgumentException("Không thể xóa danh mục này vì nó đang chứa sản phẩm.");
            }

            // 6. Thực hiện xóa
            categoryRepository.delete(input.categoryId);

            // 7. Báo cáo thành công
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
