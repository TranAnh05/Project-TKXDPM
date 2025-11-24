package Order.ViewMyOrders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageOrder.IOrderRepository;
import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageOrder.ViewMyOrders.ViewMyOrdersOutputBoundary;
import cgx.com.usecase.ManageOrder.ViewMyOrders.ViewMyOrdersRequestData;
import cgx.com.usecase.ManageOrder.ViewMyOrders.ViewMyOrdersResponseData;
import cgx.com.usecase.ManageOrder.ViewMyOrders.ViewMyOrdersUseCase;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ViewMyOrdersUseCaseTest {

    @Mock private IOrderRepository mockOrderRepository;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private ViewMyOrdersOutputBoundary mockOutputBoundary;

    private ViewMyOrdersUseCase useCase;
    private AuthPrincipal customerPrincipal;

    @BeforeEach
    void setUp() {
        useCase = new ViewMyOrdersUseCase(mockOrderRepository, mockTokenValidator, mockOutputBoundary);
        customerPrincipal = new AuthPrincipal("user-1", "user@test.com", UserRole.CUSTOMER);
    }

    // Case 1: Thành công - Có đơn hàng
    @Test
    void test_execute_success_withOrders() {
        ViewMyOrdersRequestData input = new ViewMyOrdersRequestData("token");
        
        OrderData order1 = new OrderData();
        order1.id = "ord-1"; order1.userId = "user-1"; order1.totalAmount = BigDecimal.TEN;
        
        OrderData order2 = new OrderData();
        order2.id = "ord-2"; order2.userId = "user-1"; order2.totalAmount = BigDecimal.valueOf(20);

        when(mockTokenValidator.validate("token")).thenReturn(customerPrincipal);
        when(mockOrderRepository.findByUserId("user-1")).thenReturn(List.of(order1, order2));

        ArgumentCaptor<ViewMyOrdersResponseData> responseCaptor = ArgumentCaptor.forClass(ViewMyOrdersResponseData.class);

        useCase.execute(input);

        verify(mockOrderRepository).findByUserId("user-1");
        verify(mockOutputBoundary).present(responseCaptor.capture());

        ViewMyOrdersResponseData response = responseCaptor.getValue();
        assertTrue(response.success);
        assertEquals("Lấy danh sách đơn hàng thành công.", response.message);
        assertEquals(2, response.orders.size());
    }

    // Case 2: Thành công - Không có đơn hàng nào (Empty List)
    // Đây là case quan trọng để đảm bảo UI không bị lỗi khi list rỗng
    @Test
    void test_execute_success_emptyOrders() {
        ViewMyOrdersRequestData input = new ViewMyOrdersRequestData("token");

        when(mockTokenValidator.validate("token")).thenReturn(customerPrincipal);
        // DB trả về list rỗng
        when(mockOrderRepository.findByUserId("user-1")).thenReturn(Collections.emptyList());

        ArgumentCaptor<ViewMyOrdersResponseData> responseCaptor = ArgumentCaptor.forClass(ViewMyOrdersResponseData.class);

        useCase.execute(input);

        verify(mockOutputBoundary).present(responseCaptor.capture());
        ViewMyOrdersResponseData response = responseCaptor.getValue();

        assertTrue(response.success);
        assertNotNull(response.orders);
        assertTrue(response.orders.isEmpty());
    }

    // Case 3: Lỗi Auth - Token hết hạn/không hợp lệ
    @Test
    void test_execute_failure_invalidToken() {
        ViewMyOrdersRequestData input = new ViewMyOrdersRequestData("invalid-token");
        when(mockTokenValidator.validate("invalid-token")).thenThrow(new SecurityException("Expired"));

        ArgumentCaptor<ViewMyOrdersResponseData> responseCaptor = ArgumentCaptor.forClass(ViewMyOrdersResponseData.class);

        useCase.execute(input);

        verify(mockOrderRepository, never()).findByUserId(anyString());
        verify(mockOutputBoundary).present(responseCaptor.capture());

        assertFalse(responseCaptor.getValue().success);
        assertEquals("Expired", responseCaptor.getValue().message);
    }
    
    // Case 4: Lỗi Auth - Token rỗng (Validation Input)
    @Test
    void test_execute_failure_emptyToken() {
        ViewMyOrdersRequestData input = new ViewMyOrdersRequestData("");
        
        ArgumentCaptor<ViewMyOrdersResponseData> responseCaptor = ArgumentCaptor.forClass(ViewMyOrdersResponseData.class);

        useCase.execute(input);

        verify(mockTokenValidator, never()).validate(anyString());
        verify(mockOutputBoundary).present(responseCaptor.capture());

        assertFalse(responseCaptor.getValue().success);
        assertEquals("Auth Token không được để trống.", responseCaptor.getValue().message);
    }
    
    // Case 5: Lỗi Hệ thống - Database Crash
    @Test
    void test_execute_failure_dbError() {
        ViewMyOrdersRequestData input = new ViewMyOrdersRequestData("token");
        when(mockTokenValidator.validate("token")).thenReturn(customerPrincipal);
        
        // Giả lập lỗi kết nối DB
        doThrow(new RuntimeException("DB Down")).when(mockOrderRepository).findByUserId("user-1");
        
        ArgumentCaptor<ViewMyOrdersResponseData> responseCaptor = ArgumentCaptor.forClass(ViewMyOrdersResponseData.class);

        useCase.execute(input);
        
        verify(mockOutputBoundary).present(responseCaptor.capture());
        assertFalse(responseCaptor.getValue().success);
        assertTrue(responseCaptor.getValue().message.contains("Lỗi hệ thống"));
    }
}