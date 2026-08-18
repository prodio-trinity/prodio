package com.prodio.catalog.domain;

public enum TopCategory {
    INPUT("INP", "입력장치", "catalog_product_code_seq_input"),
    DISPLAY("DISP", "디스플레이", "catalog_product_code_seq_display"),
    STORAGE("STOR", "저장장치", "catalog_product_code_seq_storage"),
    COMPUTING("COMP", "컴퓨팅", "catalog_product_code_seq_computing"),
    AUDIO("AUD", "오디오", "catalog_product_code_seq_audio"),
    ACCESSORY("ACC", "액세서리", "catalog_product_code_seq_accessory");

    private final String codePrefix;
    private final String displayName;
    private final String sequenceName;

    TopCategory(String codePrefix, String displayName, String sequenceName) {
        this.codePrefix = codePrefix;
        this.displayName = displayName;
        this.sequenceName = sequenceName;
    }

    public static TopCategory fromCode(String categoryCode) {
        try {
            return TopCategory.valueOf(categoryCode);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "catalog_sub_categories에는 있는데 TopCategory enum에 없는 대분류: " + categoryCode, e);
        }
    }

    public String codePrefix() { return codePrefix; }
    public String displayName() { return displayName; }
    public String sequenceName() { return sequenceName; }
}