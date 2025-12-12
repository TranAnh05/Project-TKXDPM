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
            List<CategoryData> allCategories = categoryRepository.findAll();

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
