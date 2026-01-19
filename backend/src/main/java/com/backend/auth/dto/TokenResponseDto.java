package com.backend.auth.dto;

public record TokenResponseDto(String accessToken, String refreshToken) {

    public static TokenResponseDto from(String accessToken , String refreshToken){
        return new TokenResponseDto(
                accessToken,
                refreshToken
        );
    }

}
