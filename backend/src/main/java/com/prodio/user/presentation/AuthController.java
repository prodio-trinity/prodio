package com.prodio.user.presentation;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import com.prodio.shared.ApiResponse;
import com.prodio.user.application.SignupCommand;
import com.prodio.user.application.SignupService;
import com.prodio.user.domain.UserAccount;
import com.prodio.user.infrastructure.security.ProdioPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
class AuthController {
    private final SignupService signupService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    record SignupRequest(String email, String password, String name) {}

    record MemberResponse(String id, String email, String name, Set<String> roles) {
        static MemberResponse of(long id, String email, String name, Set<String> roles) {
            return new MemberResponse(Long.toString(id), email, name, roles);
        }
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<MemberResponse> signup(@RequestBody SignupRequest request) {
        UserAccount created = signupService.signup(
                new SignupCommand(request.email(), request.password(), request.name()));
        return ApiResponse.success("회원가입이 완료되었습니다.",
                MemberResponse.of(created.id(), created.email(), created.name(), Set.of("STAFF")));
    }

    record LoginRequest(String email, String password) {}

    @PostMapping("/login")
    ApiResponse<MemberResponse> login(@RequestBody LoginRequest request,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authenticated = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticated);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        ProdioPrincipal principal = (ProdioPrincipal) authenticated.getPrincipal();
        Set<String> roles = new LinkedHashSet<>(authenticated.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .toList());
        return ApiResponse.success(MemberResponse.of(principal.userId(), principal.email(), principal.name(), roles));
    }
}
