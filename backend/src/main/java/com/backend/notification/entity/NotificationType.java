// =========================
package com.backend.notification.entity;

import com.backend.notification.dto.NotificationContext;

public enum NotificationType {

    COMMENT_CREATED {
        @Override
        public Message createMessage(NotificationContext ctx) {
            return Message.of(
                    "새 댓글",
                    ctx.actorName() + "님이 회원님의 글에 댓글을 남겼습니다."
            );
        }
    },

    COMMENT_REPORTED {
        @Override
        public Message createMessage(NotificationContext ctx) {
            return Message.of("댓글 신고", "회원님의 댓글이 신고되었습니다: " + ctx.subject());
        }
    },

    POST_REPORTED {
        @Override
        public Message createMessage(NotificationContext ctx) {
            return Message.of("게시글 신고", "회원님의 게시글이 신고되었습니다: " + ctx.subject());

        }
    },

    SUBSCRIBE_EXPIRE_SOON {
        @Override
        public Message createMessage(NotificationContext ctx) {
            return Message.of(
                    "구독 만료 예정",
                    ctx.subject() + " 구독이 " + ctx.daysLeft() + "일 후 만료됩니다."
            );
        }
    },

    ANNOUNCEMENT {
        @Override
        public Message createMessage(NotificationContext ctx) {
            return Message.of(
                    "공지사항",
                    ctx.announcement()
            );
        }
    };

    public abstract Message createMessage(NotificationContext ctx);

    public record Message(String title, String body) {
        public static Message of(String title, String body) {
            return new Message(title, body);
        }
    }
}