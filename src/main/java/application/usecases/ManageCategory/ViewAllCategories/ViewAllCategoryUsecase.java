package application.usecases.ManageCategory.ViewAllCategories;

import java.util.ArrayList;
import java.util.List;

import application.dtos.ManageCategory.CategoryData;
import application.dtos.ManageCategory.CategoryOutputData;
import application.dtos.ManageCategory.ViewAllCategories.ViewAllCategoriesOutputData;
import application.ports.in.ManageCategory.ViewAllCategories.ViewAllCategoriesInputBoundary;
import application.ports.out.ManageCategory.CategoryRepository;
import application.ports.out.ManageCategory.ViewAllCategories.ViewAllCategoriesOutputBoundary;
import domain.entities.Category;

public class ViewAllCategoryUsecase implements ViewAllCategoriesInputBoundary {
	private CategoryRepository categoryRepository;
	private ViewAllCategoriesOutputBoundary outBoundary;
	private ViewAllCategoriesOutputData outputData;
	
	public ViewAllCategoryUsecase() {
		
	}
	
	public ViewAllCategoryUsecase(CategoryRepository categoryRepository, ViewAllCategoriesOutputBoundary outBoundary) {
		this.categoryRepository = categoryRepository;
		this.outBoundary = outBoundary;
	}
	
	public ViewAllCategoriesOutputData getOutputData() {
		return outputData;
	}

	@Override
	public void execute() {
		outputData = new ViewAllCategoriesOutputData();
		
		try {
			
			List<CategoryData> categoryDataList = categoryRepository.findAll();
			
			if (categoryDataList.isEmpty()) {
				// 2. Xử lý kịch bản rỗng
				outputData.success = true;
				outputData.message = "Chưa có loại sản phẩm nào.";
				outputData.categories = new ArrayList<>(); // Trả về list rỗng
				outBoundary.present(outputData);
				return;
			}
			
			List<Category> categoryEntities = mapDataToEntities(categoryDataList);
			
			List<CategoryOutputData> safeOutputList = mapEntitiesToOutputData(categoryEntities);
			
			outputData.success = true;
			outputData.categories = safeOutputList;
			
		} catch (Exception e) {
			outputData.success = false;
            outputData.message = "Đã xảy ra lỗi hệ thống khi tải danh mục.";
            outputData.categories = new ArrayList<>();
		}
		
		outBoundary.present(outputData);
	}

	private List<CategoryOutputData> mapEntitiesToOutputData(List<Category> entities) {
		List<CategoryOutputData> dtoList = new ArrayList<>();
        for (Category entity : entities) {
            CategoryOutputData dto = new CategoryOutputData();
            dto.id = entity.getId();
            dto.name = entity.getName();
            dto.attributeTemplate = entity.getAttributeTemplate();
            dtoList.add(dto);
        }
        return dtoList;
	}

	private List<Category> mapDataToEntities(List<CategoryData> dataList) {
		List<Category> entities = new ArrayList<>();
        for (CategoryData data : dataList) {
            entities.add(new Category(
                data.id, 
                data.name, 
                data.attributeTemplate
            ));
        }
        return entities;
	}

}
