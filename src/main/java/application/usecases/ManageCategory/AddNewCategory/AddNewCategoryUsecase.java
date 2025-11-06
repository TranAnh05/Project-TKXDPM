package application.usecases.ManageCategory.AddNewCategory;

import application.dtos.ManageCategory.CategoryData;
import application.dtos.ManageCategory.CategoryOutputData;
import application.dtos.ManageCategory.AddNewCategory.AddNewCategoryInputData;
import application.dtos.ManageCategory.AddNewCategory.AddNewCategoryOutputData;
import application.ports.in.ManageCategory.AddNewCategory.AddNewCategoryInputBoundary;
import application.ports.out.ManageCategory.CategoryRepository;
import application.ports.out.ManageCategory.AddNewCategory.AddNewCategoryOutputBoundary;
import domain.entities.Category;

public class AddNewCategoryUsecase implements AddNewCategoryInputBoundary{
	private CategoryRepository categoryRepository;
	private AddNewCategoryOutputBoundary outBoundary;
	private AddNewCategoryOutputData outputData;
	
	public AddNewCategoryUsecase() {
		
	}
	
	public AddNewCategoryUsecase(CategoryRepository categoryRepository, AddNewCategoryOutputBoundary outBoundary) {
		this.categoryRepository = categoryRepository;
		this.outBoundary = outBoundary;
	}
	
	public AddNewCategoryOutputData getOutputData() {
		return outputData;
	}

	@Override
	public void execute(AddNewCategoryInputData input) {
		outputData = new AddNewCategoryOutputData();
		
		try {
			Category.isValidName(input.name);
            Category.isValidTemplate(input.attributeTemplate);
            
            if (categoryRepository.findByName(input.name) != null) {
                // Tự ném lỗi nghiệp vụ
                throw new IllegalArgumentException("Tên loại sản phẩm này đã tồn tại.");
            }
            
            Category categoryEntity = new Category(input.name, input.attributeTemplate);
            
            CategoryData dataToSave = mapEntityToData(categoryEntity);
            
            CategoryData savedData = categoryRepository.save(dataToSave);
            outputData.success = true;
            outputData.message = "Thêm mới thành công!";
            outputData.newCategory = mapDataToOutput(savedData);
            
		} catch (IllegalArgumentException e) {
            //  BẮT LỖI VALIDATION (T4) HOẶC LỖI NGHIỆP VỤ (T3)
            outputData.success = false;
            outputData.message = e.getMessage();
            
        }	catch (Exception e) {
        	//  Bắt lỗi hệ thống (CSDL sập, NullPointerException...)
            outputData.success = false;
            outputData.message = "Đã xảy ra lỗi hệ thống không xác định.";
		}
		
		outBoundary.present(outputData);
	}

	private CategoryOutputData mapDataToOutput(CategoryData data) {
		CategoryOutputData dto = new CategoryOutputData();
	    dto.id = data.id;
	    dto.name = data.name;
	    dto.attributeTemplate = data.attributeTemplate;
	    return dto;
	}

	private CategoryData mapEntityToData(Category entity) {
		return new CategoryData(0, entity.getName(), entity.getAttributeTemplate());
	}
	
}
