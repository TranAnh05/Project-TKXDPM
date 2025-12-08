package cgx.com.usecase.Cart.ViewCart;

import java.math.BigDecimal;
import java.util.ArrayList;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.ProductAvailability;
import cgx.com.usecase.Cart.CartData;
import cgx.com.usecase.Cart.CartItemData;
import cgx.com.usecase.Cart.ICartRepository;
import cgx.com.usecase.ManageOrder.PlaceOrder.IDeviceMapper;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;

public class ViewCartUseCase implements ViewCartInputBoundary {

    private final ICartRepository cartRepository;
    private final IDeviceRepository deviceRepository;
    private final IAuthTokenValidator tokenValidator;
    private final IDeviceMapper deviceMapper;
    private final ViewCartOutputBoundary outputBoundary;

    public ViewCartUseCase(ICartRepository cartRepository,
                           IDeviceRepository deviceRepository,
                           IAuthTokenValidator tokenValidator,
                           IDeviceMapper deviceMapper,
                           ViewCartOutputBoundary outputBoundary) {
        this.cartRepository = cartRepository;
        this.deviceRepository = deviceRepository;
        this.tokenValidator = tokenValidator;
        this.deviceMapper = deviceMapper;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(ViewCartRequestData input) {
        ViewCartResponseData output = new ViewCartResponseData();
        output.items = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        try {
            // 1. Xác thực (Authentication)
            if (input.authToken == null || input.authToken.trim().isEmpty()) {
                throw new SecurityException("Vui lòng đăng nhập để xem giỏ hàng.");
            }
            AuthPrincipal principal = tokenValidator.validate(input.authToken);

            // 2. Lấy giỏ hàng (Load Cart)
            CartData cartData = cartRepository.findByUserId(principal.userId);

            // Trường hợp giỏ hàng chưa được tạo hoặc rỗng
            if (cartData == null || cartData.items == null || cartData.items.isEmpty()) {
                output.success = true;
                output.message = "Giỏ hàng của bạn đang trống.";
                output.totalCartPrice = BigDecimal.ZERO;
                outputBoundary.present(output);
                return;
            }

            // 3. Duyệt item & Lấy thông tin Real-time
            for (CartItemData itemData : cartData.items) {
                ViewCartItemData viewItem = processCartItem(itemData);
                
                // Chỉ cộng tiền nếu sản phẩm còn khả dụng (Tùy nghiệp vụ, ở đây ta cộng hết để hiện tổng nhu cầu)
                grandTotal = grandTotal.add(viewItem.subTotal);
                output.items.add(viewItem);
            }

            // 4. Kết thúc thành công
            output.totalCartPrice = grandTotal;
            output.success = true;
            output.message = "Lấy thông tin giỏ hàng thành công.";

        } catch (SecurityException e) {
            // Lỗi nghiệp vụ/Bảo mật: Hiển thị nguyên văn cho User hiểu
            output.success = false;
            output.message = e.getMessage();
            // Reset dữ liệu để tránh UI hiển thị rác
            output.totalCartPrice = BigDecimal.ZERO; 
            
        } catch (Exception e) {
            // Lỗi hệ thống (DB chết, NullPointer, Mapper lỗi...): 
            // KHÔNG show message lỗi kỹ thuật cho User (Bảo mật).
            e.printStackTrace(); // Log ra console cho Dev xem
            output.success = false;
            output.message = "Đã xảy ra lỗi hệ thống khi tải giỏ hàng. Vui lòng thử lại sau.";
            output.totalCartPrice = BigDecimal.ZERO;
        }

        outputBoundary.present(output);
    }

    /**
     * Hàm phụ trợ: Xử lý logic cho từng item
     * Giúp hàm execute chính gọn gàng, dễ đọc.
     */
    private ViewCartItemData processCartItem(CartItemData itemData) {
        ViewCartItemData viewItem = new ViewCartItemData();
        viewItem.deviceId = itemData.deviceId;
        viewItem.quantity = itemData.quantity;

        // Gọi Repository lấy DTO mới nhất
        DeviceData deviceDTO = deviceRepository.findById(itemData.deviceId);

        if (deviceDTO != null) {
            // Chuyển đổi DTO -> Entity để sử dụng logic nghiệp vụ
            // (Mapper sẽ tự lo việc new Laptop hay new Mouse)
            ComputerDevice deviceEntity = deviceMapper.toEntity(deviceDTO);

            // Mapping thông tin cơ bản
            viewItem.deviceName = deviceEntity.getName();
            viewItem.thumbnail = deviceEntity.getThumbnail();
            viewItem.currentPrice = deviceEntity.getPrice();
            viewItem.currentStock = deviceEntity.getStockQuantity();

            // NGHIỆP VỤ: Entity tự kiểm tra xem có đáp ứng được số lượng không
            viewItem.availabilityStatus = deviceEntity.checkAvailability(itemData.quantity);

            // Tính thành tiền (Giá hiện tại * Số lượng trong giỏ)
            viewItem.subTotal = deviceEntity.getPrice().multiply(BigDecimal.valueOf(itemData.quantity));

        } else {
            // Xử lý trường hợp sản phẩm bị xóa cứng (Data Integrity Issue)
            viewItem.deviceName = "Sản phẩm không còn tồn tại";
            viewItem.availabilityStatus = ProductAvailability.DISCONTINUED;
            viewItem.currentPrice = BigDecimal.ZERO;
            viewItem.subTotal = BigDecimal.ZERO;
            viewItem.currentStock = 0;
        }
        
        return viewItem;
    }
}