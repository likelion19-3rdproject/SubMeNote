package com.backend.notification.dto;

public record NotificationContext(
        String actorName,
        String subject,
        Integer daysLeft,
        String announcement
) {
    public static NotificationContext forComment(String actorName) {
        return new NotificationContext(actorName, null, null, null);
    }

    public static NotificationContext forReport(String subjectName) {
        return new NotificationContext(null, subjectName, null, null);
    }

    public static NotificationContext forExpire(String subjectName, int daysLeft) {
        return new NotificationContext(null, subjectName, daysLeft, null);
    }

    public static NotificationContext forAnnouncement(String announcement) {
        return new NotificationContext(null, null, null, announcement);
    }
}