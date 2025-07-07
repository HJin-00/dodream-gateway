package shop.dodream.gatewayserver.repository;

import shop.dodream.gatewayserver.dto.SessionUser;

import java.util.Optional;

public interface TokenRepository {
    Optional<SessionUser> findSessionUser(String uuid);
    Optional<String> findRefreshToken(String uuid);
}
