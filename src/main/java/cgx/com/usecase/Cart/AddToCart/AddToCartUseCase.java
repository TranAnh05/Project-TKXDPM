package cgx.com.usecase.Cart.AddToCart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cgx.com.Entities.Cart;
import cgx.com.Entities.CartItem;
import cgx.com.Entities.ComputerDevice;
import cgx.com.usecase.Cart.CartData;
import cgx.com.usecase.Cart.CartItemData;
import cgx.com.usecase.Cart.ICartRepository;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;

public class AddToCartUseCase implements AddToCartInputBoundary {

    private final ICartRepository cartRepository;
    private final IDeviceRepository deviceRepository;
    private final IAuthTokenValidator tokenValidator;
    private final AddToCartOutputBoundary outputBoundary;

    public AddToCartUseCase(ICartRepository cartRepository,
                            IDeviceRepository deviceRepository,
                            IAuthTokenValidator tokenValidator,
                            AddToCartOutputBoundary outputBoundary) {
        this.cartRepository = cartRepository;
        this.deviceRepository = deviceRepository;
        this.tokenValidator = tokenValidator;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(AddToCartRequestData input) {
        AddToCartResponseData output = new AddToCartResponseData();

        try {
            // 1. Xác thực người dùng (Authentication)
            if (input.authToken == null || input.authToken.isEmpty()) {
                throw new SecurityException("Vui lòng đăng nhập để mua hàng.");
            }
            AuthPrincipal principal = tokenValidator.validate(input.authToken);

            // 2. Validate Input đầu vào
            CartItem.validateQuantity(input.quantity);
            ComputerDevice.validateId(input.deviceId);
            // 3. Kiểm tra thông tin Sản phẩm (Product Check)
            // (Bước này tương ứng Kịch bản 1 trong sơ đồ)
            DeviceData deviceData = deviceRepository.findById(input.deviceId);
            
            if (deviceData == null) {
                throw new IllegalArgumentException("Sản phẩm không tồn tại.");
            }
            // Check trạng thái kinh doanh
            ComputerDevice.validateStatus(deviceData.status);
            
            // Check hàng trong kho (Check nhanh)
            ComputerDevice.validateStockQuantity(deviceData.stockQuantity);
            
            // 4. Lấy Giỏ hàng hiện tại (Load Cart)
            CartData cartData = cartRepository.findByUserId(principal.userId);
            Cart cartEntity = mapDataToEntity(cartData, principal.userId);

            // 5. THỰC HIỆN NGHIỆP VỤ (Core Logic)
            // Gọi Entity để check logic cộng dồn và tính toán
            // (Bước này tương ứng Kịch bản 2 & 3 trong sơ đồ)
            cartEntity.addItem(
                input.deviceId, 
                input.quantity, 
                deviceData.stockQuantity, 
                deviceData.price
            );

            // 6. Lưu xuống Database (Persistence)
            CartData dataToSave = mapEntityToData(cartEntity);
            cartRepository.save(dataToSave);

            // 7. Phản hồi thành công
            output.success = true;
            output.message = "Đã thêm sản phẩm vào giỏ hàng.";
            output.totalItemsInCart = cartEntity.getTotalItemCount();

        } catch (IllegalArgumentException | SecurityException e) {
            // Xử lý các lỗi nghiệp vụ (Hết hàng, Không đủ số lượng, Chưa login...)
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            // Xử lý lỗi hệ thống
            output.success = false;
            output.message = "Lỗi hệ thống: " + e.getMessage();
            e.printStackTrace();
        }

        outputBoundary.present(output);
    }

    /**
     * Chuyển đổi từ CartData (DTO DB) sang Cart (Entity).
     * Nếu Data null (user chưa có giỏ), tạo Entity mới.
     */
    private Cart mapDataToEntity(CartData data, String userId) {
        // Trường hợp 1: User chưa có giỏ hàng trong DB -> Tạo mới Entity rỗng
        if (data == null) {
            return new Cart(userId); 
        }

        // Trường hợp 2: Đã có giỏ -> Tái tạo (Rehydrate)
        // Map danh sách items
        List<CartItem> entityItems = new ArrayList<>();
        if (data.items != null) {
            entityItems = data.items.stream()
                    .map(itemData -> new CartItem(itemData.deviceId, itemData.quantity))
                    .collect(Collectors.toList());
        }

        // Xử lý null safety cho các trường số
        BigDecimal totalPrice = (data.totalEstimatedPrice != null) ? data.totalEstimatedPrice : BigDecimal.ZERO;

        // Gọi Constructor tái tạo của Entity
        return new Cart(data.userId, entityItems, totalPrice, data.updatedAt);
    }

    /**
     * Chuyển đổi từ Cart (Entity) sang CartData (DTO DB) để lưu.
     */
    private CartData mapEntityToData(Cart entity) {
        CartData data = new CartData();
        
        // 1. Map các trường đơn giản
        data.userId = entity.getUserId();
        data.updatedAt = entity.getUpdatedAt();
        data.totalEstimatedPrice = entity.getTotalEstimatedPrice();
        
        // 2. Map danh sách items (Deep Copy để tránh tham chiếu)
        data.items = new ArrayList<>();
        for (CartItem itemEntity : entity.getItems()) {
            data.items.add(new CartItemData(
                itemEntity.getDeviceId(), 
                itemEntity.getQuantity()
            ));
        }
        
        return data;
    }
}
