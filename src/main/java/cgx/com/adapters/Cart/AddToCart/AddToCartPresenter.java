package cgx.com.adapters.Cart.AddToCart;

import cgx.com.usecase.Cart.AddToCart.AddToCartOutputBoundary;
import cgx.com.usecase.Cart.AddToCart.AddToCartResponseData;

public class AddToCartPresenter implements AddToCartOutputBoundary {

    private final AddToCartViewModel viewModel;

    public AddToCartPresenter(AddToCartViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(AddToCartResponseData responseData) {
        // 1. Chuyển đổi boolean -> String ("true"/"false")
        viewModel.success = String.valueOf(responseData.success);

        // 2. Chuyển đổi message (Giữ nguyên hoặc xử lý null nếu cần)
        viewModel.message = responseData.message;

        // 3. Chuyển đổi int -> String (Ví dụ: 5 -> "5")
        // UI chỉ việc hiển thị chuỗi này, không cần format lại
        viewModel.totalItemsInCart = String.valueOf(responseData.totalItemsInCart);
    }

    public AddToCartViewModel getViewModel() {
        return this.viewModel;
    }
}
