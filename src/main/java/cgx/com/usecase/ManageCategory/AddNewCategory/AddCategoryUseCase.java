package cgx.com.usecase.ManageCategory.AddNewCategory;

import cgx.com.Entities.Category;
import cgx.com.Entities.User;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageCategory.CategoryData;
import cgx.com.usecase.ManageCategory.ICategoryIdGenerator;
import cgx.com.usecase.ManageCategory.ICategoryRepository;
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IUserRepository;

public class AddCategoryUseCase implements AddCategoryInputBoundary{
	private final ICategoryRepository categoryRepository;
    private final IAuthTokenValidator tokenValidator;
    private final IUserRepository userRepository;
    private final ICategoryIdGenerator idGenerator;
    private final AddCategoryOutputBoundary outputBoundary;

    public AddCategoryUseCase(ICategoryRepository categoryRepository,
                              IAuthTokenValidator tokenValidator,
                              IUserRepository userRepository,
                              ICategoryIdGenerator idGenerator,
                              AddCategoryOutputBoundary outputBoundary) {
        this.categoryRepository = categoryRepository;
        this.tokenValidator = tokenValidator;
        this.userRepository = userRepository;
        this.idGenerator = idGenerator;
        this.outputBoundary = outputBoundary;
    }

		
	@Override
	public void execute(AddCategoryRequestData input) {
		AddCategoryResponseData output = new AddCategoryResponseData();

        try {
            AuthPrincipal principal = tokenValidator.validate(input.authToken);
            User.validateIsAdmin(principal.role);

            Category.validateName(input.name);

            if (categoryRepository.findByName(input.name) != null) {
                throw new IllegalArgumentException("Tên danh mục đã tồn tại: " + input.name);
            }

            if (input.parentCategoryId != null && !input.parentCategoryId.trim().isEmpty()) {
                if (categoryRepository.findById(input.parentCategoryId) == null) {
                    throw new IllegalArgumentException("Danh mục cha không tồn tại.");
                }
            }

            String newId = idGenerator.generate();
            String parentId = (input.parentCategoryId != null && input.parentCategoryId.trim().isEmpty()) 
                              ? null : input.parentCategoryId;
            
            Category newCategory = new Category(newId, input.name, input.description, parentId);

            CategoryData dataToSave = mapEntityToData(newCategory);
            categoryRepository.save(dataToSave);

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
            e.printStackTrace();
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
