package com.mingle.proton.spi;

/**
 * Created by Daniel on 2016/1/7.
 *
 * @author Daniel - ymx_gd@163.comh
 * @since 0.0.1
 */
public interface ILog {
    /**
     * 设置输出器
     */
    void setRender(AbsLogRender render);


    /**
     *
     * 设置级别
     */
    void setLogLevel(LogLevel level);

    /**
     * 输出日志
     * @param s
     */
    void println(String s);
}
