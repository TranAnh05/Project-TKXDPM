package cgx.com.usecase.Cart.RemoveFromCart;

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
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;

public class RemoveFromCartUseCase implements RemoveFromCartInputBoundary {

    private final ICartRepository cartRepository;
    private final IDeviceRepository deviceRepository;
    private final IAuthTokenValidator tokenValidator;
    private final RemoveFromCartOutputBoundary outputBoundary;

    public RemoveFromCartUseCase(ICartRepository cartRepository,
                                 IDeviceRepository deviceRepository,
                                 IAuthTokenValidator tokenValidator,
                                 RemoveFromCartOutputBoundary outputBoundary) {
        this.cartRepository = cartRepository;
        this.deviceRepository = deviceRepository;
        this.tokenValidator = tokenValidator;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(RemoveFromCartRequestData input) {
        RemoveFromCartResponseData output = new RemoveFromCartResponseData();

        try {
        	// Validate người dùng
            AuthPrincipal principal = tokenValidator.validate(input.authToken);

            // Validate Input
            ComputerDevice.validateId(input.deviceId);
            
            // Lấy giỏ hàng
            CartData cartData = cartRepository.findByUserId(principal.userId);
            
            DeviceData deviceData = deviceRepository.findById(input.deviceId);
            BigDecimal currentPrice = deviceData.price;

            Cart cartEntity = mapDataToEntity(cartData, principal.userId);

            cartEntity.removeItem(input.deviceId, currentPrice);

            CartData dataToSave = mapEntityToData(cartEntity);
            cartRepository.save(dataToSave);

            output.success = true;
            output.message = "Đã xóa sản phẩm khỏi giỏ hàng.";
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