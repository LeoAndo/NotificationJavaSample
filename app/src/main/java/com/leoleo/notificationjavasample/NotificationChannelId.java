package com.leoleo.notificationjavasample;

import androidx.annotation.NonNull;

// https://developer.android.com/training/notify-user/channels?hl=ja#importance
public enum NotificationChannelId {
    MIN_PRIORITY("重要度 低の通知チャネル"),
    LOW_PRIORITY("重要度 中の通知チャネル"),
    DEFAULT_PRIORITY("重要度 高の通知チャネル"),
    HIGH_PRIORITY("重要度 緊急の通知チャネル");
    @NonNull
    private final String userVisibleName;

    NotificationChannelId(@NonNull final String userVisibleName) {
        this.userVisibleName = userVisibleName;
    }

    @NonNull
    public String getUserVisibleName() {
        return userVisibleName;
    }
}