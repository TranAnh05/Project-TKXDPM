package Product.DeleteProduct;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageProduct.DeleteDevice.DeleteDeviceOutputBoundary;
import cgx.com.usecase.ManageProduct.DeleteDevice.DeleteDeviceRequestData;
import cgx.com.usecase.ManageProduct.DeleteDevice.DeleteDeviceResponseData;
import cgx.com.usecase.ManageProduct.DeleteDevice.DeleteLaptopUseCase;
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeleteLaptopUseCaseTest {

    @Mock private IDeviceRepository mockDeviceRepository;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private DeleteDeviceOutputBoundary mockOutputBoundary;

    private DeleteLaptopUseCase useCase;
    private AuthPrincipal adminPrincipal;
    private DeviceData existingLaptop;

    @BeforeEach
    void setUp() {
        useCase = new DeleteLaptopUseCase(mockDeviceRepository, mockTokenValidator, mockOutputBoundary);
        adminPrincipal = new AuthPrincipal("admin-1", "admin@test.com", UserRole.ADMIN);
        
        existingLaptop = new DeviceData();
        existingLaptop.id = "lap-1";
        existingLaptop.name = "Laptop";
        existingLaptop.price = BigDecimal.TEN;
        existingLaptop.stockQuantity = 10;
        existingLaptop.status = "ACTIVE";
        existingLaptop.cpu = "Intel i7"; // Laptop field
        existingLaptop.screenSize = 16.0;
        
    }

    // Case 1: Success - Soft Delete
    @Test
    void test_execute_success() {
        DeleteDeviceRequestData input = new DeleteDeviceRequestData("token", "lap-1");
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.findById("lap-1")).thenReturn(existingLaptop);

        ArgumentCaptor<DeviceData> dataCaptor = ArgumentCaptor.forClass(DeviceData.class);
        ArgumentCaptor<DeleteDeviceResponseData> responseCaptor = ArgumentCaptor.forClass(DeleteDeviceResponseData.class);

        // Act
        useCase.execute(input);

        // Assert
        verify(mockDeviceRepository).save(dataCaptor.capture());
        verify(mockOutputBoundary).present(responseCaptor.capture());

        assertTrue(responseCaptor.getValue().success);
        assertEquals("DELETED", responseCaptor.getValue().newStatus);
        
        // Check data saved to DB
        assertEquals("DELETED", dataCaptor.getValue().status);
        assertEquals("Intel i7", dataCaptor.getValue().cpu); // Data laptop vẫn được giữ nguyên
    }

    // Case 2: Fail - Not Found
    @Test
    void test_execute_failure_notFound() {
        DeleteDeviceRequestData input = new DeleteDeviceRequestData("token", "lap-999");
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.findById("lap-999")).thenReturn(null);

        ArgumentCaptor<DeleteDeviceResponseData> responseCaptor = ArgumentCaptor.forClass(DeleteDeviceResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(responseCaptor.capture());
        assertFalse(responseCaptor.getValue().success);
        assertEquals("Không tìm thấy sản phẩm với ID: lap-999", responseCaptor.getValue().message);
    }
    
    // Case 3: Fail - Not Admin
    @Test
    void test_execute_failure_notAdmin() {
        DeleteDeviceRequestData input = new DeleteDeviceRequestData("token", "lap-1");
        AuthPrincipal customer = new AuthPrincipal("u1", "e", UserRole.CUSTOMER);
        
        when(mockTokenValidator.validate("token")).thenReturn(customer);
        
        ArgumentCaptor<DeleteDeviceResponseData> responseCaptor = ArgumentCaptor.forClass(DeleteDeviceResponseData.class);
        useCase.execute(input);

        verify(mockDeviceRepository, never()).findById(anyString());
        verify(mockOutputBoundary).present(responseCaptor.capture());
        
        assertFalse(responseCaptor.getValue().success);
        assertEquals("Không có quyền truy cập (Yêu cầu Admin).", responseCaptor.getValue().message);
    }
    
    // Case 4: Fail - Mismatch Type (Mouse data)
    @Test
    void test_execute_failure_mismatch() {
        DeviceData mouseData = new DeviceData();
        mouseData.id = "mouse-1";
        mouseData.dpi = 1000;
        mouseData.cpu = null; // NOT Laptop

        DeleteDeviceRequestData input = new DeleteDeviceRequestData("token", "mouse-1");
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.findById("mouse-1")).thenReturn(mouseData);

        ArgumentCaptor<DeleteDeviceResponseData> responseCaptor = ArgumentCaptor.forClass(DeleteDeviceResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(responseCaptor.capture());
        assertFalse(responseCaptor.getValue().success);
        assertTrue(responseCaptor.getValue().message.contains("không khớp loại thiết bị"));
    }

    // Case 5: Fail - Database Error
    @Test
    void test_execute_failure_dbCrash() {
        DeleteDeviceRequestData input = new DeleteDeviceRequestData("token", "lap-1");
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.findById("lap-1")).thenReturn(existingLaptop);
        doThrow(new RuntimeException("DB Error")).when(mockDeviceRepository).save(any());

        ArgumentCaptor<DeleteDeviceResponseData> responseCaptor = ArgumentCaptor.forClass(DeleteDeviceResponseData.class);
        useCase.execute(input);
        
        verify(mockOutputBoundary).present(responseCaptor.capture());
        assertFalse(responseCaptor.getValue().success);
        assertTrue(responseCaptor.getValue().message.contains("Lỗi hệ thống"));
    }
}
