package com.ecommerce.project.user.internal.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ecommerce.project.user.api.request.AuthorizeSellerReq;
import com.ecommerce.project.user.api.response.AuthorizeSellerRes;
import com.ecommerce.project.user.internal.domain.User;
import com.ecommerce.project.user.internal.domain.UserRole;
import com.ecommerce.project.user.internal.domain.UserStatus;
import com.ecommerce.project.user.internal.persist.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserApiImplTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserApiImpl userApi = new UserApiImpl(userRepository);

    @Test
    @DisplayName("활성 판매자는 상품 관리 권한 확인에 성공한다")
    void active_seller_is_authorized() {
        // given
        User seller = mock(User.class);
        when(seller.getRole()).thenReturn(UserRole.SELLER);
        when(seller.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));

        // when
        AuthorizeSellerRes result = userApi.authorizeSeller(new AuthorizeSellerReq(1L));

        // then
        assertTrue(result.authorized());
    }

    @Test
    @DisplayName("판매자가 아니거나 활성 상태가 아니면 상품 관리 권한이 없다")
    void only_active_seller_is_authorized() {
        // given
        User buyer = mock(User.class);
        when(buyer.getRole()).thenReturn(UserRole.BUYER);
        when(buyer.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));

        // when
        AuthorizeSellerRes result = userApi.authorizeSeller(new AuthorizeSellerReq(1L));

        // then
        assertFalse(result.authorized());
    }

    @Test
    @DisplayName("활성 구매자만 주문 생성 사용자로 확인된다")
    void only_active_buyer_exists_for_order() {
        User buyer = mock(User.class);
        when(buyer.getRole()).thenReturn(UserRole.BUYER);
        when(buyer.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));

        assertTrue(userApi.existsActiveBuyer(1L));
    }

    @Test
    @DisplayName("구매자가 아니거나 탈퇴한 사용자는 주문 생성 사용자로 확인되지 않는다")
    void inactive_or_non_buyer_cannot_create_order() {
        User seller = mock(User.class);
        when(seller.getRole()).thenReturn(UserRole.SELLER);
        when(seller.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));

        assertFalse(userApi.existsActiveBuyer(1L));
        assertFalse(userApi.existsActiveBuyer(null));
    }
}
