package com.mingle.proton.spi;

/**
 * Created by Daniel on 2016/1/8.
 *
 * @author Daniel - ymx_gd@163.com
 * @since 0.0.1
 */
public enum LogLevel {
    /**
     * 最低级别，输出所有
     */
    ALL((byte)0),
    /**
     * 输出debug 及以上
     */
    DEBUG((byte)1),
    /**
     * 输出info 及以上
     */
    INFO((byte)2),
    /**
     * 输出warn 及以上
     */
    WARN((byte)3),
    /**
     * 输出error 及以上
     */
    ERROR((byte)4),
    /**
     * 输出fatal  最高级别的日志
     */
    FATAL((byte)5),
    /**
     * 最高级别， 关闭日志输出
     */
    OFF((byte)6);

    private byte weight;
    private LogLevel(byte weight){
        this.weight = weight;
    }
}
