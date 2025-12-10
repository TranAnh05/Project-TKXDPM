package cgx.com.adapters.Cart.RemoveFromCart;

import cgx.com.usecase.Cart.RemoveFromCart.RemoveFromCartOutputBoundary;
import cgx.com.usecase.Cart.RemoveFromCart.RemoveFromCartResponseData;

public class RemoveFromCartPresenter implements RemoveFromCartOutputBoundary {
    
    private final RemoveFromCartViewModel viewModel;

    public RemoveFromCartPresenter(RemoveFromCartViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(RemoveFromCartResponseData response) {
        viewModel.isSuccess = String.valueOf(response.success);
        viewModel.message = response.message;
        viewModel.totalItemsInCart = String.valueOf(response.totalItemsInCart);
    }

    public RemoveFromCartViewModel getViewModel() {
        return viewModel;
    }
}