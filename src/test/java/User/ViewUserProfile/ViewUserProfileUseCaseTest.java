package User.ViewUserProfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import cgx.com.usecase.ManageUser.ViewUserProfile.ViewUserProfileOutputBoundary;
import cgx.com.usecase.ManageUser.ViewUserProfile.ViewUserProfileRequestData;
import cgx.com.usecase.ManageUser.ViewUserProfile.ViewUserProfileResponseData;
import cgx.com.usecase.ManageUser.ViewUserProfile.ViewUserProfileUseCase;

@ExtendWith(MockitoExtension.class)
public class ViewUserProfileUseCaseTest {

    @Mock
    private IAuthTokenValidator tokenValidator;
    @Mock
    private IUserRepository userRepository;
    @Mock
    private ViewUserProfileOutputBoundary outputBoundary;

    private ViewUserProfileUseCase useCase;

    // Dữ liệu mẫu dùng chung
    private String validToken = "valid_token_123";
    private String invalidToken = "invalid_token";
    private String userId = "user-001";
    
    private ViewUserProfileRequestData request;

    @BeforeEach
    void setUp() {
        useCase = new ViewUserProfileUseCase(tokenValidator, userRepository, outputBoundary);
        request = new ViewUserProfileRequestData(validToken);
    }

    // --- Helper Method ---
    private void mockAuth() {
        AuthPrincipal principal = new AuthPrincipal(userId, "test@mail.com", UserRole.CUSTOMER);
        when(tokenValidator.validate(validToken)).thenReturn(principal);
    }


    // Case: token không hợp lệ
    @Test
    void testExecute_Fail_InvalidToken() {
        // Arrange
        when(tokenValidator.validate(invalidToken)).thenThrow(new SecurityException("Token không hợp lệ."));
        ViewUserProfileRequestData invalidRequest = new ViewUserProfileRequestData(invalidToken);

        // Act
        useCase.execute(invalidRequest);

        // Assert
        ArgumentCaptor<ViewUserProfileResponseData> captor = ArgumentCaptor.forClass(ViewUserProfileResponseData.class);
        verify(outputBoundary).present(captor.capture());
        ViewUserProfileResponseData response = captor.getValue();

        assertFalse(response.success);
        assertEquals("Token không hợp lệ.", response.message);
    }

    // Case: thành công
    @Test
    void testExecute_Success() {
        // Arrange
        mockAuth();

        // Giả lập dữ liệu tìm thấy từ DB
        UserData mockUser = new UserData(
            userId, 
            "nguyenvana@gmail.com", 
            "hashed_pass", 
            "Nguyen", 
            "Van A", 
            "0912345678", 
            UserRole.CUSTOMER, 
            AccountStatus.ACTIVE, 
            Instant.now(), 
            Instant.now()
        );
        when(userRepository.findByUserId(userId)).thenReturn(mockUser);

        // Act
        useCase.execute(request);

        // Assert
        ArgumentCaptor<ViewUserProfileResponseData> captor = ArgumentCaptor.forClass(ViewUserProfileResponseData.class);
        verify(outputBoundary).present(captor.capture());
        ViewUserProfileResponseData response = captor.getValue();

        // 1. Kiểm tra trạng thái
        assertTrue(response.success);
        assertEquals("Lấy thông tin thành công.", response.message);

        // 2. Kiểm tra dữ liệu
        assertEquals(mockUser.userId, response.userId);
        assertEquals(mockUser.email, response.email);
        assertEquals(mockUser.firstName, response.firstName);
        assertEquals(mockUser.lastName, response.lastName);
        assertEquals(mockUser.phoneNumber, response.phoneNumber);
    }
}