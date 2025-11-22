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
import cgx.com.usecase.ManageProduct.AddNewProduct.AddLaptopRequestData;
import cgx.com.usecase.ManageProduct.AddNewProduct.AddLaptopUseCase;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IUserIdGenerator;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddLaptopUseCaseTest {

    @Mock private IDeviceRepository mockDeviceRepository;
    @Mock private ICategoryRepository mockCategoryRepository;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private IUserIdGenerator mockIdGenerator;
    @Mock private AddDeviceOutputBoundary mockOutputBoundary;

    private AddLaptopUseCase useCase;
    private AuthPrincipal adminPrincipal;

    @BeforeEach
    void setUp() {
        useCase = new AddLaptopUseCase(
            mockDeviceRepository, mockCategoryRepository, mockTokenValidator, mockIdGenerator, mockOutputBoundary
        );
        adminPrincipal = new AuthPrincipal("admin-1", "admin@test.com", UserRole.ADMIN);
    }

    // 1. Test Thành công (Happy Path)
    @Test
    void test_execute_success() {
        AddLaptopRequestData input = new AddLaptopRequestData(
            "token", "MacBook Pro", "Desc", new BigDecimal("2000.0"), 10, "cat-laptop", "url",
            "M3 Pro", "16GB", "512GB", 14.2
        );

        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.existsByName("MacBook Pro")).thenReturn(false);
        when(mockCategoryRepository.findById("cat-laptop")).thenReturn(new CategoryData());
        when(mockIdGenerator.generate()).thenReturn("laptop-123");

        ArgumentCaptor<DeviceData> deviceDataCaptor = ArgumentCaptor.forClass(DeviceData.class);
        ArgumentCaptor<AddDeviceResponseData> responseCaptor = ArgumentCaptor.forClass(AddDeviceResponseData.class);

        useCase.execute(input);

        verify(mockDeviceRepository).save(deviceDataCaptor.capture());
        verify(mockOutputBoundary).present(responseCaptor.capture());

        // Check response
        assertTrue(responseCaptor.getValue().success);
        assertEquals("laptop-123", responseCaptor.getValue().newDeviceId);

        // Check data saved to DB (Quan trọng: Check mapping riêng của Laptop)
        DeviceData savedData = deviceDataCaptor.getValue();
        assertEquals("MacBook Pro", savedData.name);
        assertEquals("M3 Pro", savedData.cpu); // Check logic mapSpecificDataToDTO
        assertEquals("16GB", savedData.ram);
        assertEquals(14.2, savedData.screenSize);
    }

    // 2. Test Lỗi Validation Riêng (RAM trống)
    @Test
    void test_execute_failure_invalidRam() {
        AddLaptopRequestData input = new AddLaptopRequestData(
            "token", "Laptop", "Desc", BigDecimal.TEN, 10, "cat", "url",
            "CPU", "", "SSD", 14.0 // RAM rỗng
        );

        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.existsByName("Laptop")).thenReturn(false);
        when(mockCategoryRepository.findById("cat")).thenReturn(new CategoryData());

        ArgumentCaptor<AddDeviceResponseData> captor = ArgumentCaptor.forClass(AddDeviceResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Thông tin RAM không được trống.", captor.getValue().message);
        verify(mockDeviceRepository, never()).save(any());
    }

    // 3. Test Lỗi Auth (Không phải Admin)
    @Test
    void test_execute_failure_notAdmin() {
        AuthPrincipal customer = new AuthPrincipal("u1", "e@e.com", UserRole.CUSTOMER);
        when(mockTokenValidator.validate("token")).thenReturn(customer);

        AddLaptopRequestData input = new AddLaptopRequestData("token", "L", "D", BigDecimal.ONE, 1, "c", "u", "c", "r", "s", 13.0);
        
        ArgumentCaptor<AddDeviceResponseData> captor = ArgumentCaptor.forClass(AddDeviceResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Không có quyền truy cập (Yêu cầu Admin).", captor.getValue().message);
    }

    // 4. Test Lỗi Trùng tên (Business Rule)
    @Test
    void test_execute_failure_duplicateName() {
        AddLaptopRequestData input = new AddLaptopRequestData("token", "Laptop A", "D", BigDecimal.ONE, 1, "c", "u", "c", "r", "s", 13.0);
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockDeviceRepository.existsByName("Laptop A")).thenReturn(true);

        ArgumentCaptor<AddDeviceResponseData> captor = ArgumentCaptor.forClass(AddDeviceResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Tên sản phẩm đã tồn tại.", captor.getValue().message);
    }
}