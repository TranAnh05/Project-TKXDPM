package cgx.com.adapters.ManageProduct.AdjustStock;

import cgx.com.usecase.ManageProduct.AdjustStock.AdjustStockOutputBoundary;
import cgx.com.usecase.ManageProduct.AdjustStock.AdjustStockResponseData;

public class AdjustStockPresenter implements AdjustStockOutputBoundary {

    private AdjustStockViewModel viewModel;

    public AdjustStockPresenter(AdjustStockViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(AdjustStockResponseData responseData) {
        viewModel.success = String.valueOf(responseData.success);
        viewModel.message = responseData.message;
        // Chuyển int -> String
        viewModel.currentStock = responseData.success ? String.valueOf(responseData.currentStock) : null;
    }
    
    public AdjustStockViewModel getModel() {
        return this.viewModel;
    }
}