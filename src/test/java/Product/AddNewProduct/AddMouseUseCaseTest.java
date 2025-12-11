package Product.AddNewProduct;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageCategory.CategoryData;
import cgx.com.usecase.ManageCategory.ICategoryRepository;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageProduct.AddNewProduct.AddDeviceOutputBoundary;
import cgx.com.usecase.ManageProduct.AddNewProduct.AddDeviceResponseData;
import cgx.com.usecase.ManageProduct.AddNewProduct.AddMouseRequestData;
import cgx.com.usecase.ManageProduct.AddNewProduct.AddMouseUseCase;
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IUserIdGenerator;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddMouseUseCaseTest {

    @Mock private IDeviceRepository mockDeviceRepository;
    @Mock private ICategoryRepository mockCategoryRepository;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private IUserIdGenerator mockIdGenerator;
    @Mock private AddDeviceOutputBoundary mockOutputBoundary;

    private AddMouseUseCase useCase;
    private AuthPrincipal adminPrincipal;

    @BeforeEach
    void setUp() {
        useCase = new AddMouseUseCase(
            mockDeviceRepository, mockCategoryRepository, mockTokenValidator, mockIdGenerator, mockOutputBoundary
        );
        adminPrincipal = new AuthPrincipal("admin-1", "admin@test.com", UserRole.ADMIN);
    }

    // 1. Test Thành công
    @Test
    void test_execute_success() {
        AddMouseRequestData input = new AddMouseRequestData(
            "token", "Logitech Mouse", "Desc", new BigDecimal("50.0"), 10, "cat-mouse", "url",
            16000, true, 6
        );

        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.existsByName("Logitech Mouse")).thenReturn(false);
        when(mockCategoryRepository.findById("cat-mouse")).thenReturn(new CategoryData());
        when(mockIdGenerator.generate()).thenReturn("mouse-123");

        ArgumentCaptor<DeviceData> deviceDataCaptor = ArgumentCaptor.forClass(DeviceData.class);
        ArgumentCaptor<AddDeviceResponseData> responseCaptor = ArgumentCaptor.forClass(AddDeviceResponseData.class);

        useCase.execute(input);

        verify(mockDeviceRepository).save(deviceDataCaptor.capture());
        verify(mockOutputBoundary).present(responseCaptor.capture());

        assertTrue(responseCaptor.getValue().success);
        assertEquals("mouse-123", responseCaptor.getValue().newDeviceId);

        // Check logic mapping riêng của Mouse
        DeviceData savedData = deviceDataCaptor.getValue();
        assertEquals(16000, savedData.dpi);
        assertEquals(true, savedData.isWireless);
    }

    // 2. Test Lỗi Validation Riêng (DPI <= 0)
    @Test
    void test_execute_failure_invalidDpi() {
        AddMouseRequestData input = new AddMouseRequestData(
            "token", "Mouse", "Desc", BigDecimal.TEN, 10, "cat", "url",
            0, false, 3 // DPI = 0
        );

        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.existsByName("Mouse")).thenReturn(false);
        when(mockCategoryRepository.findById("cat")).thenReturn(new CategoryData());

        ArgumentCaptor<AddDeviceResponseData> captor = ArgumentCaptor.forClass(AddDeviceResponseData.class);
        useCase.execute(input);

        verify(mockDeviceRepository, never()).save(any());
        verify(mockOutputBoundary).present(captor.capture());

        assertFalse(captor.getValue().success);
        assertEquals("DPI phải lớn hơn 0.", captor.getValue().message);
    }

    // 3. Test Lỗi Database (System Crash) -> ĐÃ SỬA LỖI CAPTOR
    @Test
    void test_execute_failure_databaseCrash() {
        AddMouseRequestData input = new AddMouseRequestData(
            "token", "Mouse", "Desc", BigDecimal.TEN, 10, "cat", "url",
            1000, false, 3
        );

        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.existsByName("Mouse")).thenReturn(false);
        when(mockCategoryRepository.findById("cat")).thenReturn(new CategoryData());
        when(mockIdGenerator.generate()).thenReturn("mouse-123");

        // Giả lập lỗi Database
        doThrow(new RuntimeException("DB Error")).when(mockDeviceRepository).save(any(DeviceData.class));

        ArgumentCaptor<AddDeviceResponseData> captor = ArgumentCaptor.forClass(AddDeviceResponseData.class);
        
        // Act
        useCase.execute(input);

        // Assert
        // QUAN TRỌNG: Phải verify và capture trước khi gọi getValue()
        verify(mockOutputBoundary).present(captor.capture());

        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("Lỗi hệ thống"));
    }
}
