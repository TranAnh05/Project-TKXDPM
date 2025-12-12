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
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ViewMyOrdersUseCaseTest {

    @Mock
    private IOrderRepository orderRepository;
    @Mock
    private IAuthTokenValidator tokenValidator;
    @Mock
    private ViewMyOrdersOutputBoundary outputBoundary;

    private ViewMyOrdersUseCase useCase;

    // Dữ liệu mẫu
    private String userToken = "valid_user_token";
    private String userId = "user-123";
    private ViewMyOrdersRequestData request;

    @BeforeEach
    void setUp() {
        useCase = new ViewMyOrdersUseCase(orderRepository, tokenValidator, outputBoundary);
        request = new ViewMyOrdersRequestData();
        request.authToken = userToken;
    }


    // Case: Token không hợp lệ
    @Test
    void testExecute_Fail_InvalidToken() {
        // Arrange
        when(tokenValidator.validate(anyString())).thenThrow(new SecurityException("Phiên đăng nhập hết hạn."));

        // Act
        useCase.execute(request);

        // Assert
        ArgumentCaptor<ViewMyOrdersResponseData> captor = ArgumentCaptor.forClass(ViewMyOrdersResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        ViewMyOrdersResponseData response = captor.getValue();
        assertFalse(response.success);
        assertEquals("Phiên đăng nhập hết hạn.", response.message);
    }

    // Case: thành công - có đơn hàng
    @Test
    void testExecute_Success_WithOrders() {
        // Arrange
        AuthPrincipal principal = new AuthPrincipal(userId, "test@mail.com", UserRole.CUSTOMER);
        when(tokenValidator.validate(userToken)).thenReturn(principal);

        List<OrderData> mockOrders = new ArrayList<>();
        
        OrderData order1 = new OrderData();
        order1.id = "ORDER-001";
        order1.userId = userId;
        order1.totalAmount = BigDecimal.valueOf(100);
        order1.status = "PENDING";
        
        OrderData order2 = new OrderData();
        order2.id = "ORDER-002";
        order2.userId = userId;
        order2.totalAmount = BigDecimal.valueOf(200);
        order2.status = "SHIPPED";
        
        mockOrders.add(order1);
        mockOrders.add(order2);

        when(orderRepository.findByUserId(userId)).thenReturn(mockOrders);

        // Act
        useCase.execute(request);

        // Assert
        ArgumentCaptor<ViewMyOrdersResponseData> captor = ArgumentCaptor.forClass(ViewMyOrdersResponseData.class);
        verify(outputBoundary).present(captor.capture());
        ViewMyOrdersResponseData response = captor.getValue();

        // Kiểm tra kết quả
        assertTrue(response.success);
        assertEquals("Lấy danh sách đơn hàng thành công.", response.message);
        assertNotNull(response.orders);
        assertEquals(2, response.orders.size());
        
        assertEquals("ORDER-001", response.orders.get(0).id);
        assertEquals(userId, response.orders.get(0).userId);
    }

    // Case: thành công - không có đơn hàng nào
    @Test
    void testExecute_Success_NoOrders() {
        // Arrange
        AuthPrincipal principal = new AuthPrincipal(userId, "test@mail.com", UserRole.CUSTOMER);
        when(tokenValidator.validate(userToken)).thenReturn(principal);

        // Mock trả về list rỗng
        when(orderRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        // Act
        useCase.execute(request);

        // Assert
        ArgumentCaptor<ViewMyOrdersResponseData> captor = ArgumentCaptor.forClass(ViewMyOrdersResponseData.class);
        verify(outputBoundary).present(captor.capture());
        ViewMyOrdersResponseData response = captor.getValue();

        assertTrue(response.success);
        assertNotNull(response.orders);
        assertTrue(response.orders.isEmpty()); 
    }
}