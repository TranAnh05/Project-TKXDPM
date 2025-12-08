package cgx.com.usecase.Cart.UpdateCart;

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

public class UpdateCartUseCase implements UpdateCartInputBoundary {

    private final ICartRepository cartRepository;
    private final IDeviceRepository deviceRepository;
    private final IAuthTokenValidator tokenValidator;
    private final UpdateCartOutputBoundary outputBoundary;

    public UpdateCartUseCase(ICartRepository cartRepository,
                             IDeviceRepository deviceRepository,
                             IAuthTokenValidator tokenValidator,
                             UpdateCartOutputBoundary outputBoundary) {
        this.cartRepository = cartRepository;
        this.deviceRepository = deviceRepository;
        this.tokenValidator = tokenValidator;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(UpdateCartRequestData input) {
        UpdateCartResponseData output = new UpdateCartResponseData();

        try {
            // 1. Validate Auth
            if (input.authToken == null) throw new SecurityException("Vui lòng đăng nhập.");
            AuthPrincipal principal = tokenValidator.validate(input.authToken);

            // 2. Validate Input
            CartItem.validateQuantity(input.newQuantity);

            // 3. Lấy thông tin Sản phẩm (Mới nhất từ DB để check giá và kho)
            DeviceData deviceData = deviceRepository.findById(input.deviceId);
            if (deviceData == null) {
                throw new IllegalArgumentException("Sản phẩm không tồn tại.");
            }
            
            ComputerDevice.validateStatus(deviceData.status);

            // 4. Lấy Giỏ hàng
            CartData cartData = cartRepository.findByUserId(principal.userId);
            if (cartData == null) {
                throw new IllegalArgumentException("Giỏ hàng không tồn tại.");
            }

            // 5. Rehydrate Entity (Mapper)
            Cart cartEntity = mapDataToEntity(cartData, principal.userId);

            // 6. THỰC HIỆN NGHIỆP VỤ (Entity lo việc tính toán và check tồn kho)
            cartEntity.updateItemQuantity(
                input.deviceId, 
                input.newQuantity, 
                deviceData.stockQuantity, 
                deviceData.price
            );

            // 7. Lưu lại
            CartData dataToSave = mapEntityToData(cartEntity);
            cartRepository.save(dataToSave);

            // 8. Success
            output.success = true;
            output.message = "Cập nhật giỏ hàng thành công.";
            output.totalItemsInCart = cartEntity.getTotalItemCount();

        } catch (IllegalArgumentException | SecurityException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            output.success = false;
            output.message = "Lỗi hệ thống: " + e.getMessage();
            e.printStackTrace();
        }

        outputBoundary.present(output);
    }

    // --- Private Mappers (Tương tự AddToCart) ---
    private Cart mapDataToEntity(CartData data, String userId) {
        List<CartItem> items = new ArrayList<>();
        if (data.items != null) {
            items = data.items.stream()
                    .map(i -> new CartItem(i.deviceId, i.quantity))
                    .collect(Collectors.toList());
        }
        return new Cart(data.userId, items, data.totalEstimatedPrice, data.updatedAt);
    }

    private CartData mapEntityToData(Cart entity) {
        CartData data = new CartData();
        data.userId = entity.getUserId();
        data.updatedAt = entity.getUpdatedAt();
        data.totalEstimatedPrice = entity.getTotalEstimatedPrice();
        data.items = new ArrayList<>();
        for (CartItem item : entity.getItems()) {
            data.items.add(new CartItemData(item.getDeviceId(), item.getQuantity()));
        }
        return data;
    }
}
