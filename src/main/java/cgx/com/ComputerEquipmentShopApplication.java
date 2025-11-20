package cgx.com;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Điểm khởi chạy của ứng dụng Spring Boot.
 * @SpringBootApplication bao gồm:
 * - @Configuration: Cho phép đăng ký thêm Bean.
 * - @EnableAutoConfiguration: Tự động cấu hình (Tomcat, Database...).
 * - @ComponentScan: Tự động tìm các @Component, @RestController, @Repository trong package này và các package con.
 */
@SpringBootApplication
public class ComputerEquipmentShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(ComputerEquipmentShopApplication.class, args);
    }
}