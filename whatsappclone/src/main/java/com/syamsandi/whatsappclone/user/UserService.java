package com.syamsandi.whatsappclone.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserResponse> findAllUsersExceptSelf(Authentication authentication) {
        String currentUser = authentication.getName();
        return userRepository.findAllUsersExceptSelf(currentUser)
                .stream()
                .map(userMapper::toUserResponse)
                .toList();
    }
}
