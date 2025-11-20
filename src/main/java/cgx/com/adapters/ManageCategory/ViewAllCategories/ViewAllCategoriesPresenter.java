package cgx.com.adapters.ManageCategory.ViewAllCategories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cgx.com.usecase.ManageCategory.CategoryData;
import cgx.com.usecase.ManageCategory.ViewAllCategories.ViewAllCategoriesOutputBoundary;
import cgx.com.usecase.ManageCategory.ViewAllCategories.ViewAllCategoriesResponseData;

public class ViewAllCategoriesPresenter implements ViewAllCategoriesOutputBoundary {

    private ViewAllCategoriesViewModel viewModel;

    public ViewAllCategoriesPresenter(ViewAllCategoriesViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(ViewAllCategoriesResponseData responseData) {
        viewModel.success = String.valueOf(responseData.success);
        viewModel.message = responseData.message;

        if (responseData.success && responseData.categories != null) {
            viewModel.categoryTree = buildCategoryTree(responseData.categories);
        } else {
            viewModel.categoryTree = new ArrayList<>();
        }
    }

    /**
     * Logic chuyển đổi Flat List -> Tree Structure
     */
    private List<CategoryNodeViewDTO> buildCategoryTree(List<CategoryData> flatList) {
        Map<String, CategoryNodeViewDTO> idToNodeMap = new HashMap<>();
        List<CategoryNodeViewDTO> roots = new ArrayList<>();

        // 1. Tạo tất cả các Node và đưa vào Map
        for (CategoryData data : flatList) {
            CategoryNodeViewDTO node = new CategoryNodeViewDTO();
            node.id = data.categoryId;
            node.name = data.name;
            node.description = data.description;
            idToNodeMap.put(data.categoryId, node);
        }

        // 2. Liên kết Cha - Con
        for (CategoryData data : flatList) {
            CategoryNodeViewDTO childNode = idToNodeMap.get(data.categoryId);
            String parentId = data.parentCategoryId;

            if (parentId == null || parentId.trim().isEmpty()) {
                // Nếu không có cha, nó là Root
                roots.add(childNode);
            } else {
                // Nếu có cha, tìm node cha và add vào list children của cha
                CategoryNodeViewDTO parentNode = idToNodeMap.get(parentId);
                if (parentNode != null) {
                    parentNode.children.add(childNode);
                } else {
                    // Trường hợp cha không tồn tại (dữ liệu lỗi), coi như là Root
                    roots.add(childNode);
                }
            }
        }

        return roots;
    }

    public ViewAllCategoriesViewModel getModel() {
        return this.viewModel;
    }
}