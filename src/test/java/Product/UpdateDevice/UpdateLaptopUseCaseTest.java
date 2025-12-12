package Product.UpdateDevice;

import cgx.com.Entities.UserRole;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageCategory.CategoryData;
import cgx.com.usecase.ManageCategory.ICategoryRepository;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageProduct.UpdateProduct.UpdateDeviceOutputBoundary;
import cgx.com.usecase.ManageProduct.UpdateProduct.UpdateDeviceResponseData;
import cgx.com.usecase.ManageProduct.UpdateProduct.UpdateLaptopRequestData;
import cgx.com.usecase.ManageProduct.UpdateProduct.UpdateLaptopUseCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
class UpdateLaptopUseCaseTest {

    @Mock private IDeviceRepository deviceRepository;
    @Mock private ICategoryRepository categoryRepository;
    @Mock private IAuthTokenValidator tokenValidator;
    @Mock private UpdateDeviceOutputBoundary outputBoundary;

    private UpdateLaptopUseCase updateLaptopUseCase;

    @BeforeEach
    void setUp() {
        updateLaptopUseCase = new UpdateLaptopUseCase(
                deviceRepository, categoryRepository, tokenValidator, outputBoundary
        );
    }

    private UpdateLaptopRequestData createValidRequest() {
        return new UpdateLaptopRequestData(
            "valid-token",           // authToken
            "LAPTOP-001",            // id
            "MacBook Pro M3",        // name
            "Updated Description",   // description
            new BigDecimal("50000000"), // price
            10,                      // stockQuantity
            "CAT-APPLE",             // categoryId
            "thumb.jpg",             // thumbnail
            "AVAILABLE",             // status (Enum ProductAvailability)
            "M3 Pro",                // cpu
            "18GB",                  // ram
            "512GB SSD",             // storage
            14.2                     // screenSize
        );
    }

    private DeviceData createExistingDeviceData() {
        DeviceData data = new DeviceData();
        data.id = "LAPTOP-001";
        data.name = "MacBook Pro M2"; // Tên cũ khác tên mới
        data.description = "Old Desc";
        data.price = new BigDecimal("45000000");
        data.stockQuantity = 5;
        data.categoryId = "CAT-APPLE";
        data.status = "AVAILABLE";
        data.thumbnail = "old.jpg";
        data.createdAt = Instant.now();
        data.updatedAt = Instant.now();
        
        data.cpu = "M2 Pro";
        data.ram = "16GB";
        data.storage = "512GB";
        data.screenSize = 14.2;
        return data;
    }


    @Test
    @DisplayName("Fail: User không phải Admin")
    void execute_ShouldFail_WhenUserIsNotAdmin() {
        // Arrange
        UpdateLaptopRequestData input = createValidRequest();
        AuthPrincipal userPrincipal = new AuthPrincipal("u1", "user@mail.com", UserRole.CUSTOMER);
        when(tokenValidator.validate(input.authToken)).thenReturn(userPrincipal);

        // Act
        updateLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<UpdateDeviceResponseData> captor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Không có quyền truy cập.", captor.getValue().message); //
    }

    @Test
    @DisplayName("Fail: ID sản phẩm rỗng (Validation ID)")
    void execute_ShouldFail_WhenIdIsEmpty() {
        UpdateLaptopRequestData input = new UpdateLaptopRequestData(
            "token", "", "Name", "Desc", BigDecimal.TEN, 10, "Cat", "Img", "AVAILABLE", 
            "CPU", "RAM", "HDD", 14.0
        );
        
        AuthPrincipal admin = new AuthPrincipal("admin", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(anyString())).thenReturn(admin);

        // Act
        updateLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<UpdateDeviceResponseData> captor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("ID sản phẩm không được để trống.", captor.getValue().message); //
    }

    @Test
    @DisplayName("Fail: Tên sản phẩm rỗng (Validation Common)")
    void execute_ShouldFail_WhenCommonInfoIsInvalid() {
        // Arrange
        UpdateLaptopRequestData input = new UpdateLaptopRequestData(
            "token", "ID-1", "", "Desc", BigDecimal.TEN, 10, "Cat", "Img", "AVAILABLE", 
            "CPU", "RAM", "HDD", 14.0
        ); // Name rỗng
        
        AuthPrincipal admin = new AuthPrincipal("admin", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(anyString())).thenReturn(admin);

        // Act
        updateLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<UpdateDeviceResponseData> captor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Tên sản phẩm không được để trống.", captor.getValue().message); //
    }
    
    @Test
    @DisplayName("Fail: mô tả rỗng")
    void execute_ShouldFail_WhenCommonInfoIsInvalid02() {
        // Arrange
        UpdateLaptopRequestData input = new UpdateLaptopRequestData(
            "token", "ID-1", "name", "", BigDecimal.TEN, 10, "Cat", "Img", "AVAILABLE", 
            "CPU", "RAM", "HDD", 14.0
        ); // Name rỗng
        
        AuthPrincipal admin = new AuthPrincipal("admin", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(anyString())).thenReturn(admin);

        // Act
        updateLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<UpdateDeviceResponseData> captor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Mô tả sản phẩm không được để trống.", captor.getValue().message); //
    }
    
