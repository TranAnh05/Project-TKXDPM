package application.usecases.ManageCategory.UpdateCategory;

import application.dtos.ManageCategory.CategoryData;
import application.dtos.ManageCategory.CategoryOutputData;
import application.dtos.ManageCategory.UpdateCategory.UpdateCategoryInputData;
import application.dtos.ManageCategory.UpdateCategory.UpdateCategoryOutputData;
import application.ports.in.ManageCategory.UpdateCategory.UpdateCategoryInputBoundary;
import application.ports.out.ManageCategory.CategoryRepository;
import application.ports.out.ManageCategory.UpdateCategory.UpdateCategoryOutputBoundary;
import domain.entities.Category;

public class UpdateCategoryUsecase implements UpdateCategoryInputBoundary{
	private CategoryRepository categoryRepository;
	private UpdateCategoryOutputBoundary outBoundary;
	private UpdateCategoryOutputData outputData;
	
	public UpdateCategoryUsecase() {
		
	}
	
	
	public UpdateCategoryUsecase(CategoryRepository categoryRepository, UpdateCategoryOutputBoundary outBoundary) {
		this.categoryRepository = categoryRepository;
		this.outBoundary = outBoundary;
	}
	
	
	public UpdateCategoryOutputData getOutputData() {
		return outputData;
	}

	@Override
	public void execute(UpdateCategoryInputData input) {
		outputData = new UpdateCategoryOutputData();
		try {
			Category.isValidName(input.name);
			Category.isValidTemplate(input.attributeTemplate);
			
			CategoryData existingByName = categoryRepository.findByName(input.name);
            if (existingByName != null && existingByName.id != input.id) {
                throw new IllegalArgumentException("Tên loại sản phẩm này đã tồn tại.");
            }
            
            CategoryData dataToUpdate = categoryRepository.findById(input.id);
            if (dataToUpdate == null) {
                throw new IllegalArgumentException("Không tìm thấy loại sản phẩm để cập nhật.");
            }
            
            Category categoryEntity = mapDataToEntity(dataToUpdate);
            
            categoryEntity.updateInfo(input.name, input.attributeTemplate);
            
            CategoryData updatedDataToSave = mapEntityToData(categoryEntity);
            
            CategoryData savedData = categoryRepository.update(updatedDataToSave);
            
            outputData.success = true;
            outputData.message = "Cập nhật thành công!";
            outputData.updatedCategory = mapDataToOutput(savedData);
		} catch (IllegalArgumentException e) {
            // 9. BẮT LỖI VALIDATION (T4) HOẶC LỖI NGHIỆP VỤ (T3)
            outputData.success = false;
            outputData.message = e.getMessage();
            
        } catch (Exception e) {
        	outputData.success = false;
            outputData.message = "Đã xảy ra lỗi hệ thống. Vui lòng thử lại.";
		}
		
		outBoundary.present(outputData);
	}


	private CategoryOutputData mapDataToOutput(CategoryData data) {
		CategoryOutputData dto = new CategoryOutputData();
        dto.id = data.id; dto.name = data.name; dto.attributeTemplate = data.attributeTemplate;
        return dto;
	}


	private CategoryData mapEntityToData(Category entity) {
		return new CategoryData(entity.getId(), entity.getName(), entity.getAttributeTemplate());
	}


	private Category mapDataToEntity(CategoryData data) {
		return new Category(data.id, data.name, data.attributeTemplate);
	}

}
