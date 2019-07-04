package com.ego.common.exception;

/**
 * @Author: yaorange
 */
public class MyException extends RuntimeException {

    public MyException(EgoException exception) {
        super(exception.toString());
    }
}
