package com.jpos.bluejay.api;

import com.jpos.user.model.User;
import com.jpos.user.model.UserRole;
import com.jpos.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> getUsers(Authentication authentication) {
        String currentRole = currentRole(authentication);
        User[] users = userService.getUsers(currentRole);
        return Arrays.stream(users)
                .map(u -> new UserResponse(u.getId() == null ? null : u.getId().toString(), u.getUsername(), u.getRole()))
                .toList();
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request, Authentication authentication) {
        String currentRole = currentRole(authentication);
        userService.addUser(request.username(), request.password(), request.role().getValue(), currentRole);
        return new UserResponse(null, request.username(), request.role().getValue());
    }

    private String currentRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElseThrow();
    }

    public record CreateUserRequest(@NotBlank String username,
                                    @NotBlank String password,
                                    @NotNull UserRole role) {
    }

    public record UserResponse(String id, String username, String role) {
    }
}
