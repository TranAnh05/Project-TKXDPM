package Product.UpdateDevice;

import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageCategory.CategoryData;
import cgx.com.usecase.ManageCategory.ICategoryRepository;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageProduct.UpdateProduct.UpdateDeviceOutputBoundary;
import cgx.com.usecase.ManageProduct.UpdateProduct.UpdateDeviceResponseData;
import cgx.com.usecase.ManageProduct.UpdateProduct.UpdateLaptopRequestData;
import cgx.com.usecase.ManageProduct.UpdateProduct.UpdateLaptopUseCase;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;

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
public class UpdateLaptopUseCaseTest {

    @Mock private IDeviceRepository mockDeviceRepository;
    @Mock private ICategoryRepository mockCategoryRepository;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private UpdateDeviceOutputBoundary mockOutputBoundary;

    private UpdateLaptopUseCase useCase;
    private AuthPrincipal adminPrincipal;
    private DeviceData existingLaptopData;

    @BeforeEach
    void setUp() {
        useCase = new UpdateLaptopUseCase(
            mockDeviceRepository, mockCategoryRepository, mockTokenValidator, mockOutputBoundary
        );
        adminPrincipal = new AuthPrincipal("admin-1", "admin@test.com", UserRole.ADMIN);
        
        // Dữ liệu Laptop mẫu trong DB
        existingLaptopData = new DeviceData();
        existingLaptopData.id = "lap-1";
        existingLaptopData.name = "Old Mac";
        existingLaptopData.price = new BigDecimal("1000");
        existingLaptopData.stockQuantity = 5;
        existingLaptopData.categoryId = "cat-1";
        existingLaptopData.status = "ACTIVE";
        existingLaptopData.createdAt = Instant.now();
        existingLaptopData.updatedAt = Instant.now();
        existingLaptopData.cpu = "M1"; 
        existingLaptopData.ram = "8GB";
        existingLaptopData.screenSize = 13.3;
    }

    // --- NHÓM 1: AUTH & INPUT VALIDATION CHUNG ---

    @Test
    void test_execute_failure_emptyToken() {
        UpdateLaptopRequestData input = new UpdateLaptopRequestData(
            "", "lap-1", "Name", "Desc", BigDecimal.TEN, 1, "c", "t", "S", "cpu", "ram", "s", 14.0
        );
        
        
        ArgumentCaptor<UpdateDeviceResponseData> captor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        useCase.execute(input); // Chưa gọi validate token

        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Auth Token không được để trống.", captor.getValue().message);
    }

    @Test
    void test_execute_failure_notAdmin() {
        AuthPrincipal customer = new AuthPrincipal("u1", "e@e.com", UserRole.CUSTOMER);
        when(mockTokenValidator.validate("token")).thenReturn(customer);

        UpdateLaptopRequestData input = new UpdateLaptopRequestData(
            "token", "lap-1", "N", "D", BigDecimal.ONE, 1, "c", "t", "S", "c", "r", "s", 14.0
        );
        
        ArgumentCaptor<UpdateDeviceResponseData> captor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Không có quyền truy cập (Yêu cầu Admin).", captor.getValue().message);
    }

    @Test
    void test_execute_failure_emptyId() {
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        UpdateLaptopRequestData input = new UpdateLaptopRequestData(
            "token", "", "N", "D", BigDecimal.ONE, 1, "c", "t", "S", "c", "r", "s", 14.0
        );

        ArgumentCaptor<UpdateDeviceResponseData> captor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("ID sản phẩm không được để trống.", captor.getValue().message);
    }

    // --- NHÓM 2: BUSINESS RULES (NOT FOUND, MISMATCH) ---

    @Test
    void test_execute_failure_notFound() {
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.findById("lap-999")).thenReturn(null);

        UpdateLaptopRequestData input = new UpdateLaptopRequestData(
            "token", "lap-999", "N", "D", BigDecimal.ONE, 1, "c", "t", "S", "c", "r", "s", 14.0
        );

