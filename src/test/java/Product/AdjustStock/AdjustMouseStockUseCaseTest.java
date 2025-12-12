package Product.AdjustStock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.UserRole;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageProduct.AdjustStock.AdjustMouseStockUseCase;
import cgx.com.usecase.ManageProduct.AdjustStock.AdjustStockOutputBoundary;
import cgx.com.usecase.ManageProduct.AdjustStock.AdjustStockRequestData;
import cgx.com.usecase.ManageProduct.AdjustStock.AdjustStockResponseData;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdjustMouseStockUseCaseTest {

    @Mock private IDeviceRepository mockDeviceRepository;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private AdjustStockOutputBoundary mockOutputBoundary;

    private AdjustMouseStockUseCase useCase;
    private AuthPrincipal adminPrincipal;
    private DeviceData existingMouse;

    @BeforeEach
    void setUp() {
        useCase = new AdjustMouseStockUseCase(mockDeviceRepository, mockTokenValidator, mockOutputBoundary);
        adminPrincipal = new AuthPrincipal("admin-1", "admin@e.com", UserRole.ADMIN);
        
        existingMouse = new DeviceData();
        existingMouse.id = "mouse-1";
        existingMouse.name = "Mouse";
        existingMouse.price = BigDecimal.TEN;
        existingMouse.stockQuantity = 10;
        existingMouse.dpi = 1000; // Mouse field
        existingMouse.isWireless = true;
        existingMouse.buttonCount = 5;
    }

    // Case 1: Success
    @Test
    void test_execute_success() {
        AdjustStockRequestData input = new AdjustStockRequestData("token", "mouse-1", 50);
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.findById("mouse-1")).thenReturn(existingMouse);

        ArgumentCaptor<DeviceData> dataCaptor = ArgumentCaptor.forClass(DeviceData.class);
        ArgumentCaptor<AdjustStockResponseData> responseCaptor = ArgumentCaptor.forClass(AdjustStockResponseData.class);

        useCase.execute(input);

        verify(mockDeviceRepository).save(dataCaptor.capture());
        verify(mockOutputBoundary).present(responseCaptor.capture());

        assertTrue(responseCaptor.getValue().success);
        assertEquals(50, responseCaptor.getValue().currentStock);
        assertEquals(1000, dataCaptor.getValue().dpi);
    }

    // Case 2: Fail - Not Found
    @Test
    void test_execute_failure_notFound() {
        AdjustStockRequestData input = new AdjustStockRequestData("token", "mouse-999", 50);
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.findById("mouse-999")).thenReturn(null);

        ArgumentCaptor<AdjustStockResponseData> responseCaptor = ArgumentCaptor.forClass(AdjustStockResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(responseCaptor.capture());
        assertFalse(responseCaptor.getValue().success);
        assertEquals("Không tìm thấy sản phẩm với ID: mouse-999", responseCaptor.getValue().message);
    }
    
    // Case 3: Fail - Not Admin
    @Test
    void test_execute_failure_notAdmin() {
        AdjustStockRequestData input = new AdjustStockRequestData("token", "mouse-1", 50);
        AuthPrincipal customer = new AuthPrincipal("u1", "e", UserRole.CUSTOMER);
        
        when(mockTokenValidator.validate("token")).thenReturn(customer);

        ArgumentCaptor<AdjustStockResponseData> responseCaptor = ArgumentCaptor.forClass(AdjustStockResponseData.class);
        useCase.execute(input);

        verify(mockDeviceRepository, never()).findById(anyString());
        verify(mockOutputBoundary).present(responseCaptor.capture());
        assertFalse(responseCaptor.getValue().success);
        assertEquals("Không có quyền truy cập (Yêu cầu Admin).", responseCaptor.getValue().message);
    }
    
    // Case 4: Fail - Empty Token
    @Test
    void test_execute_failure_emptyToken() {
        AdjustStockRequestData input = new AdjustStockRequestData("", "mouse-1", 50);

        ArgumentCaptor<AdjustStockResponseData> responseCaptor = ArgumentCaptor.forClass(AdjustStockResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(responseCaptor.capture());
        assertFalse(responseCaptor.getValue().success);
        assertEquals("Auth Token không được để trống.", responseCaptor.getValue().message);
    }
}
