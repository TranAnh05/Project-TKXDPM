package Product.UpdateDevice;

import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageCategory.CategoryData;
import cgx.com.usecase.ManageCategory.ICategoryRepository;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageProduct.UpdateProduct.UpdateDeviceOutputBoundary;
import cgx.com.usecase.ManageProduct.UpdateProduct.UpdateDeviceResponseData;
import cgx.com.usecase.ManageProduct.UpdateProduct.UpdateMouseRequestData;
import cgx.com.usecase.ManageProduct.UpdateProduct.UpdateMouseUseCase;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdateMouseUseCaseTest {

    @Mock private IDeviceRepository mockDeviceRepository;
    @Mock private ICategoryRepository mockCategoryRepository;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private UpdateDeviceOutputBoundary mockOutputBoundary;

    private UpdateMouseUseCase useCase;
    private AuthPrincipal adminPrincipal;
    private DeviceData existingMouseData;

    @BeforeEach
    void setUp() {
        useCase = new UpdateMouseUseCase(
            mockDeviceRepository, mockCategoryRepository, mockTokenValidator, mockOutputBoundary
        );
        adminPrincipal = new AuthPrincipal("admin-1", "admin@test.com", UserRole.ADMIN);
        
        existingMouseData = new DeviceData();
        existingMouseData.id = "mouse-1";
        existingMouseData.name = "Old Mouse";
        existingMouseData.price = BigDecimal.TEN;
        existingMouseData.stockQuantity = 5;
        existingMouseData.categoryId = "cat-1";
        existingMouseData.dpi = 1000; // Mouse field
        existingMouseData.isWireless = true;
        existingMouseData.buttonCount = 5;
    }

    @Test
    void test_execute_success() {
        UpdateMouseRequestData input = new UpdateMouseRequestData(
            "token", "mouse-1", "New Mouse", "Desc", BigDecimal.TEN, 10, "cat-1", "url", "ACTIVE",
            16000, true, 5
        );

        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.findById("mouse-1")).thenReturn(existingMouseData);
        when(mockDeviceRepository.existsByName("New Mouse")).thenReturn(false);
        when(mockCategoryRepository.findById("cat-1")).thenReturn(new CategoryData());

        ArgumentCaptor<DeviceData> captor = ArgumentCaptor.forClass(DeviceData.class);
        ArgumentCaptor<UpdateDeviceResponseData> responseCaptor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);

        useCase.execute(input);

        verify(mockDeviceRepository).save(captor.capture());
        verify(mockOutputBoundary).present(responseCaptor.capture());

        assertTrue(responseCaptor.getValue().success);
        assertEquals(16000, captor.getValue().dpi);
    }

    @Test
    void test_execute_failure_invalidDpi() {
        UpdateMouseRequestData input = new UpdateMouseRequestData(
            "token", "mouse-1", "Name", "D", BigDecimal.TEN, 10, "c", "u", "A",
            0, true, 5 // DPI 0 -> Fail
        );

        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.findById("mouse-1")).thenReturn(existingMouseData);

        ArgumentCaptor<UpdateDeviceResponseData> captor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("DPI phải lớn hơn 0.", captor.getValue().message);
    }
}
