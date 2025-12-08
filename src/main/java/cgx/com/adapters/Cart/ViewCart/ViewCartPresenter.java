package cgx.com.adapters.Cart.ViewCart;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;

import cgx.com.usecase.Cart.ViewCart.ViewCartItemData;
import cgx.com.usecase.Cart.ViewCart.ViewCartOutputBoundary;
import cgx.com.usecase.Cart.ViewCart.ViewCartResponseData;

public class ViewCartPresenter implements ViewCartOutputBoundary {

    private final ViewCartViewModel viewModel;
    private final DecimalFormat currencyFormatter;

    public ViewCartPresenter(ViewCartViewModel viewModel) {
        this.viewModel = viewModel;
        // Cấu hình format tiền tệ Việt Nam (Ví dụ: 1.000.000 đ)
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator('.');
        this.currencyFormatter = new DecimalFormat("#,### đ", symbols);
    }

    @Override
    public void present(ViewCartResponseData response) {
        // 1. Chuyển đổi trạng thái chung
        viewModel.isSuccess = String.valueOf(response.success);
        viewModel.message = response.message;

        if (response.success) {
            // 2. Format Tổng tiền giỏ hàng (Xử lý null safety)
            BigDecimal total = (response.totalCartPrice != null) ? response.totalCartPrice : BigDecimal.ZERO;
            viewModel.totalCartPrice = currencyFormatter.format(total);

            // 3. Map danh sách items
            if (response.items != null) {
                viewModel.items = response.items.stream()
                        .map(this::mapToItemViewModel)
                        .collect(Collectors.toList());
            } else {
                viewModel.items = new ArrayList<>();
            }
        } else {
            // Trường hợp lỗi: Reset dữ liệu hiển thị về rỗng/0
            viewModel.totalCartPrice = "0 đ";
            viewModel.items = new ArrayList<>();
        }
    }

    /**
     * Logic chuyển đổi chi tiết từng món hàng
     */
    private CartItemViewModel mapToItemViewModel(ViewCartItemData dto) {
        CartItemViewModel itemVM = new CartItemViewModel();
        
        itemVM.deviceId = dto.deviceId;
        itemVM.deviceName = dto.deviceName;
        itemVM.thumbnail = dto.thumbnail;
        
        // Convert int -> String
        itemVM.quantity = String.valueOf(dto.quantity);
        
        // Convert BigDecimal -> String Money
        itemVM.unitPrice = currencyFormatter.format(dto.currentPrice);
        itemVM.subTotal = currencyFormatter.format(dto.subTotal);
        
        // Convert Enum/Logic -> String Label & Color
        // Logic hiển thị trạng thái hàng hóa
        switch (dto.availabilityStatus) {
            case AVAILABLE:
                itemVM.statusLabel = "Còn hàng";
                itemVM.statusColor = "green"; // Gợi ý cho UI tô màu xanh
                itemVM.isBuyable = "true";
                break;
                
            case NOT_ENOUGH_STOCK:
                itemVM.statusLabel = "Chỉ còn " + dto.currentStock + " sản phẩm";
                itemVM.statusColor = "orange"; // Gợi ý màu cam
                itemVM.isBuyable = "false";    // Không cho mua nếu thiếu
                break;
                
            case OUT_OF_STOCK:
                itemVM.statusLabel = "Tạm hết hàng";
                itemVM.statusColor = "red";
                itemVM.isBuyable = "false";
                break;
                
            case DISCONTINUED:
                itemVM.statusLabel = "Ngừng kinh doanh";
                itemVM.statusColor = "gray";
                itemVM.isBuyable = "false";
                break;
                
            default:
                itemVM.statusLabel = "Không xác định";
                itemVM.statusColor = "black";
                itemVM.isBuyable = "false";
        }
        
        return itemVM;
    }

    public ViewCartViewModel getViewModel() {
        return this.viewModel;
    }
}
