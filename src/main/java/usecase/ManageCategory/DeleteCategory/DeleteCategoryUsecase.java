package usecase.ManageCategory.DeleteCategory;

import usecase.ManageCategory.CategoryData;
import usecase.ManageCategory.CategoryRepository;

public class DeleteCategoryUsecase implements DeleteCategoryInputBoundary{
	private CategoryRepository categoryRepository;
	private DeleteCategoryOutputBoundary outBoundary;
	private DeleteCategoryOutputData outputData;
	
	public DeleteCategoryUsecase() {
		
	}
	
	public DeleteCategoryUsecase(CategoryRepository categoryRepository, DeleteCategoryOutputBoundary outBoundary) {
		this.categoryRepository = categoryRepository;
		this.outBoundary = outBoundary;
	}
	
	public DeleteCategoryOutputData getOutputData() {
		return outputData;
	}

	@Override
	public void execute(DeleteCategoryInputData input) {
		outputData = new DeleteCategoryOutputData();
		
		try {
			CategoryData categoryToDelete = categoryRepository.findById(input.categoryId);
            if (categoryToDelete == null) {
                throw new IllegalArgumentException("Không tìm thấy loại sản phẩm để xóa.");
            }
            
            int productCount = categoryRepository.countProductsByCategoryId(input.categoryId);
            if (productCount > 0) {
                throw new IllegalArgumentException(
                    "Không thể xóa. Loại sản phẩm này đang chứa " + productCount + " sản phẩm."
                );
            }
            
            categoryRepository.deleteById(input.categoryId);
            
            outputData.success = true;
            outputData.message = "Đã xóa thành công loại sản phẩm: " + categoryToDelete.name;
		} catch (IllegalArgumentException e) {
            // 5. BẮT LỖI NGHIỆP VỤ (T3)
            outputData.success = false;
            outputData.message = e.getMessage();
            
        } catch (Exception e) {
        	// 6. Bắt lỗi hệ thống
            outputData.success = false;
            outputData.message = "Đã xảy ra lỗi hệ thống. Vui lòng thử lại.";
		}
		
		outBoundary.present(this.outputData);
	}

}