    @Test
    @DisplayName("Fail: Giá âm")
    void execute_ShouldFail_WhenCommonInfoIsInvalid03() {
        // Arrange
        UpdateLaptopRequestData input = new UpdateLaptopRequestData(
            "token", "ID-1", "name", "des", BigDecimal.valueOf(-1), 10, "Cat", "Img", "AVAILABLE", 
            "CPU", "RAM", "HDD", 14.0
        ); // Name rỗng
        
        AuthPrincipal admin = new AuthPrincipal("admin", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(anyString())).thenReturn(admin);

        // Act
        updateLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<UpdateDeviceResponseData> captor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Giá bán không hợp lệ.", captor.getValue().message); //
    }
    
    @Test
    @DisplayName("Fail: tồn kho âm")
    void execute_ShouldFail_WhenCommonInfoIsInvalid05() {
        // Arrange
        UpdateLaptopRequestData input = new UpdateLaptopRequestData(
            "token", "ID-1", "name", "des", BigDecimal.TEN, -1, "Cat", "Img", "AVAILABLE", 
            "CPU", "RAM", "HDD", 14.0
        ); // Name rỗng
        
        AuthPrincipal admin = new AuthPrincipal("admin", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(anyString())).thenReturn(admin);

        // Act
        updateLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<UpdateDeviceResponseData> captor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Số lượng tồn kho không được âm.", captor.getValue().message); //
    }

