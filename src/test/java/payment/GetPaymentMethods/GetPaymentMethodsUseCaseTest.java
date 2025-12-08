package payment.GetPaymentMethods;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.PaymentMethodConfig;
import cgx.com.usecase.Payment.IPaymentMethodRepository;
import cgx.com.usecase.Payment.GetPaymentMethods.GetPaymentMethodsOutputBoundary;
import cgx.com.usecase.Payment.GetPaymentMethods.GetPaymentMethodsRequestData;
import cgx.com.usecase.Payment.GetPaymentMethods.GetPaymentMethodsResponseData;
import cgx.com.usecase.Payment.GetPaymentMethods.GetPaymentMethodsUseCase;

@ExtendWith(MockitoExtension.class)
public class GetPaymentMethodsUseCaseTest {

    @Mock private IPaymentMethodRepository mockRepository;
    @Mock private GetPaymentMethodsOutputBoundary mockOutputBoundary;

    private GetPaymentMethodsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetPaymentMethodsUseCase(mockRepository, mockOutputBoundary);
    }

    @Test
    void test_execute_success_returnsList() {
        // GIVEN: Repository trả về 2 phương thức
        PaymentMethodConfig cod = new PaymentMethodConfig("COD", "Tiền mặt", "Desc1", "img1", true, 1);
        PaymentMethodConfig bank = new PaymentMethodConfig("BANKING", "CK", "Desc2", "img2", true, 2);
        when(mockRepository.findAllActive()).thenReturn(Arrays.asList(cod, bank));

        // WHEN
        useCase.execute(new GetPaymentMethodsRequestData());

        // THEN
        ArgumentCaptor<GetPaymentMethodsResponseData> captor = ArgumentCaptor.forClass(GetPaymentMethodsResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        GetPaymentMethodsResponseData response = captor.getValue();

        assertTrue(response.success);
        assertEquals(2, response.methods.size());
        assertEquals("COD", response.methods.get(0).code);
        assertEquals("BANKING", response.methods.get(1).code);
    }

    @Test
    void test_execute_success_emptyList() {
        // GIVEN: Repository không tìm thấy gì (hoặc admin tắt hết)
        when(mockRepository.findAllActive()).thenReturn(Collections.emptyList());

        // WHEN
        useCase.execute(new GetPaymentMethodsRequestData());

        // THEN
        ArgumentCaptor<GetPaymentMethodsResponseData> captor = ArgumentCaptor.forClass(GetPaymentMethodsResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertTrue(captor.getValue().success);
        assertNotNull(captor.getValue().methods);
        assertTrue(captor.getValue().methods.isEmpty());
    }

    @Test
    void test_execute_fail_dbException() {
        // GIVEN: DB bị lỗi
        when(mockRepository.findAllActive()).thenThrow(new RuntimeException("DB Connection Failed"));

        // WHEN
        useCase.execute(new GetPaymentMethodsRequestData());

        // THEN
        ArgumentCaptor<GetPaymentMethodsResponseData> captor = ArgumentCaptor.forClass(GetPaymentMethodsResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("Lỗi hệ thống"));
        assertTrue(captor.getValue().methods.isEmpty()); // Đảm bảo list không null để tránh crash UI
    }
}