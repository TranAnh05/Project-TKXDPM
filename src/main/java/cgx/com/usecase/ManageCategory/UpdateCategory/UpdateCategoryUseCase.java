package cgx.com.usecase.ManageCategory.UpdateCategory;

import java.time.Instant;


import cgx.com.Entities.Category;
import cgx.com.Entities.User;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageCategory.CategoryData;
import cgx.com.usecase.ManageCategory.ICategoryRepository;
import cgx.com.usecase.ManageCategory.AddNewCategory.AddCategoryResponseData;
import cgx.com.usecase.ManageUser.IUserRepository;

public class UpdateCategoryUseCase implements UpdateCategoryInputBoundary{
	private final ICategoryRepository categoryRepository;
    private final IAuthTokenValidator tokenValidator;
    private final IUserRepository userRepository;
    private final UpdateCategoryOutputBoundary outputBoundary;
	
    public UpdateCategoryUseCase(ICategoryRepository categoryRepository,
            IAuthTokenValidator tokenValidator,
            IUserRepository userRepository,
            UpdateCategoryOutputBoundary outputBoundary) {
		this.categoryRepository = categoryRepository;
		this.tokenValidator = tokenValidator;
		this.userRepository = userRepository;
		this.outputBoundary = outputBoundary;
	}
    
    @Override
	public void execute(UpdateCategoryRequestData input) {
    	// Tái sử dụng DTO Response
        AddCategoryResponseData output = new AddCategoryResponseData();

        try {
            AuthPrincipal principal = tokenValidator.validate(input.authToken);
            User.validateIsAdmin(principal.role);
            
            Category.validateID(input.categoryId);
            Category.validateName(input.name);
            
            CategoryData existingData = categoryRepository.findById(input.categoryId);
            if (existingData == null) {
                throw new IllegalArgumentException("Không tìm thấy danh mục với ID: " + input.categoryId);
            }

            Category categoryEntity = new Category(
                existingData.categoryId,
                existingData.name,
                existingData.description,
                existingData.parentCategoryId,
                existingData.createdAt,
                existingData.updatedAt
            );

            if (!input.name.equalsIgnoreCase(categoryEntity.getName())) {
                if (categoryRepository.findByName(input.name) != null) {
                    throw new IllegalArgumentException("Tên danh mục mới đã tồn tại: " + input.name);
                }
            }

            String newParentId = (input.parentCategoryId != null && input.parentCategoryId.trim().isEmpty()) 
                                 ? null : input.parentCategoryId;

            if (newParentId != null) {
                // Danh mục cha phải tồn tại
                if (categoryRepository.findById(newParentId) == null) {
                    throw new IllegalArgumentException("Danh mục cha không tồn tại.");
                }
                // Không được làm cha của chính mình
                categoryEntity.validateNotSelfParent(newParentId);
            }
            
            Category updatedEntity = new Category(
                categoryEntity.getCategoryId(),
                input.name,
                input.description,
                newParentId,
                categoryEntity.getCreatedAt(),
                Instant.now() 
            );

            CategoryData dataToSave = mapEntityToData(updatedEntity);
            categoryRepository.save(dataToSave);

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
