package Product.DeleteProduct;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.UserRole;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageOrder.IOrderRepository;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageProduct.DeleteDevice.DeleteDeviceOutputBoundary;
import cgx.com.usecase.ManageProduct.DeleteDevice.DeleteDeviceRequestData;
import cgx.com.usecase.ManageProduct.DeleteDevice.DeleteDeviceResponseData;
import cgx.com.usecase.ManageProduct.DeleteDevice.DeleteLaptopUseCase;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteDeviceUseCaseTest {

    @Mock
    private IDeviceRepository deviceRepository;
    @Mock
    private IOrderRepository orderRepository;
    @Mock
    private IAuthTokenValidator tokenValidator;
    @Mock
    private DeleteDeviceOutputBoundary outputBoundary;

    private DeleteLaptopUseCase deleteLaptopUseCase;

    @BeforeEach
    void setUp() {
        deleteLaptopUseCase = new DeleteLaptopUseCase(
                deviceRepository,
                orderRepository,
                tokenValidator,
                outputBoundary
        );
    }

    // Case: Không phải admin
    @Test
    void testExecute_WhenUserNotAdmin_ShouldFail() {
        // Arrange
        DeleteDeviceRequestData input = new DeleteDeviceRequestData();
        input.authToken = "user-token";
        input.deviceId = "laptop-01";

        AuthPrincipal mockPrincipal = new AuthPrincipal("user1", "test@gmail.com", UserRole.CUSTOMER);
        when(tokenValidator.validate(input.authToken)).thenReturn(mockPrincipal);

        // Act
        deleteLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<DeleteDeviceResponseData> captor = ArgumentCaptor.forClass(DeleteDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        DeleteDeviceResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("Không có quyền truy cập.", output.message); 
        
        verify(deviceRepository, never()).findById(any());
    }

    // Case: ID rỗng
    @Test
    void testExecute_WhenDeviceIdInvalid_ShouldFail() {
        // Arrange
        DeleteDeviceRequestData input = new DeleteDeviceRequestData();
        input.authToken = "admin-token";
        input.deviceId = ""; // ID rỗng

        // Mock Admin để qua bước security
        AuthPrincipal mockAdmin = new AuthPrincipal("Admin", "test@gmail.com", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(mockAdmin);

        // Act
        deleteLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<DeleteDeviceResponseData> captor = ArgumentCaptor.forClass(DeleteDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        DeleteDeviceResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("ID sản phẩm không được để trống.", output.message);
    }

    // Case: Không tìm thấy
    @Test
    void testExecute_WhenDeviceNotFound_ShouldFail() {
        // Arrange
        String deviceId = "unknown-laptop";
        DeleteDeviceRequestData input = new DeleteDeviceRequestData();
        input.authToken = "admin-token";
        input.deviceId = deviceId;

        AuthPrincipal mockAdmin = new AuthPrincipal("Admin", "test@gmail.com", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(mockAdmin);
        
        when(deviceRepository.findById(deviceId)).thenReturn(null);

        // Act
        deleteLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<DeleteDeviceResponseData> captor = ArgumentCaptor.forClass(DeleteDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        DeleteDeviceResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("Không tìm thấy sản phẩm với ID: " + deviceId, output.message);
    }

    // Case: Còn trong đơn hàng
    @Test
    void testExecute_WhenDeviceInActiveOrder_ShouldFail() {
        // Arrange
        String deviceId = "laptop-in-order";
        DeleteDeviceRequestData input = new DeleteDeviceRequestData();
        input.authToken = "admin-token";
        input.deviceId = deviceId;

        AuthPrincipal mockAdmin = new AuthPrincipal("Admin", "test@gmail.com", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(mockAdmin);

        // Mock Device Data
        DeviceData mockData = createMockDeviceData(deviceId, 10, "AVAILABLE");
        when(deviceRepository.findById(deviceId)).thenReturn(mockData);

        when(orderRepository.isProductInActiveOrder(deviceId)).thenReturn(true);

        // Act
        deleteLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<DeleteDeviceResponseData> captor = ArgumentCaptor.forClass(DeleteDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        DeleteDeviceResponseData output = captor.getValue();

        assertFalse(output.success);
        assertTrue(output.message.contains("Sản phẩm đang nằm trong đơn hàng chưa hoàn tất"));
        
        verify(deviceRepository, never()).save(any());
    }

    // Case thành công
    @Test
    void testExecute_WhenAllConditionsMet_ShouldSucceed() {
        // Arrange
        String deviceId = "valid-laptop";
        DeleteDeviceRequestData input = new DeleteDeviceRequestData();
        input.authToken = "admin-token";
        input.deviceId = deviceId;

        // 1. Mock Admin
        AuthPrincipal mockAdmin = new AuthPrincipal("Admin", "test@gmail.com", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(mockAdmin);

        // 2. Mock Device Data (Đang có hàng)
        DeviceData mockData = createMockDeviceData(deviceId, 50, "AVAILABLE");
        when(deviceRepository.findById(deviceId)).thenReturn(mockData);

        // 3. Mock Order (Không có đơn hàng nào giữ sản phẩm này)
        when(orderRepository.isProductInActiveOrder(deviceId)).thenReturn(false);

        // Act
        deleteLaptopUseCase.execute(input);

        // Assert - Verify Output
        ArgumentCaptor<DeleteDeviceResponseData> responseCaptor = ArgumentCaptor.forClass(DeleteDeviceResponseData.class);
        verify(outputBoundary).present(responseCaptor.capture());
        DeleteDeviceResponseData output = responseCaptor.getValue();

        assertTrue(output.success);
        assertEquals("DISCONTINUED", output.newStatus);
        assertEquals(deviceId, output.deletedDeviceId);

        ArgumentCaptor<DeviceData> saveCaptor = ArgumentCaptor.forClass(DeviceData.class);
        verify(deviceRepository).save(saveCaptor.capture());
        DeviceData savedData = saveCaptor.getValue();

        assertEquals("DISCONTINUED", savedData.status); 
        assertEquals(0, savedData.stockQuantity);      
    }
    
    // --- Helper Method ---
    private DeviceData createMockDeviceData(String id, int stock, String status) {
        DeviceData d = new DeviceData();
        d.id = id;
        d.name = "Test Laptop";
        d.price = BigDecimal.valueOf(1000);
        d.stockQuantity = stock;
        d.status = status;
        d.categoryId = "CAT-LAPTOP";
        d.createdAt = Instant.now();
        d.updatedAt = Instant.now();
        d.cpu = "i7";
        d.ram = "16GB";
        d.storage = "512GB";
        d.screenSize = 15.6;
        return d;
    }
}