package cgx.com.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cgx.com.infrastructure.presentation.WebPresenter;
import cgx.com.usecase.ManageUser.AuthenticateUser.AuthenticateUserRequestData;
import cgx.com.usecase.ManageUser.AuthenticateUser.LoginByEmailUseCase;
import cgx.com.usecase.ManageUser.RegisterUser.RegisterByEmailUseCase;
import cgx.com.usecase.ManageUser.RegisterUser.RegisterUserRequestData;
import cgx.com.usecase.ManageUser.VerifyEmail.VerifyEmailRequestData;
import cgx.com.usecase.ManageUser.VerifyEmail.VerifyEmailUseCase;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterByEmailUseCase registerUseCase;
    private final LoginByEmailUseCase loginUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final WebPresenter presenter;

    public AuthController(RegisterByEmailUseCase registerUseCase,
                          LoginByEmailUseCase loginUseCase,
                          VerifyEmailUseCase verifyEmailUseCase,
                          WebPresenter presenter) {
        this.registerUseCase = registerUseCase;
        this.loginUseCase = loginUseCase;
        this.verifyEmailUseCase = verifyEmailUseCase;
        this.presenter = presenter;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterUserRequestData request) {
        // 1. Gọi Use Case
        registerUseCase.execute(request);
        
        // 2. Lấy kết quả từ Presenter
        return ResponseEntity.ok(presenter.getResponse());
    }
    
 // GET http://localhost:8080/api/auth/verify?token=...
    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        VerifyEmailRequestData request = new VerifyEmailRequestData(token);
        verifyEmailUseCase.execute(request);
        return ResponseEntity.ok(presenter.getResponse());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticateUserRequestData request) {
        loginUseCase.execute(request);
        return ResponseEntity.ok(presenter.getResponse());
    }
}