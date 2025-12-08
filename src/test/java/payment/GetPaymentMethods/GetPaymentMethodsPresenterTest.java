package payment.GetPaymentMethods;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.Payment.GetPaymentMethods.GetPaymentMethodsPresenter;
import cgx.com.adapters.Payment.GetPaymentMethods.GetPaymentMethodsViewModel;
import cgx.com.adapters.Payment.GetPaymentMethods.PaymentMethodViewDTO;
import cgx.com.usecase.Payment.GetPaymentMethods.GetPaymentMethodsResponseData;
import cgx.com.usecase.Payment.GetPaymentMethods.PaymentMethodDTO;

public class GetPaymentMethodsPresenterTest {

    private GetPaymentMethodsPresenter presenter;
    private GetPaymentMethodsViewModel viewModel;

    @BeforeEach
    void setUp() {
        // ViewModel là POJO đơn giản, khởi tạo trực tiếp
        viewModel = new GetPaymentMethodsViewModel();
        // Inject ViewModel vào Presenter
        presenter = new GetPaymentMethodsPresenter(viewModel);
    }

    // =========================================================================
    // CASE 1: THÀNH CÔNG - CÓ DỮ LIỆU
    // Kiểm tra xem Presenter có map đúng các trường từ DTO sang ViewDTO không
    // =========================================================================
    @Test
    @DisplayName("Present Success: Mapping dữ liệu chính xác từ UseCase sang ViewModel")
    void test_present_success_withData() {
        // GIVEN: Dữ liệu giả lập từ Use Case trả về
        PaymentMethodDTO codDto = new PaymentMethodDTO("COD", "Tiền mặt", "Thanh toán khi nhận", "/icon/cod.png");
        PaymentMethodDTO bankDto = new PaymentMethodDTO("BANKING", "Chuyển khoản", "Quét QR", "/icon/bank.png");
        
        GetPaymentMethodsResponseData response = new GetPaymentMethodsResponseData();
        response.success = true;
        response.message = "OK";
        response.methods = Arrays.asList(codDto, bankDto);

        // WHEN: Gọi Presenter
        presenter.present(response);

        // THEN: Kiểm tra ViewModel
        // 1. Kiểm tra trạng thái chung
        assertEquals("true", viewModel.isSuccess);
        assertEquals("OK", viewModel.errorMessage);
        assertNotNull(viewModel.methodList);
        assertEquals(2, viewModel.methodList.size());

        // 2. Kiểm tra Mapping chi tiết phần tử đầu tiên (COD)
        PaymentMethodViewDTO codView = viewModel.methodList.get(0);
        
        // Kiểm tra logic đổi tên trường (Name -> Label, Description -> SubLabel)
        assertEquals("COD", codView.code);
        assertEquals("Tiền mặt", codView.label);          // dto.name -> view.label
        assertEquals("Thanh toán khi nhận", codView.subLabel); // dto.description -> view.subLabel
        assertEquals("/icon/cod.png", codView.imageSource); // dto.icon -> view.imageSource
    }

    // =========================================================================
    // CASE 2: THÀNH CÔNG - DANH SÁCH RỖNG
    // Kiểm tra xem Presenter có xử lý an toàn khi list rỗng không (tránh null)
    // =========================================================================
    @Test
    @DisplayName("Present Success: Xử lý danh sách rỗng an toàn")
    void test_present_success_emptyList() {
        // GIVEN
        GetPaymentMethodsResponseData response = new GetPaymentMethodsResponseData();
        response.success = true;
        response.message = "No methods found";
        response.methods = new ArrayList<>(); // List rỗng

        // WHEN
        presenter.present(response);

        // THEN
        assertEquals("true", viewModel.isSuccess);
        assertNotNull(viewModel.methodList); // Quan trọng: Không được null
        assertTrue(viewModel.methodList.isEmpty());
    }

    // =========================================================================
    // CASE 3: THẤT BẠI (FAILURE)
    // Kiểm tra khi Use Case báo lỗi
    // =========================================================================
    @Test
    @DisplayName("Present Failure: Hiển thị lỗi và list rỗng")
    void test_present_failure() {
        // GIVEN
        GetPaymentMethodsResponseData response = new GetPaymentMethodsResponseData();
        response.success = false;
        response.message = "Lỗi kết nối Database";
        response.methods = null; // Use Case trả về null khi lỗi

        // WHEN
        presenter.present(response);

        // THEN
        assertEquals("false", viewModel.isSuccess);
        assertEquals("Lỗi kết nối Database", viewModel.errorMessage);
        
        // Quan trọng: Dù response.methods là null, ViewModel phải là empty list để UI không crash
        assertNotNull(viewModel.methodList); 
        assertTrue(viewModel.methodList.isEmpty());
    }
}