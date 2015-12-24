package com.mingle.proton.utils.math;

import java.util.HashMap;

/**
 * Created by Daniel on 2015/12/11.
 *
 * @author Daniel - ymx_gd@163.com
 * @since 0.0.1
 */
public enum Operator{
    plus('+',1),
    minus('-',1),
    multiply('*',2),
    divide('/',2),
    ;
    private char symbol;
    private int priority;

    public static final HashMap<Character,Operator> map = new HashMap<>();

    static {
        map.put(plus.symbol,plus);
        map.put(minus.symbol,plus);
        map.put(multiply.symbol,plus);
        map.put(divide.symbol,plus);
    }

    private Operator(char symbol, int priority){
        this.symbol = symbol;
        this.priority = priority;
    }

    /**
     * 优先级大于或等于入参
     * @param o
     * @return
     */
    public boolean notLowerThen(Operator o){
        return this.priority >= o.priority;
    }

    public static Operator parse(char c){
        return map.get(c);
    }

    public static boolean isOperator(char c){
        for(Operator ope : Operator.values()){
            if(ope.symbol==c){
                return true;
            }
        }
        return false;
    }
}
