package application.usecases.ManageCategory.GetCategoryTemplate;

import application.dtos.ManageCategory.CategoryData;
import application.dtos.ManageCategory.GetCategoryTemplate.GetCategoryTemplateInputData;
import application.dtos.ManageCategory.GetCategoryTemplate.GetCategoryTemplateOutputData;
import application.ports.in.ManageCategory.GetCategoryTemplate.GetCategoryTemplateInputBoundary;
import application.ports.out.ManageCategory.CategoryRepository;
import application.ports.out.ManageCategory.GetCategoryTemplate.GetCategoryTemplateOutputBoundary;

public class GetCategoryTemplateUsecase implements GetCategoryTemplateInputBoundary{
	private CategoryRepository categoryRepository;
	private GetCategoryTemplateOutputBoundary outBoundary;
	private GetCategoryTemplateOutputData outputData;
	
	public GetCategoryTemplateUsecase() {
		
	}
	
	public GetCategoryTemplateUsecase(CategoryRepository categoryRepository,
			GetCategoryTemplateOutputBoundary outBoundary) {
		this.categoryRepository = categoryRepository;
		this.outBoundary = outBoundary;
	}
	
	public GetCategoryTemplateOutputData getOutputData() {
		return outputData;
	}



	@Override
	public void execute(GetCategoryTemplateInputData input) {
		outputData = new GetCategoryTemplateOutputData();
		
		try {
			CategoryData categoryData = categoryRepository.findById(input.categoryId);
            
            if (categoryData == null) {
                throw new IllegalArgumentException("Không tìm thấy loại sản phẩm.");
            }
            
            outputData.success = true;
            outputData.attributeTemplate = categoryData.attributeTemplate;
		} catch (IllegalArgumentException e) {
            // 3. BẮT LỖI NGHIỆP VỤ (T3)
            outputData.success = false;
            outputData.message = e.getMessage();
        } catch (Exception e) {
        	// 4. Bắt lỗi hệ thống
           outputData.success = false;
           outputData.message = "Đã xảy ra lỗi hệ thống. Vui lòng thử lại.";
		}
		
		outBoundary.present(this.outputData);
		
	}

}
