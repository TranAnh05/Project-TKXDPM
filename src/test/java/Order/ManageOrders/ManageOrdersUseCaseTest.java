package Order.ManageOrders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageOrder.IOrderRepository;
import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageOrder.ManageOrders.ManageOrdersOutputBoundary;
import cgx.com.usecase.ManageOrder.ManageOrders.ManageOrdersRequestData;
import cgx.com.usecase.ManageOrder.ManageOrders.ManageOrdersResponseData;
import cgx.com.usecase.ManageOrder.ManageOrders.ManageOrdersUseCase;
import cgx.com.usecase.ManageOrder.ManageOrders.OrderSearchCriteria;
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManageOrdersUseCaseTest {

    @Mock private IOrderRepository mockOrderRepository;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private ManageOrdersOutputBoundary mockOutputBoundary;

    private ManageOrdersUseCase useCase;
    private AuthPrincipal adminPrincipal;

    @BeforeEach
    void setUp() {
        useCase = new ManageOrdersUseCase(mockOrderRepository, mockTokenValidator, mockOutputBoundary);
        adminPrincipal = new AuthPrincipal("admin", "admin@e.com", UserRole.ADMIN);
    }

    @Test
    void test_execute_success_filterStatus() {
        // ARRANGE: Lọc đơn PENDING
        ManageOrdersRequestData input = new ManageOrdersRequestData("token", "PENDING", null, 1, 10);
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockOrderRepository.search(any(OrderSearchCriteria.class), eq(0), eq(10)))
            .thenReturn(List.of(new OrderData()));
        when(mockOrderRepository.count(any(OrderSearchCriteria.class))).thenReturn(5L);

        ArgumentCaptor<OrderSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(OrderSearchCriteria.class);
        ArgumentCaptor<ManageOrdersResponseData> responseCaptor = ArgumentCaptor.forClass(ManageOrdersResponseData.class);

        // ACT
        useCase.execute(input);

        // ASSERT
        verify(mockOrderRepository).search(criteriaCaptor.capture(), eq(0), eq(10));
        verify(mockOutputBoundary).present(responseCaptor.capture());

        assertEquals("PENDING", criteriaCaptor.getValue().status);
        assertTrue(responseCaptor.getValue().success);
        assertEquals(1, responseCaptor.getValue().pagination.currentPage);
        assertEquals(5, responseCaptor.getValue().pagination.totalCount);
    }

    @Test
    void test_execute_failure_notAdmin() {
        ManageOrdersRequestData input = new ManageOrdersRequestData("token", null, null, 1, 10);
        AuthPrincipal customer = new AuthPrincipal("cust", "e", UserRole.CUSTOMER);
        
        when(mockTokenValidator.validate("token")).thenReturn(customer);

        ArgumentCaptor<ManageOrdersResponseData> responseCaptor = ArgumentCaptor.forClass(ManageOrdersResponseData.class);
        useCase.execute(input);

        verify(mockOrderRepository, never()).search(any(), anyInt(), anyInt());
        verify(mockOutputBoundary).present(responseCaptor.capture());

        assertFalse(responseCaptor.getValue().success);
        assertEquals("Không có quyền truy cập (Yêu cầu Admin).", responseCaptor.getValue().message);
    }

    @Test
    void test_execute_failure_dbCrash() {
        ManageOrdersRequestData input = new ManageOrdersRequestData("token", null, null, 1, 10);
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        doThrow(new RuntimeException("DB Error")).when(mockOrderRepository).search(any(), anyInt(), anyInt());

        ArgumentCaptor<ManageOrdersResponseData> responseCaptor = ArgumentCaptor.forClass(ManageOrdersResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(responseCaptor.capture());
        assertFalse(responseCaptor.getValue().success);
        assertTrue(responseCaptor.getValue().message.contains("Lỗi hệ thống"));
    }
}