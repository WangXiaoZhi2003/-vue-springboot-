package com.code.project.domain;

public interface MailStats {
    Long getUnreadCount();
    Long getStarredCount();
    Long getSentCount();
    Long getDraftCount();
}