package dev.hivens.hidemymods.common;

/**
 * Constants shared by the forge-1.7.10 and forge-1.12.2 sources.
 * The neoforge-1.21.1 subproject targets Java 21 source and has its
 * own copy because the _common-src convention only works for loader
 * subprojects that compile against Java 8.
 */
public final class ModMeta {
    public static final String MOD_ID   = "hidemymods";
    public static final String MOD_NAME = "hidemymods";

    private ModMeta() {}
}
