package Product.ViewDeviceDetail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageProduct.ViewDeviceDetail.ViewDeviceDetailOutputBoundary;
import cgx.com.usecase.ManageProduct.ViewDeviceDetail.ViewDeviceDetailRequestData;
import cgx.com.usecase.ManageProduct.ViewDeviceDetail.ViewDeviceDetailResponseData;
import cgx.com.usecase.ManageProduct.ViewDeviceDetail.ViewDeviceDetailUseCase;
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ViewDeviceDetailUseCaseTest {

    @Mock private IDeviceRepository mockDeviceRepository;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private ViewDeviceDetailOutputBoundary mockOutputBoundary;

    private ViewDeviceDetailUseCase useCase;
    private DeviceData activeDevice;
    private DeviceData inactiveDevice;

    @BeforeEach
    void setUp() {
        useCase = new ViewDeviceDetailUseCase(mockDeviceRepository, mockTokenValidator, mockOutputBoundary);

        activeDevice = new DeviceData();
        activeDevice.id = "dev-active";
        activeDevice.name = "Laptop";
        activeDevice.status = "ACTIVE";
        activeDevice.price = BigDecimal.TEN;

        inactiveDevice = new DeviceData();
        inactiveDevice.id = "dev-hidden";
        inactiveDevice.name = "Mouse";
        inactiveDevice.status = "INACTIVE";
    }

    // 1. Guest xem ACTIVE -> OK
    @Test
    void test_execute_guestViewActive_success() {
        ViewDeviceDetailRequestData input = new ViewDeviceDetailRequestData("dev-active", null);
        when(mockDeviceRepository.findById("dev-active")).thenReturn(activeDevice);

        ArgumentCaptor<ViewDeviceDetailResponseData> captor = ArgumentCaptor.forClass(ViewDeviceDetailResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertTrue(captor.getValue().success);
    }

    // 2. Guest xem INACTIVE -> Fail
    @Test
    void test_execute_guestViewInactive_failure() {
        ViewDeviceDetailRequestData input = new ViewDeviceDetailRequestData("dev-hidden", null);
        when(mockDeviceRepository.findById("dev-hidden")).thenReturn(inactiveDevice);

        ArgumentCaptor<ViewDeviceDetailResponseData> captor = ArgumentCaptor.forClass(ViewDeviceDetailResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Không tìm thấy sản phẩm.", captor.getValue().message);
    }

    // 3. Admin xem INACTIVE -> OK
    @Test
    void test_execute_adminViewInactive_success() {
        ViewDeviceDetailRequestData input = new ViewDeviceDetailRequestData("dev-hidden", "admin-token");
        when(mockDeviceRepository.findById("dev-hidden")).thenReturn(inactiveDevice);
        
        AuthPrincipal admin = new AuthPrincipal("admin", "e", UserRole.ADMIN);
        when(mockTokenValidator.validate("admin-token")).thenReturn(admin);

        ArgumentCaptor<ViewDeviceDetailResponseData> captor = ArgumentCaptor.forClass(ViewDeviceDetailResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertTrue(captor.getValue().success);
    }
    
    // 4. Customer xem INACTIVE -> Fail
    @Test
    void test_execute_customerViewInactive_failure() {
        ViewDeviceDetailRequestData input = new ViewDeviceDetailRequestData("dev-hidden", "cust-token");
        when(mockDeviceRepository.findById("dev-hidden")).thenReturn(inactiveDevice);
        
        AuthPrincipal customer = new AuthPrincipal("cust", "e", UserRole.CUSTOMER);
        when(mockTokenValidator.validate("cust-token")).thenReturn(customer);

        ArgumentCaptor<ViewDeviceDetailResponseData> captor = ArgumentCaptor.forClass(ViewDeviceDetailResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Không tìm thấy sản phẩm.", captor.getValue().message);
    }

    // 5. (TEST MỚI) Token Lỗi/Hết hạn xem INACTIVE -> Coi như Guest -> Fail
    @Test
    void test_execute_invalidTokenViewInactive_failure() {
        ViewDeviceDetailRequestData input = new ViewDeviceDetailRequestData("dev-hidden", "invalid-token");
        when(mockDeviceRepository.findById("dev-hidden")).thenReturn(inactiveDevice);
        
        // Giả lập Token Validator ném lỗi
        when(mockTokenValidator.validate("invalid-token")).thenThrow(new SecurityException("Expired"));

        ArgumentCaptor<ViewDeviceDetailResponseData> captor = ArgumentCaptor.forClass(ViewDeviceDetailResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        // Phải báo "Không tìm thấy" chứ không phải báo lỗi Token, vì đây là logic fallback
        assertEquals("Không tìm thấy sản phẩm.", captor.getValue().message);
    }

    // 6. Not Found DB
    @Test
    void test_execute_failure_notFoundDB() {
        ViewDeviceDetailRequestData input = new ViewDeviceDetailRequestData("dev-999", null);
        when(mockDeviceRepository.findById("dev-999")).thenReturn(null);

        ArgumentCaptor<ViewDeviceDetailResponseData> captor = ArgumentCaptor.forClass(ViewDeviceDetailResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Không tìm thấy sản phẩm.", captor.getValue().message); // Từ bước 3
    }
    
    // 7. Empty ID
    @Test
    void test_execute_failure_emptyId() {
        ViewDeviceDetailRequestData input = new ViewDeviceDetailRequestData("", null);
        ArgumentCaptor<ViewDeviceDetailResponseData> captor = ArgumentCaptor.forClass(ViewDeviceDetailResponseData.class);
        useCase.execute(input);
        
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("ID sản phẩm không được để trống.", captor.getValue().message);
    }

    // 8. System Error
    @Test
    void test_execute_failure_dbCrash() {
        ViewDeviceDetailRequestData input = new ViewDeviceDetailRequestData("dev-active", null);
        doThrow(new RuntimeException("DB Error")).when(mockDeviceRepository).findById(anyString());

        ArgumentCaptor<ViewDeviceDetailResponseData> captor = ArgumentCaptor.forClass(ViewDeviceDetailResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Lỗi hệ thống không xác định.", captor.getValue().message);
    }
}