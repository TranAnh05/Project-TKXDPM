package usecase.ManageProduct.ViewAllProducts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Entities.Keyboard;
import Entities.Laptop;
import Entities.Mouse;
import Entities.Product;
import application.ports.out.ManageProduct.ProductRepository;
import usecase.ManageCategory.CategoryData;
import usecase.ManageCategory.CategoryRepository;
import usecase.ManageProduct.ProductData;
import usecase.ManageProduct.ProductOutputData;

public class ViewAllProductsUsecase implements ViewAllProductsInputBoundary{
	private ProductRepository productRepository;
	private CategoryRepository categoryRepository;
	private ViewAllProductsOutputBoundary outBoundary;
	private ViewAllProductsOutputData outputData;
	
	public ViewAllProductsUsecase() {
		
	}
	
	public ViewAllProductsUsecase(ProductRepository productRepository, CategoryRepository categoryRepository,
			ViewAllProductsOutputBoundary outBoundary) {
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
		this.outBoundary = outBoundary;
	}
	
	public ViewAllProductsOutputData getOutputData() {
		return outputData;
	}
	
	@Override
	public void execute() {
		outputData = new ViewAllProductsOutputData();
		
		try {
			// 1. Lấy dữ liệu từ csdl
	        List<ProductData> productDataList = productRepository.findAll();
	        List<CategoryData> categoryDataList = categoryRepository.findAll();
			
	        // 2. Xử lý kịch bản rỗng
	        if (productDataList.isEmpty()) {
	            outputData.success = true;
	            outputData.message = "Chưa có sản phẩm nào.";
	            outputData.products = new ArrayList<>();
	            outBoundary.present(outputData);
	            return;
	        }
	        
	        // 3. Chuẩn bị Map<Integer, CategoryData> (T3 DTO) để tra cứu
	        Map<Integer, CategoryData> categoryDataMap = mapCategoriesToData(categoryDataList);
	        
	        // 4. chuyển dữ liệu sang outputData
	        List<ProductSummaryOutputData> safeOutputList = mapToOutputData(productDataList, categoryDataMap);
	        
	        outputData.success = true;
            outputData.products = safeOutputList;
		} catch (Exception e) {
			// 7. Bắt lỗi hệ thống
			 e.printStackTrace();
            outputData.success = false;
            outputData.message = "Đã xảy ra lỗi hệ thống khi tải sản phẩm.";
            outputData.products = new ArrayList<>();
		}
		
		outBoundary.present(outputData);
	}

	

	private List<ProductSummaryOutputData> mapToOutputData(List<ProductData> dataList,
			Map<Integer, CategoryData> categoryMap) {
		
		List<ProductSummaryOutputData> dtoList = new ArrayList<>();
        for (ProductData data : dataList) {
            // 1. Tạo DTO Tóm tắt (T3)
            ProductSummaryOutputData dto = new ProductSummaryOutputData();
            
            // 2. Chỉ sao chép các trường chung
            dto.id = data.id;
            dto.name = data.name;
            dto.price = data.price;
            dto.stockQuantity = data.stockQuantity;
            dto.imageUrl = data.imageUrl;
            
            // 3. "Làm giàu" (Enrich) Tên Category
            CategoryData category = categoryMap.get(data.categoryId);
            dto.categoryName = (category != null) ? category.name : "Không rõ";
            
            dtoList.add(dto);
        }
        
        return dtoList;
	}

	private Map<Integer, CategoryData> mapCategoriesToData(List<CategoryData> dataList) {
		Map<Integer, CategoryData> map = new HashMap<>();
        for (CategoryData data : dataList) { map.put(data.id, data); }
        return map;
	}
}
