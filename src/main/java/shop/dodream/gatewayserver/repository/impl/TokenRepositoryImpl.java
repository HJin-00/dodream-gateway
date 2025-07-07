package shop.dodream.gatewayserver.repository.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import shop.dodream.gatewayserver.dto.Role;
import shop.dodream.gatewayserver.dto.SessionUser;
import shop.dodream.gatewayserver.repository.TokenRepository;

import java.util.LinkedHashMap;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TokenRepositoryImpl implements TokenRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private static final String KEY_PREFIX = "session:";
    private static final String REFRESH_KEY_PREFIX = "refresh:";

    @Override
    public Optional<SessionUser> findSessionUser(String uuid) {
        Object result = redisTemplate.opsForValue().get(KEY_PREFIX + uuid);
        log.debug("find session user: {}", result);
        if (result instanceof LinkedHashMap map) {
            String userId = map.get("userId").toString();
            String roleStr = map.get("role").toString();
            Role role = Role.valueOf(roleStr);
            SessionUser sessionUser = new SessionUser(userId,role);
            return Optional.of(sessionUser);
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> findRefreshToken(String uuid) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(REFRESH_KEY_PREFIX + uuid));
    }
}
