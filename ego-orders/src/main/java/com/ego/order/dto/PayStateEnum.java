package com.ego.order.dto;

/**
 * @author yaorange
 * @date 2018/10/5
 */
public enum PayStateEnum {

    NOT_PAY(0), SUCCESS(1), FAIL(2);

    int value;

    PayStateEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
