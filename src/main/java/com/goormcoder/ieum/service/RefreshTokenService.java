package com.goormcoder.ieum.service;

import com.goormcoder.ieum.domain.RefreshToken;
import com.goormcoder.ieum.repository.RefreshTokenRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void save(String token) {
        RefreshToken refreshToken = new RefreshToken(token);
        refreshTokenRepository.save(refreshToken);
        isExists(token);
    }

    public boolean isExists(String token) {
        return refreshTokenRepository.existsById(token);
    }

    @Transactional
    public void expire(String token) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findById(token);
        refreshToken.ifPresent(refreshTokenRepository::delete);
    }

}
