package com.ecom.model;

public enum RoomStatus {
    AVAILABLE("Trống", "success"),
    RESERVED("Đã cọc", "warning"),
    OCCUPIED("Đang thuê", "info"),
    NOTICE_PERIOD("Sắp trống", "secondary"),
    MAINTENANCE("Bảo trì", "danger");

    private final String displayName;
    private final String badgeClass;

    RoomStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