        ArgumentCaptor<UpdateDeviceResponseData> captor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Không tìm thấy sản phẩm với ID: lap-999", captor.getValue().message);
    }

    @Test
    void test_execute_failure_mismatchType() {
        // Giả lập tìm thấy ID nhưng dữ liệu là Mouse (không có CPU)
        DeviceData mouseData = new DeviceData();
        mouseData.id = "mouse-1";
        mouseData.dpi = 1000; 
        // cpu = null

        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.findById("mouse-1")).thenReturn(mouseData);

        UpdateLaptopRequestData input = new UpdateLaptopRequestData(
            "token", "mouse-1", "N", "D", BigDecimal.ONE, 1, "c", "t", "S", "c", "r", "s", 14.0
        );

        ArgumentCaptor<UpdateDeviceResponseData> captor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("không hợp lệ hoặc không khớp loại thiết bị"));
    }

    // --- NHÓM 3: VALIDATION CHI TIẾT (CHUNG & RIÊNG) ---

    @Test
    void test_execute_failure_invalidCommonData_PriceNegative() {
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.findById("lap-1")).thenReturn(existingLaptopData);

        UpdateLaptopRequestData input = new UpdateLaptopRequestData(
            "token", "lap-1", "Name", "D", new BigDecimal("-100"), 1, "c", "t", "S", "c", "r", "s", 14.0
        );

        ArgumentCaptor<UpdateDeviceResponseData> captor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Giá bán không hợp lệ.", captor.getValue().message); // Từ Entity
    }

    @Test
    void test_execute_failure_invalidSpecificData_EmptyCpu() {
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.findById("lap-1")).thenReturn(existingLaptopData);

        UpdateLaptopRequestData input = new UpdateLaptopRequestData(
            "token", "lap-1", "Name", "D", BigDecimal.TEN, 1, "c", "t", "S", 
            "", "Ram", "Storage", 14.0 // CPU rỗng
        );

        ArgumentCaptor<UpdateDeviceResponseData> captor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Thông tin CPU không được trống.", captor.getValue().message); // Từ Laptop Entity
    }

    // --- NHÓM 4: BUSINESS RULES (DUPLICATE, CATEGORY) ---

    @Test
    void test_execute_failure_duplicateName() {
        // Tên mới "New Name" đã tồn tại
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.findById("lap-1")).thenReturn(existingLaptopData);
        when(mockDeviceRepository.existsByName("New Name")).thenReturn(true);

        UpdateLaptopRequestData input = new UpdateLaptopRequestData(
            "token", "lap-1", "New Name", "D", BigDecimal.TEN, 1, "c", "t", "S", "c", "r", "s", 14.0
        );

        ArgumentCaptor<UpdateDeviceResponseData> captor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Tên sản phẩm mới đã tồn tại.", captor.getValue().message);
    }

    @Test
    void test_execute_failure_categoryNotFound() {
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.findById("lap-1")).thenReturn(existingLaptopData);
        // Tên không đổi, bỏ qua check duplicate
        when(mockCategoryRepository.findById("invalid-cat")).thenReturn(null);

        UpdateLaptopRequestData input = new UpdateLaptopRequestData(
            "token", "lap-1", "Old Mac", "D", BigDecimal.TEN, 1, "invalid-cat", "t", "S", "c", "r", "s", 14.0
        );

        ArgumentCaptor<UpdateDeviceResponseData> captor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        useCase.execute(input);
        
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Danh mục không tồn tại.", captor.getValue().message);
    }

    // --- NHÓM 5: SUCCESS & SYSTEM ERROR ---

    @Test
    void test_execute_success() {
        // Mọi điều kiện OK
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.findById("lap-1")).thenReturn(existingLaptopData);
        when(mockCategoryRepository.findById("cat-new")).thenReturn(new CategoryData());

        UpdateLaptopRequestData input = new UpdateLaptopRequestData(
            "token", "lap-1", "Old Mac", "D", new BigDecimal("2000"), 5, "cat-new", "url", "ACTIVE",
            "M2", "16GB", "1TB", 14.0
        );

        ArgumentCaptor<DeviceData> dataCaptor = ArgumentCaptor.forClass(DeviceData.class);
        ArgumentCaptor<UpdateDeviceResponseData> responseCaptor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);

        useCase.execute(input);

        verify(mockDeviceRepository).save(dataCaptor.capture());
        verify(mockOutputBoundary).present(responseCaptor.capture());

        assertTrue(responseCaptor.getValue().success);
        
        // Check Mapping
        DeviceData saved = dataCaptor.getValue();
        assertEquals("M2", saved.cpu);
        assertEquals("16GB", saved.ram);
        assertEquals(new BigDecimal("2000"), saved.price);
    }

    @Test
    void test_execute_failure_dbCrash() {
        // Mọi điều kiện OK nhưng DB lỗi khi Save
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.findById("lap-1")).thenReturn(existingLaptopData);
        when(mockCategoryRepository.findById("cat-new")).thenReturn(new CategoryData());
        
        doThrow(new RuntimeException("DB Down")).when(mockDeviceRepository).save(any());

        UpdateLaptopRequestData input = new UpdateLaptopRequestData(
            "token", "lap-1", "Old Mac", "D", BigDecimal.TEN, 1, "cat-new", "u", "A", "c", "r", "s", 14.0
        );

        ArgumentCaptor<UpdateDeviceResponseData> captor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("Lỗi hệ thống"));
    }
}