    @Test
    @DisplayName("Fail: Trạng thái sản phẩm không hợp lệ")
    void execute_ShouldFail_WhenStatusIsInvalid() {
        // Arrange
        UpdateLaptopRequestData input = new UpdateLaptopRequestData(
            "token", "ID-1", "Name", "Desc", BigDecimal.TEN, 10, "Cat", "Img", "INVALID_STATUS", 
            "CPU", "RAM", "HDD", 14.0
        );
        
        AuthPrincipal admin = new AuthPrincipal("admin", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(anyString())).thenReturn(admin);

        // Act
        updateLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<UpdateDeviceResponseData> captor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("Trạng thái sản phẩm không hợp lệ")); //
    }

    @Test
    @DisplayName("Fail: CPU rỗng")
    void execute_ShouldFail_WhenSpecificDataIsInvalid() {
        // Arrange
        UpdateLaptopRequestData input = new UpdateLaptopRequestData(
            "token", "ID-1", "Name", "Desc", BigDecimal.TEN, 10, "Cat", "Img", "AVAILABLE", 
            "", "RAM", "HDD", 14.0
        ); // CPU rỗng
        
        AuthPrincipal admin = new AuthPrincipal("admin", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(anyString())).thenReturn(admin);

        // Act
        updateLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<UpdateDeviceResponseData> captor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Thông tin CPU không được trống.", captor.getValue().message); //
    }
    
    @Test
    @DisplayName("Fail: ram rỗng")
    void execute_ShouldFail_WhenSpecificDataIsInvalid02() {
        // Arrange
        UpdateLaptopRequestData input = new UpdateLaptopRequestData(
            "token", "ID-1", "Name", "Desc", BigDecimal.TEN, 10, "Cat", "Img", "AVAILABLE", 
            "cpu", "", "HDD", 14.0
        ); // CPU rỗng
        
        AuthPrincipal admin = new AuthPrincipal("admin", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(anyString())).thenReturn(admin);

        // Act
        updateLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<UpdateDeviceResponseData> captor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Thông tin RAM không được trống.", captor.getValue().message); //
    }
    
    @Test
    @DisplayName("Fail: dung lượng rỗng")
    void execute_ShouldFail_WhenSpecificDataIsInvalid03() {
        // Arrange
        UpdateLaptopRequestData input = new UpdateLaptopRequestData(
            "token", "ID-1", "Name", "Desc", BigDecimal.TEN, 10, "Cat", "Img", "AVAILABLE", 
            "cpu", "ram", "", 14.0
        ); // CPU rỗng
        
        AuthPrincipal admin = new AuthPrincipal("admin", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(anyString())).thenReturn(admin);

        // Act
        updateLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<UpdateDeviceResponseData> captor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Thông tin Storage không được trống.", captor.getValue().message); //
    }
    
    @Test
    @DisplayName("Fail: kích thước màn hình nhỏ hơn 0")
    void execute_ShouldFail_WhenSpecificDataIsInvalid04() {
        // Arrange
        UpdateLaptopRequestData input = new UpdateLaptopRequestData(
            "token", "ID-1", "Name", "Desc", BigDecimal.TEN, 10, "Cat", "Img", "AVAILABLE", 
            "cpu", "ram", "HDD", -1
        ); // CPU rỗng
        
        AuthPrincipal admin = new AuthPrincipal("admin", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(anyString())).thenReturn(admin);

        // Act
        updateLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<UpdateDeviceResponseData> captor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Kích thước màn hình không hợp lệ.", captor.getValue().message); //
    }

    @Test
    @DisplayName("Fail: Không tìm thấy sản phẩm trong DB")
    void execute_ShouldFail_WhenDeviceNotFound() {
        // Arrange
        UpdateLaptopRequestData input = createValidRequest();
        AuthPrincipal admin = new AuthPrincipal("admin", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(anyString())).thenReturn(admin);
        
        // Mock Repo trả về null
        when(deviceRepository.findById(input.id)).thenReturn(null);

        // Act
        updateLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<UpdateDeviceResponseData> captor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Không tìm thấy sản phẩm với ID: " + input.id, captor.getValue().message); //
    }

    @Test
    @DisplayName("Fail: Tên mới bị trùng với sản phẩm khác")
    void execute_ShouldFail_WhenNewNameExists() {
        // Arrange
        UpdateLaptopRequestData input = createValidRequest(); // Name: "MacBook Pro M3"
        AuthPrincipal admin = new AuthPrincipal("admin", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(anyString())).thenReturn(admin);
        
        // Mock DB trả về data cũ (Tên: "MacBook Pro M2")
        DeviceData existingData = createExistingDeviceData();
        when(deviceRepository.findById(input.id)).thenReturn(existingData);
        
        // Tên Input khác Tên Cũ -> Code sẽ check existsByName
        // Mock existsByName trả về true (đã có sp khác dùng tên này)
        when(deviceRepository.existsByName(input.name)).thenReturn(true);

        // Act
        updateLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<UpdateDeviceResponseData> captor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Tên sản phẩm mới đã tồn tại.", captor.getValue().message); //
    }

    @Test
    @DisplayName("Fail: Danh mục mới không tồn tại")
    void execute_ShouldFail_WhenCategoryNotFound() {
        // Arrange
        UpdateLaptopRequestData input = createValidRequest();
        AuthPrincipal admin = new AuthPrincipal("admin", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(anyString())).thenReturn(admin);
        
        DeviceData existingData = createExistingDeviceData();
        when(deviceRepository.findById(input.id)).thenReturn(existingData);
        
        // Mock tên không trùng
        when(deviceRepository.existsByName(input.name)).thenReturn(false);
        
        // Mock Category trả về null
        when(categoryRepository.findById(input.categoryId)).thenReturn(null);

        // Act
        updateLaptopUseCase.execute(input);

        // Assert
        ArgumentCaptor<UpdateDeviceResponseData> captor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Danh mục không tồn tại.", captor.getValue().message); //
    }

    @Test
    @DisplayName("10. Success: Cập nhật thành công")
    void execute_ShouldSucceed_WhenAllConditionsMet() {
        // Arrange
        UpdateLaptopRequestData input = createValidRequest();
        AuthPrincipal admin = new AuthPrincipal("admin", "admin@mail.com", UserRole.ADMIN);
        
        // 1. Validate Auth OK
        when(tokenValidator.validate(input.authToken)).thenReturn(admin);
        
        // 2. Find Existing OK
        DeviceData existingData = createExistingDeviceData();
        when(deviceRepository.findById(input.id)).thenReturn(existingData);
        
        // 3. Check Name OK (Không trùng)
        when(deviceRepository.existsByName(input.name)).thenReturn(false);
        
        // 4. Check Category OK
        CategoryData category = new CategoryData("CAT-APPLE", "MacBooks", "", null, null, null);
        when(categoryRepository.findById(input.categoryId)).thenReturn(category);

        // Act
        updateLaptopUseCase.execute(input);

        // Assert Output
        ArgumentCaptor<UpdateDeviceResponseData> outputCaptor = ArgumentCaptor.forClass(UpdateDeviceResponseData.class);
        verify(outputBoundary).present(outputCaptor.capture());
        UpdateDeviceResponseData output = outputCaptor.getValue();
        
        assertTrue(output.success);
        assertEquals("Cập nhật sản phẩm thành công!", output.message); //
        assertEquals(input.id, output.deviceId);

        // Assert Data Saved
        ArgumentCaptor<DeviceData> saveCaptor = ArgumentCaptor.forClass(DeviceData.class);
        verify(deviceRepository).save(saveCaptor.capture());
        DeviceData savedData = saveCaptor.getValue();

        // Kiểm tra dữ liệu được lưu xuống DB có khớp với Input không
        assertEquals(input.name, savedData.name);
        assertEquals(input.price, savedData.price);
        assertEquals(input.status, savedData.status);
        
        // Kiểm tra dữ liệu riêng của Laptop đã được update
        assertEquals(input.cpu, savedData.cpu);
        assertEquals(input.ram, savedData.ram);
    }
}