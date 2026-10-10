package com.liquidpackage.blockEntity.container.iface;

public enum ContainerDataEnum {
    HEAT_PERCENT,
    MAX_PROGRESS,
    PROGRESS;

    public int id() {
        return this.ordinal();
    }

    public static ContainerDataEnum fromId(int id) {
        if (id >= 0 && id < values().length) {
            return values()[id];
        }
        return null;
    }
}
