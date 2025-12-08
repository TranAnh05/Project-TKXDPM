package cgx.com.adapters.Cart.UpdateCart;

import cgx.com.usecase.Cart.UpdateCart.UpdateCartOutputBoundary;
import cgx.com.usecase.Cart.UpdateCart.UpdateCartResponseData;

public class UpdateCartPresenter implements UpdateCartOutputBoundary {
    
    private final UpdateCartViewModel viewModel;

    public UpdateCartPresenter(UpdateCartViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(UpdateCartResponseData response) {
        viewModel.isSuccess = String.valueOf(response.success);
        viewModel.message = response.message;
        // Trả về số 0 nếu lỗi, hoặc số thực tế nếu thành công
        viewModel.totalItemsInCart = String.valueOf(response.totalItemsInCart);
    }
    
    public UpdateCartViewModel getViewModel() { return viewModel; }
}
