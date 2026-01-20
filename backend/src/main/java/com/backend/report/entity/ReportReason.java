package com.backend.report.entity;

public enum ReportReason {
    SPAM,                   // 광고/도배
    HARASSMENT,             // 괴롭힘, 혐오, 차별
    SEXUAL,                 // 성적 콘텐츠
    VIOLENCE,               // 폭력/위협
    ILLEGAL,                // 불법 행위/정보
    COPYRIGHT,              // 저작권 침해
    PRIVACY,                // 개인정보 침해
    MISINFORMATION,         // 허위/오정보
    OFF_TOPIC,              // 주제와 무관
    ETC
}