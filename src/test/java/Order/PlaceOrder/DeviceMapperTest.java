package Order.PlaceOrder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.Laptop;
import cgx.com.Entities.Mouse;
import cgx.com.usecase.ManageOrder.PlaceOrder.DeviceMapper;
import cgx.com.usecase.ManageOrder.PlaceOrder.IDeviceSpecificMapper;
import cgx.com.usecase.ManageOrder.PlaceOrder.LaptopMapper;
import cgx.com.usecase.ManageOrder.PlaceOrder.MouseMapper;
import cgx.com.usecase.ManageProduct.DeviceData;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceMapperTest {

    private DeviceMapper deviceMapper;
    private LaptopMapper laptopMapper;
    private MouseMapper mouseMapper;

    @BeforeEach
    void setUp() {
        // Khởi tạo các Mapper con thật sự (không Mock, để test logic chọn strategy)
        laptopMapper = new LaptopMapper();
        mouseMapper = new MouseMapper();
        
        // Inject list vào DeviceMapper
        List<IDeviceSpecificMapper> strategies = Arrays.asList(laptopMapper, mouseMapper);
        deviceMapper = new DeviceMapper(strategies);
    }

    // Case 1: Map Laptop DTO -> Entity
    @Test
    void test_toEntity_Laptop() {
        DeviceData data = new DeviceData();
        data.id = "1";
        data.name = "MacBook";
        data.type = "LAPTOP"; // Key quan trọng
        data.cpu = "M1";
        data.screenSize = 13.3;
        
        ComputerDevice result = deviceMapper.toEntity(data);
        
        assertTrue(result instanceof Laptop);
        Laptop laptop = (Laptop) result;
        assertEquals("M1", laptop.getCpu());
        assertEquals(13.3, laptop.getScreenSize());
    }

    // Case 2: Map Mouse DTO -> Entity
    @Test
    void test_toEntity_Mouse() {
        DeviceData data = new DeviceData();
        data.id = "2";
        data.name = "Logitech";
        data.type = "MOUSE"; // Key quan trọng
        data.dpi = 1000;
        data.isWireless = true;
        data.buttonCount = 10;
        
        ComputerDevice result = deviceMapper.toEntity(data);
        
        assertTrue(result instanceof Mouse);
        Mouse mouse = (Mouse) result;
        assertEquals(1000, mouse.getDpi());
    }

    // Case 3: Map Entity -> Laptop DTO
    @Test
    void test_toDTO_Laptop() {
        Laptop laptop = new Laptop(
            "1", "Mac", "Desc", BigDecimal.TEN, 10, "cat", "ACTIVE", "img", 
            Instant.now(), Instant.now(), "M1", "8GB", "256GB", 13.3
        );

        DeviceData result = deviceMapper.toDTO(laptop);
        
        assertEquals("LAPTOP", result.type); // Phải tự động set type
        assertEquals("M1", result.cpu);
    }

    // Case 4: Loại không hỗ trợ -> Exception
    @Test
    void test_toEntity_UnsupportedType() {
        DeviceData data = new DeviceData();
        data.type = "KEYBOARD"; // Chưa có mapper cho cái này
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            deviceMapper.toEntity(data);
        });
        
        assertTrue(exception.getMessage().contains("Không hỗ trợ loại thiết bị"));
    }
}
