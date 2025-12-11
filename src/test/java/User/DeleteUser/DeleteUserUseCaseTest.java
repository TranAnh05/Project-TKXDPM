package User.DeleteUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.AccountStatus;
import cgx.com.Entities.OrderStatus;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageOrder.IOrderRepository;
import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.DeleteUser.DeleteUserOutputBoundary;
import cgx.com.usecase.ManageUser.DeleteUser.DeleteUserRequestData;
import cgx.com.usecase.ManageUser.DeleteUser.DeleteUserResponseData;
import cgx.com.usecase.ManageUser.DeleteUser.DeleteUserUseCase;

@ExtendWith(MockitoExtension.class)
public class DeleteUserUseCaseTest {

    @Mock
    private IAuthTokenValidator tokenValidator;
    @Mock
    private IUserRepository userRepository;
    @Mock
    private IOrderRepository orderRepository; // Mock Order Repo
    @Mock
    private DeleteUserOutputBoundary outputBoundary;

    private DeleteUserUseCase useCase;

    // Dữ liệu mẫu
    private String adminToken = "valid_admin_token";
    private String adminId = "admin-001";
    private String targetUserId = "user-target-002";
    
    private DeleteUserRequestData request;

    @BeforeEach
    void setUp() {
        // Khởi tạo instance nặc danh cho Abstract Class để test logic
        useCase = new DeleteUserUseCase(tokenValidator, userRepository, orderRepository, outputBoundary) {};

        request = new DeleteUserRequestData();
        request.authToken = adminToken;
        request.targetUserId = targetUserId;
    }

    // --- HELPER MOCKING ---
    private void mockAdminAuth() {
        AuthPrincipal principal = new AuthPrincipal(adminId, "admin@test.com", UserRole.ADMIN);
        when(tokenValidator.validate(adminToken)).thenReturn(principal);
    }
    
    private void mockAdminData() {
        UserData adminData = new UserData(adminId, "admin@test.com", "hash", "Admin", "User", "0900000000", UserRole.ADMIN, AccountStatus.ACTIVE, Instant.now(), Instant.now());
        when(userRepository.findByUserId(adminId)).thenReturn(adminData);
    }

    private void mockTargetUserData() {
        UserData targetUser = new UserData(targetUserId, "target@test.com", "hash", "Target", "User", "0900000000", UserRole.CUSTOMER, AccountStatus.ACTIVE, Instant.now(), Instant.now());
        when(userRepository.findByUserId(targetUserId)).thenReturn(targetUser);
    }

    // Không phải admin
    @Test
    void testExecute_Fail_NotAdmin() {
        AuthPrincipal customerPrincipal = new AuthPrincipal("cust-001", "cust@test.com", UserRole.CUSTOMER);
        when(tokenValidator.validate(anyString())).thenReturn(customerPrincipal);

        useCase.execute(request);

        ArgumentCaptor<DeleteUserResponseData> captor = ArgumentCaptor.forClass(DeleteUserResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Không có quyền truy cập.", captor.getValue().message);
    }

    // Không tìm thấy tài khoản 
    @Test
    void testExecute_Fail_TargetUserNotFound() {
        mockAdminAuth();
        mockAdminData(); // Cần admin data để init logic
        
        when(userRepository.findByUserId(targetUserId)).thenReturn(null); // Target không thấy

        useCase.execute(request);

        ArgumentCaptor<DeleteUserResponseData> captor = ArgumentCaptor.forClass(DeleteUserResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Không tìm thấy tài khoản người dùng.", captor.getValue().message);
    }

    // Admin không thể xóa chính mình
    @Test
    void testExecute_Fail_AdminSelfDelete() {
        mockAdminAuth();
        mockAdminData();
        
        request.targetUserId = adminId;

        useCase.execute(request);

        ArgumentCaptor<DeleteUserResponseData> captor = ArgumentCaptor.forClass(DeleteUserResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("Admin không thể tự cập nhật"));
    }
    
    // User có đơn hàng
    @Test
    void testExecute_Fail_UserHasActiveOrders() {
        // Arrange
        mockAdminAuth();
        mockAdminData();
        mockTargetUserData();

        // Mock danh sách đơn hàng có chứa đơn đang giao (SHIPPED)
        List<OrderData> activeOrders = new ArrayList<>();
        OrderData order = new OrderData();
        order.id = "ORDER-001";
        order.status = OrderStatus.SHIPPED.name(); // "SHIPPED"
        activeOrders.add(order);

        when(orderRepository.findByUserId(targetUserId)).thenReturn(activeOrders);

        // Act
        useCase.execute(request);

        // Assert
        ArgumentCaptor<DeleteUserResponseData> captor = ArgumentCaptor.forClass(DeleteUserResponseData.class);
        verify(outputBoundary).present(captor.capture());
        DeleteUserResponseData response = captor.getValue();
        
        assertFalse(response.success);
        assertEquals("Không thể xóa người dùng đang có đơn hàng chưa hoàn tất (Mã đơn: ORDER-001).", response.message);
        
        verify(userRepository, never()).update(any(UserData.class));
    }

    // Thành công
    @Test
    void testExecute_Success() {
        // Arrange
        mockAdminAuth();
        mockAdminData();
        mockTargetUserData();

        List<OrderData> finishedOrders = new ArrayList<>();
        OrderData oldOrder = new OrderData();
        oldOrder.id = "ORDER-OLD";
        oldOrder.status = OrderStatus.DELIVERED.name();
        finishedOrders.add(oldOrder);
        
        when(orderRepository.findByUserId(targetUserId)).thenReturn(finishedOrders);

        when(userRepository.update(any(UserData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        useCase.execute(request);

        // Assert
        ArgumentCaptor<DeleteUserResponseData> responseCaptor = ArgumentCaptor.forClass(DeleteUserResponseData.class);
        verify(outputBoundary).present(responseCaptor.capture());
        DeleteUserResponseData response = responseCaptor.getValue();

        // Validate Output
        assertTrue(response.success);
        assertEquals("Xóa người dùng thành công.", response.message);
        assertEquals(targetUserId, response.deletedUserId);
        assertEquals(AccountStatus.DELETED.toString(), response.newStatus);

        ArgumentCaptor<UserData> userCaptor = ArgumentCaptor.forClass(UserData.class);
        verify(userRepository).update(userCaptor.capture());
        
        UserData savedData = userCaptor.getValue();
        assertEquals(AccountStatus.DELETED, savedData.status); 
        assertNotNull(savedData.updatedAt);
    }
}