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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchDevicesUseCaseTest {

    @Mock
    private IDeviceRepository deviceRepository;
    @Mock
    private IAuthTokenValidator tokenValidator;
    @Mock
    private SearchDevicesOutputBoundary outputBoundary;

    private SearchDevicesUseCase searchDevicesUseCase;

    @BeforeEach
    void setUp() {
        searchDevicesUseCase = new SearchDevicesUseCase(deviceRepository, tokenValidator, outputBoundary);
    }

    // Case: Giá thấp nhất âm
    @Test
    void testExecute_WhenMinPriceNegative_ShouldFail() {
        // Arrange
        SearchDevicesRequestData input = createRequest(null, null, BigDecimal.valueOf(-10), null, null);

        // Act
        searchDevicesUseCase.execute(input);

        // Assert
        ArgumentCaptor<SearchDevicesResponseData> captor = ArgumentCaptor.forClass(SearchDevicesResponseData.class);
        verify(outputBoundary).present(captor.capture());
        SearchDevicesResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("Giá thấp nhất không được âm.", output.message);
        verifyNoInteractions(deviceRepository);
    }

    // Case: Giá cao nhất âm
    @Test
    void testExecute_WhenMaxPriceNegative_ShouldFail() {
        // Arrange
        SearchDevicesRequestData input = createRequest(null, null, null, BigDecimal.valueOf(-1), null);

        // Act
        searchDevicesUseCase.execute(input);

        // Assert
        ArgumentCaptor<SearchDevicesResponseData> captor = ArgumentCaptor.forClass(SearchDevicesResponseData.class);
        verify(outputBoundary).present(captor.capture());
        SearchDevicesResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("Giá cao nhất không được âm.", output.message);
    }

    // Case: Giá thấp nhất cao hơn giá cao nhất
    @Test
    void testExecute_WhenMinGreaterThanMax_ShouldFail() {
        // Arrange
        SearchDevicesRequestData input = createRequest(null, null, BigDecimal.valueOf(100), BigDecimal.valueOf(50), null);

        // Act
        searchDevicesUseCase.execute(input);

        // Assert
        ArgumentCaptor<SearchDevicesResponseData> captor = ArgumentCaptor.forClass(SearchDevicesResponseData.class);
        verify(outputBoundary).present(captor.capture());
        SearchDevicesResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("Giá thấp nhất không được lớn hơn giá cao nhất.", output.message);
    }

    // Case: Admin lọc theo status
    @Test
    void testExecute_AdminUser_CanFilterStatus() {
        // Arrange
        String token = "admin-token";
        String targetStatus = "DISCONTINUED"; // Admin muốn xem hàng ngừng kinh doanh
        SearchDevicesRequestData input = createRequest(token, null, null, null, targetStatus);

        // Mock User là ADMIN
        AuthPrincipal mockAdmin = new AuthPrincipal("admin1", "admin@gmail.com", UserRole.ADMIN);
        when(tokenValidator.validate(token)).thenReturn(mockAdmin);

        // Act
        searchDevicesUseCase.execute(input);

        // Assert
        ArgumentCaptor<DeviceSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(DeviceSearchCriteria.class);
        verify(deviceRepository).search(criteriaCaptor.capture(), anyInt(), anyInt());

        assertEquals("DISCONTINUED", criteriaCaptor.getValue().status);
    }

    // Case: Thành công
    @Test
    void testExecute_HappyPath_ShouldReturnResults() {
        // Arrange
        // Input: Page 1, Size 10
        SearchDevicesRequestData input = createRequest(null, "macbook", null, null, null);
        input.page = 1;
        input.size = 10;

        // Mock Data trả về từ DB
        List<DeviceData> mockResults = new ArrayList<>();
        mockResults.add(new DeviceData());
        
        long mockTotalCount = 55; 

        // Mock hành vi Repository
        when(deviceRepository.search(any(DeviceSearchCriteria.class), eq(0), eq(10))).thenReturn(mockResults);
        when(deviceRepository.count(any(DeviceSearchCriteria.class))).thenReturn(mockTotalCount);

        // Act
        searchDevicesUseCase.execute(input);

        // Assert
        ArgumentCaptor<SearchDevicesResponseData> captor = ArgumentCaptor.forClass(SearchDevicesResponseData.class);
        verify(outputBoundary).present(captor.capture());
        SearchDevicesResponseData output = captor.getValue();

        assertTrue(output.success);
        assertEquals("Tìm kiếm thành công.", output.message);
        
        assertEquals(1, output.devices.size());
        
        assertNotNull(output.pagination);
        assertEquals(55, output.pagination.totalCount);
        assertEquals(1, output.pagination.currentPage);
        assertEquals(10, output.pagination.pageSize);
        assertEquals(6, output.pagination.totalPages);
    }

    private SearchDevicesRequestData createRequest(String token, String keyword, BigDecimal min, BigDecimal max, String status) {
        return new SearchDevicesRequestData(token, keyword, null, min, max, status, 1, 10);
    }
}