package usecase.ManageCategory.AddNewCategory;

import Entities.Category;
import Entities.UserRole;
import usecase.ManageCategory.CategoryData;
import usecase.ManageCategory.ICategoryIdGenerator;
import usecase.ManageCategory.ICategoryRepository;
import usecase.ManageUser.IAuthTokenValidator;
import usecase.ManageUser.ViewUserProfile.AuthPrincipal;

public class AddCategoryUseCase implements AddCategoryInputBoundary{
	private final ICategoryRepository categoryRepository;
    private final IAuthTokenValidator tokenValidator; // Tái sử dụng từ User Management
    private final ICategoryIdGenerator idGenerator;
    private final AddCategoryOutputBoundary outputBoundary;

    public AddCategoryUseCase(ICategoryRepository categoryRepository,
                              IAuthTokenValidator tokenValidator,
                              ICategoryIdGenerator idGenerator,
                              AddCategoryOutputBoundary outputBoundary) {
        this.categoryRepository = categoryRepository;
        this.tokenValidator = tokenValidator;
        this.idGenerator = idGenerator;
        this.outputBoundary = outputBoundary;
    }

		
	@Override
	public void execute(AddCategoryRequestData input) {
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

            // 2. Validate Input (Dùng Entity)
            Category.validateName(input.name);

            // 3. Kiểm tra trùng tên (Quy tắc nghiệp vụ)
            if (categoryRepository.findByName(input.name) != null) {
                throw new IllegalArgumentException("Tên danh mục đã tồn tại: " + input.name);
            }

            // 4. Kiểm tra Parent Category (Nếu có)
            if (input.parentCategoryId != null && !input.parentCategoryId.trim().isEmpty()) {
                if (categoryRepository.findById(input.parentCategoryId) == null) {
                    throw new IllegalArgumentException("Danh mục cha không tồn tại.");
                }
            }

            // 5. Tạo Entity
            String newId = idGenerator.generate();
            // Xử lý chuỗi rỗng cho parentId thành null cho sạch dữ liệu
            String parentId = (input.parentCategoryId != null && input.parentCategoryId.trim().isEmpty()) 
                              ? null : input.parentCategoryId;
            
            Category newCategory = Category.create(newId, input.name, input.description, parentId);

            // 6. Map sang DTO và Lưu
            CategoryData dataToSave = mapEntityToData(newCategory);
            categoryRepository.save(dataToSave);

            // 7. Báo cáo thành công
            output.success = true;
            output.message = "Thêm danh mục thành công.";
            output.categoryId = newCategory.getCategoryId();
            output.name = newCategory.getName();
            output.parentCategoryId = newCategory.getParentCategoryId();

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
