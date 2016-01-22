package com.mingle.proton.utils.log;

import com.mingle.proton.spi.AbsLogRender;
import com.mingle.proton.spi.ILog;
import com.mingle.proton.spi.LogLevel;

/**
 * Created by Daniel on 2016/1/8.
 *
 * @author Daniel - ymx_gd@163.com
 * @since 0.0.1
 */
public abstract class MyLogger implements ILog {
    private LogLevel level;

    public static MySimpleLogger getSimpleLogger(String s){
        return new MySimpleLogger(s);
    }

    public LogLevel getLevel() {
        return level;
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }

    public LogLevel getLogLevel() {
        return level;
    }

    @Override
    public void setRender(AbsLogRender render) {
    }

    @Override
    public void setLogLevel(LogLevel level) {
    }

    @Override
    public void println(String s) {
    }
}
