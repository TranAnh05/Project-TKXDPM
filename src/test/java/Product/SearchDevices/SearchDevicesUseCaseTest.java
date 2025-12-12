package Product.SearchDevices;

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
import cgx.com.usecase.ManageProduct.SearchDevices.DeviceSearchCriteria;
import cgx.com.usecase.ManageProduct.SearchDevices.SearchDevicesOutputBoundary;
import cgx.com.usecase.ManageProduct.SearchDevices.SearchDevicesRequestData;
import cgx.com.usecase.ManageProduct.SearchDevices.SearchDevicesResponseData;
import cgx.com.usecase.ManageProduct.SearchDevices.SearchDevicesUseCase;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SearchDevicesUseCaseTest {

    @Mock private IDeviceRepository mockDeviceRepository;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private SearchDevicesOutputBoundary mockOutputBoundary;

    private SearchDevicesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SearchDevicesUseCase(mockDeviceRepository, mockTokenValidator, mockOutputBoundary);
    }

    /**
     * Case 1: Guest tìm kiếm (Token null)
     * -> Hệ thống phải tự động gán status = "ACTIVE" vào tiêu chí tìm kiếm.
     */
    @Test
    void test_execute_guestSearch_forceActive() {
        // ARRANGE
        SearchDevicesRequestData input = new SearchDevicesRequestData(
            null, "Laptop", null, null, null, null, 1, 10
        );

        when(mockDeviceRepository.search(any(DeviceSearchCriteria.class), eq(0), eq(10)))
            .thenReturn(List.of(new DeviceData()));
        when(mockDeviceRepository.count(any(DeviceSearchCriteria.class))).thenReturn(1L);

        ArgumentCaptor<DeviceSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(DeviceSearchCriteria.class);
        ArgumentCaptor<SearchDevicesResponseData> responseCaptor = ArgumentCaptor.forClass(SearchDevicesResponseData.class);

        // ACT
        useCase.execute(input);

        // ASSERT
        verify(mockDeviceRepository).search(criteriaCaptor.capture(), eq(0), eq(10));
        verify(mockOutputBoundary).present(responseCaptor.capture());

        // Kiểm tra logic nghiệp vụ: Guest phải bị ép tìm "ACTIVE"
        assertEquals("ACTIVE", criteriaCaptor.getValue().status);
        assertEquals("Laptop", criteriaCaptor.getValue().keyword);
        
        assertTrue(responseCaptor.getValue().success);
    }

    /**
     * Case 2: Admin tìm kiếm (Token Admin)
     * -> Hệ thống cho phép tìm theo statusFilter (ví dụ: null để tìm tất cả, hoặc tìm "OUT_OF_STOCK").
     */
    @Test
    void test_execute_adminSearch_customStatus() {
        // ARRANGE
        // Admin muốn tìm hàng OUT_OF_STOCK
        SearchDevicesRequestData input = new SearchDevicesRequestData(
            "admin-token", null, null, null, null, "OUT_OF_STOCK", 1, 10
        );
        
        AuthPrincipal admin = new AuthPrincipal("admin", "e", UserRole.ADMIN);
        when(mockTokenValidator.validate("admin-token")).thenReturn(admin);

        when(mockDeviceRepository.search(any(), anyInt(), anyInt())).thenReturn(List.of());
        when(mockDeviceRepository.count(any())).thenReturn(0L);

        ArgumentCaptor<DeviceSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(DeviceSearchCriteria.class);

        // ACT
        useCase.execute(input);

        // ASSERT
        verify(mockDeviceRepository).search(criteriaCaptor.capture(), anyInt(), anyInt());
        
        // Admin được phép tìm OUT_OF_STOCK
        assertEquals("OUT_OF_STOCK", criteriaCaptor.getValue().status);
    }

    /**
     * Case 3: Token Lỗi/Hết hạn -> Coi như Guest -> Force ACTIVE
     */
    @Test
    void test_execute_invalidToken_fallbackToGuest() {
        SearchDevicesRequestData input = new SearchDevicesRequestData(
            "invalid-token", "Mouse", null, null, null, "DELETED", 1, 10
        );
        
        // Giả lập token lỗi
        when(mockTokenValidator.validate("invalid-token")).thenThrow(new SecurityException("Expired"));
        
        when(mockDeviceRepository.search(any(), anyInt(), anyInt())).thenReturn(List.of());

        ArgumentCaptor<DeviceSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(DeviceSearchCriteria.class);

        useCase.execute(input);

        verify(mockDeviceRepository).search(criteriaCaptor.capture(), anyInt(), anyInt());
        
        // Dù gửi statusFilter="DELETED", nhưng vì token lỗi -> Guest -> ACTIVE
        assertEquals("ACTIVE", criteriaCaptor.getValue().status);
    }

    /**
     * Case 4: DB Crash (System Error)
     */
    @Test
    void test_execute_failure_dbCrash() {
        SearchDevicesRequestData input = new SearchDevicesRequestData(null, "", null, null, null, null, 1, 10);
        
        doThrow(new RuntimeException("DB Error")).when(mockDeviceRepository).search(any(), anyInt(), anyInt());

        ArgumentCaptor<SearchDevicesResponseData> captor = ArgumentCaptor.forClass(SearchDevicesResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Lỗi hệ thống không xác định.", captor.getValue().message);
    }
    
    /**
     * Case 5: Giá nghịch lý (Min > Max) -> Phải báo lỗi Validation
     */
    @Test
    void test_execute_failure_invalidPriceRange() {
        // Min = 100, Max = 50 -> Vô lý
        SearchDevicesRequestData input = new SearchDevicesRequestData(
            null, "Laptop", null, new BigDecimal("100"), new BigDecimal("50"), null, 1, 10
        );

        ArgumentCaptor<SearchDevicesResponseData> responseCaptor = ArgumentCaptor.forClass(SearchDevicesResponseData.class);

        // ACT
        useCase.execute(input);

        // ASSERT
        verify(mockDeviceRepository, never()).search(any(), anyInt(), anyInt()); // Không bao giờ gọi DB
        verify(mockOutputBoundary).present(responseCaptor.capture());

        assertFalse(responseCaptor.getValue().success);
        assertEquals("Giá thấp nhất không được lớn hơn giá cao nhất.", responseCaptor.getValue().message);
    }

    /**
     * Case 6: Giá âm -> Phải báo lỗi
     */
    @Test
    void test_execute_failure_negativePrice() {
        // Min = -10
        SearchDevicesRequestData input = new SearchDevicesRequestData(
            null, "Laptop", null, new BigDecimal("-10"), null, null, 1, 10
        );

        ArgumentCaptor<SearchDevicesResponseData> responseCaptor = ArgumentCaptor.forClass(SearchDevicesResponseData.class);

        useCase.execute(input);

        verify(mockOutputBoundary).present(responseCaptor.capture());
        assertFalse(responseCaptor.getValue().success);
        assertEquals("Giá thấp nhất không được âm.", responseCaptor.getValue().message);
    }
}
