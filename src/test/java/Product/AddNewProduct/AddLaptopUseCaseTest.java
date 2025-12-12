package Product.AddNewProduct;

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
import cgx.com.usecase.ManageCategory.CategoryData;
import cgx.com.usecase.ManageCategory.ICategoryRepository;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageProduct.IProductIdGenerator;
import cgx.com.usecase.ManageProduct.AddNewProduct.AddDeviceOutputBoundary;
import cgx.com.usecase.ManageProduct.AddNewProduct.AddDeviceResponseData;
import cgx.com.usecase.ManageProduct.AddNewProduct.AddLaptopRequestData;
import cgx.com.usecase.ManageProduct.AddNewProduct.AddLaptopUseCase;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddLaptopUseCaseTest {

    @Mock private IDeviceRepository deviceRepository;
    @Mock private ICategoryRepository categoryRepository;
    @Mock private IAuthTokenValidator tokenValidator;
    @Mock private IProductIdGenerator idGenerator;
    @Mock private AddDeviceOutputBoundary outputBoundary;

    private AddLaptopUseCase addLaptopUseCase;

    @BeforeEach
    void setUp() {
        addLaptopUseCase = new AddLaptopUseCase(
                deviceRepository, categoryRepository, tokenValidator, idGenerator, outputBoundary
        );
    }

    // Helper method để tạo request hợp lệ, giúp code test gọn hơn
    private AddLaptopRequestData createValidRequest() {
        return new AddLaptopRequestData(
                "valid-admin-token",
                "MacBook Pro M2",
                "Laptop cao cấp",
                new BigDecimal("30000000"),
                10,
                "CAT_LAPTOP",
                "thumb.jpg",
                "Apple M2",
                "16GB",
                "512GB SSD",
                13.6
        );
    }

    @Test
    @DisplayName("Fail: User không phải là Admin")
    void execute_ShouldFail_WhenUserIsNotAdmin() {
        // Arrange
        AddLaptopRequestData input = createValidRequest();
        AuthPrincipal userPrincipal = new AuthPrincipal("user1", "user@mail.com", UserRole.CUSTOMER);
        when(tokenValidator.validate(input.authToken)).thenReturn(userPrincipal);

        // Act
        addLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<AddDeviceResponseData> captor = ArgumentCaptor.forClass(AddDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AddDeviceResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("Không có quyền truy cập.", output.message);
    }

    @Test
    @DisplayName("Fail: Thông tin chung không hợp lệ (Tên rỗng)")
    void execute_ShouldFail_WhenCommonInfoIsInvalid() {
        // Arrange
        AddLaptopRequestData input = new AddLaptopRequestData(
                "token", "", "Desc", BigDecimal.TEN, 5, "cat", "img", 
                "CPU", "RAM", "HDD", 14.0
        ); 
        
        AuthPrincipal adminPrincipal = new AuthPrincipal("admin", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);

        // Act
        addLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<AddDeviceResponseData> captor = ArgumentCaptor.forClass(AddDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AddDeviceResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("Tên sản phẩm không được để trống.", output.message);
    }
    
    @Test
    @DisplayName("Fail: Thông tin chung không hợp lệ - mô tả rỗng")
    void execute_ShouldFail_WhenCommonInfoIsInvalid02() {
        // Arrange
        AddLaptopRequestData input = new AddLaptopRequestData(
                "token", "name", "", BigDecimal.TEN, 5, "cat", "img", 
                "CPU", "RAM", "HDD", 14.0
        ); 
        
        AuthPrincipal adminPrincipal = new AuthPrincipal("admin", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);

        // Act
        addLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<AddDeviceResponseData> captor = ArgumentCaptor.forClass(AddDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AddDeviceResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("Mô tả sản phẩm không được để trống.", output.message);
    }
    
    @Test
    @DisplayName("Fail: Thông tin chung không hợp lệ - mô tả rỗng")
    void execute_ShouldFail_WhenCommonInfoIsInvalid03() {
        // Arrange
        AddLaptopRequestData input = new AddLaptopRequestData(
                "token", "name", "des", BigDecimal.valueOf(-1), 5, "cat", "img", 
                "CPU", "RAM", "HDD", 14.0
        ); 
        
        AuthPrincipal adminPrincipal = new AuthPrincipal("admin", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);

        // Act
        addLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<AddDeviceResponseData> captor = ArgumentCaptor.forClass(AddDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AddDeviceResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("Giá bán không hợp lệ.", output.message);
    }
    
    @Test
    @DisplayName("Fail: Thông tin chung không hợp lệ - mô tả rỗng")
    void execute_ShouldFail_WhenCommonInfoIsInvalid04() {
        // Arrange
        AddLaptopRequestData input = new AddLaptopRequestData(
                "token", "name", "des", BigDecimal.TEN, -2, "cat", "img", 
                "CPU", "RAM", "HDD", 14.0
        ); 
        
        AuthPrincipal adminPrincipal = new AuthPrincipal("admin", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);

        // Act
        addLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<AddDeviceResponseData> captor = ArgumentCaptor.forClass(AddDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AddDeviceResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("Số lượng tồn kho không được âm.", output.message);
    }

    @Test
    @DisplayName("Fail: Thông tin Laptop không hợp lệ")
    void execute_ShouldFail_WhenSpecificInfoIsInvalid() {
        // Arrange
        AddLaptopRequestData input = new AddLaptopRequestData(
                "token", "Dell", "Desc", BigDecimal.TEN, 5, "cat", "img", 
                "", "RAM", "HDD", 14.0
        ); 
        
        AuthPrincipal adminPrincipal = new AuthPrincipal("admin", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);

        // Act
        addLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<AddDeviceResponseData> captor = ArgumentCaptor.forClass(AddDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AddDeviceResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("Thông tin CPU không được trống.", output.message);
    }
    
    @Test
    @DisplayName("Fail: Thông tin Laptop không hợp lệ")
    void execute_ShouldFail_WhenSpecificInfoIsInvalid02() {
        // Arrange
        AddLaptopRequestData input = new AddLaptopRequestData(
                "token", "Dell", "Desc", BigDecimal.TEN, 5, "cat", "img", 
                "I7", "", "HDD", 14.0
        ); 
        
        AuthPrincipal adminPrincipal = new AuthPrincipal("admin", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);

        // Act
        addLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<AddDeviceResponseData> captor = ArgumentCaptor.forClass(AddDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AddDeviceResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("Thông tin RAM không được trống.", output.message);
    }
    
    @Test
    @DisplayName("Fail: Thông tin Laptop không hợp lệ")
    void execute_ShouldFail_WhenSpecificInfoIsInvalid03() {
        // Arrange
        AddLaptopRequestData input = new AddLaptopRequestData(
                "token", "Dell", "Desc", BigDecimal.TEN, 5, "cat", "img", 
                "I7", "ram", "", 14.0
        ); 
        
        AuthPrincipal adminPrincipal = new AuthPrincipal("admin", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);

        // Act
        addLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<AddDeviceResponseData> captor = ArgumentCaptor.forClass(AddDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AddDeviceResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("Thông tin Storage không được trống.", output.message);
    }
    
    @Test
    @DisplayName("Fail: Thông tin Laptop không hợp lệ")
    void execute_ShouldFail_WhenSpecificInfoIsInvalid04() {
        // Arrange
        AddLaptopRequestData input = new AddLaptopRequestData(
                "token", "Dell", "Desc", BigDecimal.TEN, 5, "cat", "img", 
                "I7", "ram", "HDD", -2
        ); 
        
        AuthPrincipal adminPrincipal = new AuthPrincipal("admin", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);

        // Act
        addLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<AddDeviceResponseData> captor = ArgumentCaptor.forClass(AddDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AddDeviceResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("Kích thước màn hình không hợp lệ.", output.message);
    }


    @Test
    @DisplayName("Fail: Tên sản phẩm đã tồn tại trong DB")
    void execute_ShouldFail_WhenProductNameExists() {
        // Arrange
        AddLaptopRequestData input = createValidRequest();
        AuthPrincipal adminPrincipal = new AuthPrincipal("admin", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);
        
        // Mock DB báo trùng tên
        when(deviceRepository.existsByName(input.name)).thenReturn(true);

        // Act
        addLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<AddDeviceResponseData> captor = ArgumentCaptor.forClass(AddDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AddDeviceResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("Tên sản phẩm đã tồn tại.", output.message);
    }

    @Test
    @DisplayName("Fail: Danh mục (Category) không tồn tại")
    void execute_ShouldFail_WhenCategoryNotFound() {
        // Arrange
        AddLaptopRequestData input = createValidRequest();
        AuthPrincipal adminPrincipal = new AuthPrincipal("admin", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);
        
        when(deviceRepository.existsByName(input.name)).thenReturn(false);
        when(categoryRepository.findById(input.categoryId)).thenReturn(null);

        // Act
        addLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<AddDeviceResponseData> captor = ArgumentCaptor.forClass(AddDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AddDeviceResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("Danh mục không tồn tại.", output.message);
    }

    @Test
    @DisplayName("Success: Thêm Laptop thành công")
    void execute_ShouldSucceed_WhenAllInputsAreValid() {
        // Arrange
        AddLaptopRequestData input = createValidRequest();
        AuthPrincipal adminPrincipal = new AuthPrincipal("admin", "admin@mail.com", UserRole.ADMIN);
        
        // 1. Token & Role OK
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);
        // 2. Tên chưa trùng
        when(deviceRepository.existsByName(input.name)).thenReturn(false);
        // 3. Category tồn tại
        CategoryData mockCategory = new CategoryData("CAT_LAPTOP", "Laptops", "", null, Instant.now(), Instant.now());
        when(categoryRepository.findById(input.categoryId)).thenReturn(mockCategory);
        // 4. Generate ID mới
        when(idGenerator.generate()).thenReturn("NEW_LAPTOP_ID");

        // Act
        addLaptopUseCase.execute(input);

        // Assert Output
        ArgumentCaptor<AddDeviceResponseData> outputCaptor = ArgumentCaptor.forClass(AddDeviceResponseData.class);
        verify(outputBoundary).present(outputCaptor.capture());
        AddDeviceResponseData output = outputCaptor.getValue();

        assertTrue(output.success);
        assertEquals("Thêm sản phẩm thành công!", output.message);
        assertEquals("NEW_LAPTOP_ID", output.newDeviceId);

        // Assert Data được lưu vào DB
        ArgumentCaptor<DeviceData> saveCaptor = ArgumentCaptor.forClass(DeviceData.class);
        verify(deviceRepository).save(saveCaptor.capture());
        DeviceData savedData = saveCaptor.getValue();

        // Kiểm tra dữ liệu chung (Common)
        assertEquals("NEW_LAPTOP_ID", savedData.id);
        assertEquals("MacBook Pro M2", savedData.name);
        assertEquals(new BigDecimal("30000000"), savedData.price);
        assertEquals("AVAILABLE", savedData.status);

        // Kiểm tra dữ liệu riêng (Specific - Laptop)
        assertEquals("Apple M2", savedData.cpu);
        assertEquals("16GB", savedData.ram);
        assertEquals("512GB SSD", savedData.storage);
        assertEquals(13.6, savedData.screenSize);
    }
}