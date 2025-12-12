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
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;

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
        	// Xác thực người dùng
            AuthPrincipal principal = tokenValidator.validate(input.authToken);
            
            // Kiểm tra đầu vào
            CartItem.validateQuantity(input.quantity);
            ComputerDevice.validateId(input.deviceId);
            
            DeviceData deviceData = deviceRepository.findById(input.deviceId);
            
            if (deviceData == null) {
                throw new IllegalArgumentException("Sản phẩm không tồn tại.");
            }
            
            // Check trạng thái kinh doanh
            ComputerDevice.validateStatus(deviceData.status);
            
            // Check hàng trong kho (Check nhanh)
            ComputerDevice.validateStockQuantity(deviceData.stockQuantity);
            
            // Lấy Giỏ hàng hiện tại (Load Cart)
            CartData cartData = cartRepository.findByUserId(principal.userId);
            Cart cartEntity = mapDataToEntity(cartData, principal.userId);

            // Nghiệp vụ: 
            cartEntity.addItem(
                input.deviceId, 
                input.quantity, 
                deviceData.stockQuantity, 
                deviceData.price
            );

            CartData dataToSave = mapEntityToData(cartEntity);
            cartRepository.save(dataToSave);

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

    // Map DTO sang Entity
    private Cart mapDataToEntity(CartData data, String userId) {
    	// Trường hợp user chưa có giỏ hàng
        if (data == null) {
            return new Cart(userId); 
        }

        // Trường hợp đã có giỏ hàng: 
        List<CartItem> entityItems = new ArrayList<>();
        if (data.items != null) {
            entityItems = data.items.stream()
                    .map(itemData -> new CartItem(itemData.deviceId, itemData.quantity))
                    .collect(Collectors.toList());
        }

        BigDecimal totalPrice = data.totalEstimatedPrice;
        
        return new Cart(data.userId, entityItems, totalPrice, data.updatedAt);
    }

    // Map Entity sang DTO
    private CartData mapEntityToData(Cart entity) {
        CartData data = new CartData();
        
        data.userId = entity.getUserId();
        data.updatedAt = entity.getUpdatedAt();
        data.totalEstimatedPrice = entity.getTotalEstimatedPrice();
        
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
