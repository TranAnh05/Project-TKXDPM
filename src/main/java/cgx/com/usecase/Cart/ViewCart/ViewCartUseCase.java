package cgx.com.usecase.Cart.ViewCart;

import java.math.BigDecimal;
import java.util.ArrayList;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.ProductAvailability;
import cgx.com.usecase.Cart.CartData;
import cgx.com.usecase.Cart.CartItemData;
import cgx.com.usecase.Cart.ICartRepository;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageOrder.PlaceOrder.IDeviceMapper;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;

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
                
                grandTotal = grandTotal.add(viewItem.subTotal);
                output.items.add(viewItem);
            }

            // 4. Kết thúc thành công
            output.totalCartPrice = grandTotal;
            output.success = true;
            output.message = "Lấy thông tin giỏ hàng thành công.";

        } catch (SecurityException e) {
            output.success = false;
            output.message = e.getMessage();
            output.totalCartPrice = BigDecimal.ZERO; 
            
        } catch (Exception e) {
            e.printStackTrace(); 
            output.success = false;
            output.message = "Đã xảy ra lỗi hệ thống khi tải giỏ hàng. Vui lòng thử lại sau." + e.getMessage();
            output.totalCartPrice = BigDecimal.ZERO;
        }

        outputBoundary.present(output);
    }

    private ViewCartItemData processCartItem(CartItemData itemData) {
        ViewCartItemData viewItem = new ViewCartItemData();
        viewItem.deviceId = itemData.deviceId;
        viewItem.quantity = itemData.quantity;

        DeviceData deviceDTO = deviceRepository.findById(itemData.deviceId);

        if (deviceDTO != null) {
        	// chuyển dto sang entity
            ComputerDevice deviceEntity = deviceMapper.toEntity(deviceDTO);

            viewItem.deviceName = deviceEntity.getName();
            viewItem.thumbnail = deviceEntity.getThumbnail();
            viewItem.currentPrice = deviceEntity.getPrice();
            viewItem.currentStock = deviceEntity.getStockQuantity();

            viewItem.availabilityStatus = deviceEntity.checkAvailability(itemData.quantity);

            // Tính thành tiền
            viewItem.subTotal = deviceEntity.getPrice().multiply(BigDecimal.valueOf(itemData.quantity));
        }
        
        return viewItem;
    }
}