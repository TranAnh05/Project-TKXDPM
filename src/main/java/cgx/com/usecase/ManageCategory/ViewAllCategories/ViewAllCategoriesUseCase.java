package cgx.com.usecase.ManageCategory.ViewAllCategories;

import java.util.List;

import cgx.com.usecase.ManageCategory.CategoryData;
import cgx.com.usecase.ManageCategory.ICategoryRepository;

public class ViewAllCategoriesUseCase implements ViewAllCategoriesInputBoundary {

    private final ICategoryRepository categoryRepository;
    private final ViewAllCategoriesOutputBoundary outputBoundary;

    public ViewAllCategoriesUseCase(ICategoryRepository categoryRepository,
                                    ViewAllCategoriesOutputBoundary outputBoundary) {
        this.categoryRepository = categoryRepository;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute() {
        ViewAllCategoriesResponseData output = new ViewAllCategoriesResponseData();

        try {
            // 1. Lấy tất cả danh mục từ CSDL
            List<CategoryData> allCategories = categoryRepository.findAll();

            // 2. Báo cáo thành công
            output.success = true;
            output.message = "Lấy danh sách danh mục thành công.";
            output.categories = allCategories;

        } catch (Exception e) {
            output.success = false;
            output.message = "Lỗi hệ thống không xác định.";
        }

        outputBoundary.present(output);
    }
}
