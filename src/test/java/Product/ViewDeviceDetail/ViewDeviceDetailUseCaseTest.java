package Product.ViewDeviceDetail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import cgx.com.usecase.ManageProduct.ViewDeviceDetail.ViewDeviceDetailOutputBoundary;
import cgx.com.usecase.ManageProduct.ViewDeviceDetail.ViewDeviceDetailRequestData;
import cgx.com.usecase.ManageProduct.ViewDeviceDetail.ViewDeviceDetailResponseData;
import cgx.com.usecase.ManageProduct.ViewDeviceDetail.ViewDeviceDetailUseCase;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViewDeviceDetailUseCaseTest {

    @Mock
    private IDeviceRepository deviceRepository;

    @Mock
    private IAuthTokenValidator tokenValidator; 

    @Mock
    private ViewDeviceDetailOutputBoundary outputBoundary;

    private ViewDeviceDetailUseCase viewDeviceDetailUseCase;

    @BeforeEach
    void setUp() {
        viewDeviceDetailUseCase = new ViewDeviceDetailUseCase(deviceRepository, tokenValidator, outputBoundary);
    }

    @Test
    @DisplayName("TC1: Thất bại khi ID sản phẩm không hợp lệ (Null/Empty)")
    void testExecute_WhenIdInvalid_ShouldFail() {
        // Arrange
        ViewDeviceDetailRequestData input = new ViewDeviceDetailRequestData();
        input.deviceId = ""; // ID rỗng sẽ kích hoạt lỗi từ ComputerDevice.validateId

        // Act
        viewDeviceDetailUseCase.execute(input);

        // Assert
        ArgumentCaptor<ViewDeviceDetailResponseData> captor = ArgumentCaptor.forClass(ViewDeviceDetailResponseData.class);
        verify(outputBoundary).present(captor.capture());
        ViewDeviceDetailResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("ID sản phẩm không được để trống.", output.message);
        
        verify(deviceRepository, never()).findById(any());
    }

    @Test
    @DisplayName("TC2: Thất bại khi không tìm thấy sản phẩm trong Database")
    void testExecute_WhenDeviceNotFound_ShouldFail() {
        // Arrange
        String deviceId = "unknown-id";
        ViewDeviceDetailRequestData input = new ViewDeviceDetailRequestData();
        input.deviceId = deviceId;

        when(deviceRepository.findById(deviceId)).thenReturn(null);

        // Act
        viewDeviceDetailUseCase.execute(input);

        // Assert
        ArgumentCaptor<ViewDeviceDetailResponseData> captor = ArgumentCaptor.forClass(ViewDeviceDetailResponseData.class);
        verify(outputBoundary).present(captor.capture());
        ViewDeviceDetailResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("Không tìm thấy sản phẩm.", output.message);
    }

    @Test
    @DisplayName("TC4: Thành công khi tìm thấy sản phẩm")
    void testExecute_WhenDeviceFound_ShouldSucceed() {
        // Arrange
        String deviceId = "iphone-15";
        ViewDeviceDetailRequestData input = new ViewDeviceDetailRequestData();
        input.deviceId = deviceId;

        DeviceData mockDevice = new DeviceData();
        mockDevice.id = deviceId;
        mockDevice.name = "iPhone 15";
        mockDevice.status = "AVAILABLE"; 

        when(deviceRepository.findById(deviceId)).thenReturn(mockDevice);

        // Act
        viewDeviceDetailUseCase.execute(input);

        // Assert
        ArgumentCaptor<ViewDeviceDetailResponseData> captor = ArgumentCaptor.forClass(ViewDeviceDetailResponseData.class);
        verify(outputBoundary).present(captor.capture());
        ViewDeviceDetailResponseData output = captor.getValue();

        assertTrue(output.success);
        assertEquals("Lấy thông tin sản phẩm thành công.", output.message);
        assertNotNull(output.device);
        assertEquals(deviceId, output.device.id);
        assertEquals("iPhone 15", output.device.name);
    }
}