package Product.AdjustStock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageProduct.AdjustStock.AdjustLaptopStockUseCase;
import cgx.com.usecase.ManageProduct.AdjustStock.AdjustStockOutputBoundary;
import cgx.com.usecase.ManageProduct.AdjustStock.AdjustStockRequestData;
import cgx.com.usecase.ManageProduct.AdjustStock.AdjustStockResponseData;
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdjustLaptopStockUseCaseTest {

    @Mock private IDeviceRepository mockDeviceRepository;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private AdjustStockOutputBoundary mockOutputBoundary;

    private AdjustLaptopStockUseCase useCase;
    private AuthPrincipal adminPrincipal;
    private DeviceData existingLaptop;

    @BeforeEach
    void setUp() {
        useCase = new AdjustLaptopStockUseCase(mockDeviceRepository, mockTokenValidator, mockOutputBoundary);
        adminPrincipal = new AuthPrincipal("admin-1", "admin@test.com", UserRole.ADMIN);
        
        existingLaptop = new DeviceData();
        existingLaptop.id = "lap-1";
        existingLaptop.name = "Laptop";
        existingLaptop.price = BigDecimal.TEN;
        existingLaptop.stockQuantity = 10;
        existingLaptop.cpu = "Intel i7"; // Laptop field
        existingLaptop.screenSize = 14.0;
    }

    // Case 1: Success
    @Test
    void test_execute_success() {
        AdjustStockRequestData input = new AdjustStockRequestData("token", "lap-1", 50);
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.findById("lap-1")).thenReturn(existingLaptop);

        ArgumentCaptor<DeviceData> dataCaptor = ArgumentCaptor.forClass(DeviceData.class);
        ArgumentCaptor<AdjustStockResponseData> responseCaptor = ArgumentCaptor.forClass(AdjustStockResponseData.class);

        useCase.execute(input);

        verify(mockDeviceRepository).save(dataCaptor.capture());
        verify(mockOutputBoundary).present(responseCaptor.capture());

        assertTrue(responseCaptor.getValue().success);
        assertEquals(50, responseCaptor.getValue().currentStock);
        // Kiểm tra mapping laptop
        assertEquals("Intel i7", dataCaptor.getValue().cpu);
    }

    // Case 2: Fail - Negative Quantity (Entity Validation)
    @Test
    void test_execute_failure_negative() {
        AdjustStockRequestData input = new AdjustStockRequestData("token", "lap-1", -1);
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.findById("lap-1")).thenReturn(existingLaptop);

        ArgumentCaptor<AdjustStockResponseData> responseCaptor = ArgumentCaptor.forClass(AdjustStockResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(responseCaptor.capture());
        assertFalse(responseCaptor.getValue().success);
        assertEquals("Số lượng tồn kho không được âm.", responseCaptor.getValue().message);
    }

    // Case 3: Fail - Mismatch Type (Mouse data passed to Laptop UseCase)
    @Test
    void test_execute_failure_mismatch() {
        DeviceData mouseData = new DeviceData();
        mouseData.id = "mouse-1";
        mouseData.dpi = 1000;
        mouseData.cpu = null; // NOT Laptop

        AdjustStockRequestData input = new AdjustStockRequestData("token", "mouse-1", 50);
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.findById("mouse-1")).thenReturn(mouseData);

        ArgumentCaptor<AdjustStockResponseData> responseCaptor = ArgumentCaptor.forClass(AdjustStockResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(responseCaptor.capture());
        assertFalse(responseCaptor.getValue().success);
        assertTrue(responseCaptor.getValue().message.contains("không khớp loại thiết bị"));
    }

    // Case 4: Fail - Database Error
    @Test
    void test_execute_failure_dbCrash() {
        AdjustStockRequestData input = new AdjustStockRequestData("token", "lap-1", 50);
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.findById("lap-1")).thenReturn(existingLaptop);
        doThrow(new RuntimeException("DB Error")).when(mockDeviceRepository).save(any());

        ArgumentCaptor<AdjustStockResponseData> responseCaptor = ArgumentCaptor.forClass(AdjustStockResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(responseCaptor.capture());
        assertFalse(responseCaptor.getValue().success);
        assertTrue(responseCaptor.getValue().message.contains("Lỗi hệ thống"));
    }
}
