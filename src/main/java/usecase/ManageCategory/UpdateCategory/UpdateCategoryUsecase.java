package usecase.ManageCategory.UpdateCategory;

import Entities.Category;
import usecase.ManageCategory.CategoryData;
import usecase.ManageCategory.CategoryOutputData;
import usecase.ManageCategory.CategoryRepository;

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
			
			CategoryData existingByName = categoryRepository.findByName(input.name);
            if (existingByName != null && existingByName.id != input.id) {
                throw new IllegalArgumentException("Tên loại sản phẩm này đã tồn tại.");
            }
            
            CategoryData dataToUpdate = categoryRepository.findById(input.id);
            if (dataToUpdate == null) {
                throw new IllegalArgumentException("Không tìm thấy loại sản phẩm để cập nhật.");
            }
            
            Category categoryEntity = mapDataToEntity(dataToUpdate);
            
            categoryEntity.setName(input.name);
            
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
        dto.id = data.id; dto.name = data.name;;
        return dto;
	}


	private CategoryData mapEntityToData(Category entity) {
		return new CategoryData(entity.getId(), entity.getName());
	}


	private Category mapDataToEntity(CategoryData data) {
		return new Category(data.id, data.name);
	}

}
