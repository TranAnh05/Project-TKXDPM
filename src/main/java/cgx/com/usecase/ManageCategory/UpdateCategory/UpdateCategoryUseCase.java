package cgx.com.usecase.ManageCategory.UpdateCategory;

import java.time.Instant;

import cgx.com.Entities.Category;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageCategory.CategoryData;
import cgx.com.usecase.ManageCategory.ICategoryRepository;
import cgx.com.usecase.ManageCategory.AddNewCategory.AddCategoryResponseData;
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;

public class UpdateCategoryUseCase implements UpdateCategoryInputBoundary{
	private final ICategoryRepository categoryRepository;
    private final IAuthTokenValidator tokenValidator;
    private final UpdateCategoryOutputBoundary outputBoundary;
	
    public UpdateCategoryUseCase(ICategoryRepository categoryRepository,
            IAuthTokenValidator tokenValidator,
            UpdateCategoryOutputBoundary outputBoundary) {
		this.categoryRepository = categoryRepository;
		this.tokenValidator = tokenValidator;
		this.outputBoundary = outputBoundary;
	}
    
    @Override
	public void execute(UpdateCategoryRequestData input) {
    	// Tái sử dụng DTO Response
        AddCategoryResponseData output = new AddCategoryResponseData();

        try {
            // 1. Validate Token & Quyền Admin
            if (input.authToken == null || input.authToken.trim().isEmpty()) {
                throw new SecurityException("Auth Token không được để trống.");
            }
            AuthPrincipal principal = tokenValidator.validate(input.authToken);
            if (principal.role != UserRole.ADMIN) {
                throw new SecurityException("Không có quyền truy cập (Yêu cầu Admin).");
            }

            // 2. Kiểm tra danh mục mục tiêu có tồn tại không
            if (input.categoryId == null || input.categoryId.trim().isEmpty()) {
                throw new IllegalArgumentException("ID danh mục không được để trống.");
            }
            CategoryData existingData = categoryRepository.findById(input.categoryId);
            if (existingData == null) {
                throw new IllegalArgumentException("Không tìm thấy danh mục với ID: " + input.categoryId);
            }

            // 3. Tái tạo Entity từ DTO (Re-hydrate)
            Category categoryEntity = new Category(
                existingData.categoryId,
                existingData.name,
                existingData.description,
                existingData.parentCategoryId,
                existingData.createdAt,
                existingData.updatedAt
            );

            // 4. Validate Tên mới (Logic Entity)
            Category.validateName(input.name);

            // 5. Kiểm tra trùng tên (Nếu tên thay đổi)
            if (!input.name.equalsIgnoreCase(categoryEntity.getName())) {
                // Tên đã thay đổi -> Kiểm tra xem tên mới đã có ai dùng chưa
                if (categoryRepository.findByName(input.name) != null) {
                    throw new IllegalArgumentException("Tên danh mục mới đã tồn tại: " + input.name);
                }
            }

            // 6. Kiểm tra Parent Category
            String newParentId = (input.parentCategoryId != null && input.parentCategoryId.trim().isEmpty()) 
                                 ? null : input.parentCategoryId;

            if (newParentId != null) {
                // Rule 1: Cha phải tồn tại
                if (categoryRepository.findById(newParentId) == null) {
                    throw new IllegalArgumentException("Danh mục cha không tồn tại.");
                }
                // Rule 2: Không được làm cha của chính mình (Vòng lặp đơn giản)
                if (newParentId.equals(categoryEntity.getCategoryId())) {
                    throw new IllegalArgumentException("Danh mục không thể là cha của chính nó.");
                }
            }

            // 7. Cập nhật Entity (Logic nghiệp vụ)
            // (Ở đây ta set trực tiếp vì Java không có hàm update sẵn trong Entity mẫu trước, 
            // nhưng đúng ra nên có hàm entity.updateDetails(...))
            Category updatedEntity = new Category(
                categoryEntity.getCategoryId(),
                input.name,
                input.description,
                newParentId,
                categoryEntity.getCreatedAt(),
                Instant.now() // Update thời gian
            );

            // 8. Lưu xuống CSDL
            CategoryData dataToSave = mapEntityToData(updatedEntity);
            categoryRepository.save(dataToSave);

            // 9. Báo cáo thành công
            output.success = true;
            output.message = "Cập nhật danh mục thành công.";
            output.categoryId = updatedEntity.getCategoryId();
            output.name = updatedEntity.getName();
            output.parentCategoryId = updatedEntity.getParentCategoryId();

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

	private CategoryData mapEntityToData(Category entity) {
		return new CategoryData(
	            entity.getCategoryId(),
	            entity.getName(),
	            entity.getDescription(),
	            entity.getParentCategoryId(),
	            entity.getCreatedAt(),
	            entity.getUpdatedAt()
	        );
	}

}
