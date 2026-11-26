package com.ecommerce.project.user.internal.application;

import com.ecommerce.project.user.api.UserApi;
import com.ecommerce.project.user.api.request.AuthorizeSellerReq;
import com.ecommerce.project.user.api.response.AuthorizeSellerRes;
import com.ecommerce.project.user.internal.domain.User;
import com.ecommerce.project.user.internal.domain.UserRole;
import com.ecommerce.project.user.internal.domain.UserStatus;
import com.ecommerce.project.user.internal.persist.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserApiImpl implements UserApi {

    private final UserRepository userRepository;

    @Override
    public AuthorizeSellerRes authorizeSeller(final AuthorizeSellerReq request) {
        if (request == null || request.userId() == null) {
            return new AuthorizeSellerRes(false);
        }

        return new AuthorizeSellerRes(
                userRepository.findById(request.userId())
                        .map(this::isActiveSeller)
                        .orElse(false)
        );
    }

    private boolean isActiveSeller(final User user) {
        return user.getRole() == UserRole.SELLER && user.getStatus() == UserStatus.ACTIVE;
    }
}
