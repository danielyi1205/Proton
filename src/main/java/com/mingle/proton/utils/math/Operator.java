package com.mingle.proton.utils.math;

import com.mingle.proton.spi.LogLevel;
import com.mingle.proton.utils.log.MyLogger;

import java.util.HashMap;

/**
 * Created by Daniel on 2015/12/11.
 *
 * @author Daniel - ymx_gd@163.com
 * @since 0.0.1
 */
public enum Operator{
    plus("+",1),
    minus("-",1),
    multiply("*",2),
    divide("/",2),
    ;
    private String symbol;
    private int priority;
    private MyLogger logger ;

    public static final HashMap<String,Operator> map = new HashMap<>();

    static {
        map.put(plus.symbol,plus);
        map.put(minus.symbol,minus);
        map.put(multiply.symbol,multiply);
        map.put(divide.symbol,divide);
    }

    private Operator(String symbol, int priority){
        this.symbol = symbol;
        this.priority = priority;
        this.logger = MyLogger.getSimpleLogger(this.getClass().getName());
    }

    public Operator closeLog(){
        this.logger.setLevel(LogLevel.OFF);
        return this;
    }
    public Operator openLog(){
        this.logger.setLevel(LogLevel.INFO);
        return this;
    }
    /**
     * 优先级大于或等于入参
     * @param o
     * @return
     */
    public boolean notLowerThen(Operator o){
        return this.priority >= o.priority;
    }

    public static Operator parse(String c){
        return map.get(c);
    }

    public Number operate(Number n1, Number n2){
        if(n1 == null || n2 == null){
            return null;
        }
        switch(this){
            case plus:{
                Number result = n1.doubleValue() + n2.doubleValue();
                logger.println(n1.doubleValue() + " + " + n2.doubleValue() + " = " + result);
                return result;
            }
            case minus:{
                Number result = n1.doubleValue() - n2.doubleValue();
                logger.println(n1.doubleValue() +" - "+ n2.doubleValue() + " = " + result);
                return result;
            }
            case multiply:{
                Number result = n1.doubleValue() * n2.doubleValue();
                logger.println(n1.doubleValue() +" * "+ n2.doubleValue() + " = " + result);
                return result;
            }
            case divide:{
                Number result = n1.doubleValue() / n2.doubleValue();
                logger.println(n1.doubleValue() +" / "+ n2.doubleValue() + " = " + result);
                return result;
            }
            default:
                return null;
        }
    }

    public static boolean isOperator(String c){
        for(Operator ope : Operator.values()){
            if(ope.symbol.equals(c)){
                return true;
            }
        }
        return false;
    }
}
