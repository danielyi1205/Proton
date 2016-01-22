package com.mingle.proton.utils.log;

import com.mingle.proton.spi.LogLevel;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Daniel on 2016/1/7.
 *
 * @author Daniel - ymx_gd@163.com
 * @since 0.0.1
 */
public class MySimpleLogger extends MyLogger {

    private Logger logger;
    private Level logLevel;
    public MySimpleLogger(String name) {
        this.logger = Logger.getLogger(name);
        super.setLevel(LogLevel.INFO);
        this.logLevel = Level.INFO;
    }

    @Override
    public void setLogLevel(LogLevel level) {
        super.setLevel(level);
        switch(level){
            case ALL:{
                this.logLevel = Level.ALL;
                break;
            }
            case DEBUG:{
                this.logLevel = Level.CONFIG;
                break;
            }
            case INFO:{
                this.logLevel = Level.INFO;
                break;
            }
            case WARN:{
                this.logLevel = Level.WARNING;
                break;
            }
            case ERROR:{
                this.logLevel = Level.FINE;
                break;
            }
            case FATAL:{
                this.logLevel = Level.FINEST;
                break;
            }
             case OFF:{
                this.logLevel = Level.OFF;
                break;
            }

        }
    }

    @Override
    public void println(String s) {
        if(LogLevel.OFF.equals(this.getLevel()))
            return;
        logger.log(this.logLevel,s);
    }
}
