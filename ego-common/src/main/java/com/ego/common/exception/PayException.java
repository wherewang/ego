package com.ego.common.exception;


import com.ego.common.enums.ExceptionEnum;

/**
 * @Copyright (C), 2018-2019
 * @Author: JAVA在召唤
 * @Date: 2019-03-11 18:21
 * @Description:
 */
public class PayException extends RuntimeException {

    private ExceptionEnum exceptionEnum;

    public PayException(ExceptionEnum exceptionEnum) {
        this.exceptionEnum = exceptionEnum;
    }


    public ExceptionEnum getExceptionEnum() {
        return exceptionEnum;
    }
}
