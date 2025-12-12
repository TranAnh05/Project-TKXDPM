package User.UpdateUserProfile;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.AccountStatus;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.UpdateUserProfile.UpdateUserProfileOutputBoundary;
import cgx.com.usecase.ManageUser.UpdateUserProfile.UpdateUserProfileRequestData;
import cgx.com.usecase.ManageUser.UpdateUserProfile.UpdateUserProfileUseCase;
import cgx.com.usecase.ManageUser.ViewUserProfile.ViewUserProfileResponseData;

@ExtendWith(MockitoExtension.class)
public class UpdateUserProfileUseCaseTest {

    @Mock
    private IAuthTokenValidator tokenValidator;
    @Mock
    private IUserRepository userRepository;
    @Mock
    private UpdateUserProfileOutputBoundary outputBoundary;

    private UpdateUserProfileUseCase useCase;

    // Dữ liệu mẫu
    private String userToken = "valid_user_token";
    private String userId = "user-123";
    
    // Request hợp lệ mặc định
    private UpdateUserProfileRequestData validRequest;

    @BeforeEach
    void setUp() {
        useCase = new UpdateUserProfileUseCase(tokenValidator, userRepository, outputBoundary);
        
        // Input hợp lệ: Tên không rỗng, SĐT đúng format (09...)
        validRequest = new UpdateUserProfileRequestData(userToken, "Nguyen", "Van A", "0912345678");
    }

    private void mockAuth() {
        AuthPrincipal principal = new AuthPrincipal(userId, "user@test.com", UserRole.CUSTOMER);
        when(tokenValidator.validate(userToken)).thenReturn(principal);
    }

    private void mockUserData() {
        // Dữ liệu cũ trong DB
        UserData oldData = new UserData(userId, "user@test.com", "hash", "OldFirst", "OldLast", "0988888888", UserRole.CUSTOMER, AccountStatus.ACTIVE, Instant.now(), Instant.now());
        when(userRepository.findByUserId(userId)).thenReturn(oldData);
    }

    // Case: Tên rỗng
    @Test
    void testExecute_Fail_InvalidName() {
        // Arrange
        mockAuth();
        UpdateUserProfileRequestData invalidRequest = new UpdateUserProfileRequestData(userToken, "", "Van A", "0912345678");

        // Act
        useCase.execute(invalidRequest);

        // Assert
        ArgumentCaptor<ViewUserProfileResponseData> captor = ArgumentCaptor.forClass(ViewUserProfileResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("Tên không được để trống"));
    }

    // Case: Số điện thoại không hợp lệ
    @Test
    void testExecute_Fail_InvalidPhone() {
        // Arrange
        mockAuth();
        UpdateUserProfileRequestData invalidRequest = new UpdateUserProfileRequestData(userToken, "Nguyen", "Van A", "123");

        // Act
        useCase.execute(invalidRequest);

        // Assert
        ArgumentCaptor<ViewUserProfileResponseData> captor = ArgumentCaptor.forClass(ViewUserProfileResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("Số điện thoại không đúng định dạng"));
    }

    // Case: Thành công
    @Test
    void testExecute_Success() {
        // Arrange
        mockAuth();
        mockUserData();

        // Mock update trả về data mới
        when(userRepository.update(any(UserData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        useCase.execute(validRequest);

        // Assert
        ArgumentCaptor<ViewUserProfileResponseData> responseCaptor = ArgumentCaptor.forClass(ViewUserProfileResponseData.class);
        verify(outputBoundary).present(responseCaptor.capture());
        ViewUserProfileResponseData response = responseCaptor.getValue();

        // 1. Kiểm tra Output
        assertTrue(response.success);
        assertEquals("Cập nhật hồ sơ thành công.", response.message);
        assertEquals(validRequest.firstName, response.firstName);
        assertEquals(validRequest.lastName, response.lastName);
        assertEquals(validRequest.phoneNumber, response.phoneNumber);

        // 2. Kiểm tra Repository được gọi đúng data
        ArgumentCaptor<UserData> userCaptor = ArgumentCaptor.forClass(UserData.class);
        verify(userRepository).update(userCaptor.capture());
        UserData savedData = userCaptor.getValue();

        assertEquals(validRequest.firstName, savedData.firstName);
        assertNotNull(savedData.updatedAt); // Thời gian phải được cập nhật
    }
}