package payment.ProcessPayment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.Payment.ProcessPayment.ProcessPaymentPresenter;
import cgx.com.adapters.Payment.ProcessPayment.ProcessPaymentViewModel;
import cgx.com.usecase.Payment.ProcessPayment.ProcessPaymentResponseData;

import static org.junit.jupiter.api.Assertions.*;

public class ProcessPaymentPresenterTest {

    private ProcessPaymentPresenter presenter;
    private ProcessPaymentViewModel viewModel;

    @BeforeEach
    void setUp() {
        viewModel = new ProcessPaymentViewModel();
        presenter = new ProcessPaymentPresenter(viewModel);
    }

    @Test
    void test_present_success() {
        // ARRANGE: Successful banking payment response
        ProcessPaymentResponseData response = new ProcessPaymentResponseData();
        response.success = true;
        response.message = "Scan QR code";
        response.paymentUrl = "http://qr-link.com";
        response.transactionRef = "TXN-123";

        // ACT
        presenter.present(response);

        // ASSERT
        assertEquals("true", viewModel.success);
        assertEquals("Scan QR code", viewModel.message);
        assertEquals("http://qr-link.com", viewModel.paymentUrl);
        assertEquals("TXN-123", viewModel.transactionRef);
    }

    @Test
    void test_present_failure() {
        // ARRANGE: Failed payment response
        ProcessPaymentResponseData response = new ProcessPaymentResponseData();
        response.success = false;
        response.message = "Invalid payment method";
        // paymentUrl and transactionRef remain null

        // ACT
        presenter.present(response);

        // ASSERT
        assertEquals("false", viewModel.success);
        assertEquals("Invalid payment method", viewModel.message);
        assertNull(viewModel.paymentUrl);
        assertNull(viewModel.transactionRef);
    }
    
    @Test
    void test_present_success_cod() {
        // ARRANGE: Successful COD payment (no URL)
        ProcessPaymentResponseData response = new ProcessPaymentResponseData();
        response.success = true;
        response.message = "Prepare cash";
        response.paymentUrl = null; // COD has no URL
        response.transactionRef = "COD-456";

        // ACT
        presenter.present(response);

        // ASSERT
        assertEquals("true", viewModel.success);
        assertEquals("Prepare cash", viewModel.message);
        assertNull(viewModel.paymentUrl);
        assertEquals("COD-456", viewModel.transactionRef);
    }
}