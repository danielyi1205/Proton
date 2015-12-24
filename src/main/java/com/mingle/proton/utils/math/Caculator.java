package com.mingle.proton.utils.math;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Pattern;

/**
 * Created by Daniel on 2015/12/11.
 * @author Daniel - ymx_gd@163.com
 * @since 0.0.1
 */
public class Caculator {




    public static void main(String[] args){
        new Caculator().test();
        new Caculator().test1();
//        new Caculator().caculate("4+54*6-8/9");
    }

    public void test(){
        char a = 'a';
        System.out.println(a==97);
    }
    public void test1(){
        Pattern pattern = Pattern.compile("[0-9]++");
    }

    public BigDecimal caculate(String expression){
        if(StringUtils.isBlank(expression)){
            //字符串验证
            return null;
        }
        char[] chars = expression.toLowerCase().toCharArray();

        System.out.println("原始串：");
        for(int i =0; i<chars.length; i++ ){
            System.out.print(chars[i]+",");
        }
        System.out.println();

        LinkedList<Character> statck = new LinkedList();
        ArrayList<Character> chars2 = new ArrayList();

        for(int i =0; i<chars.length; i++ ){
            if(Character.isDigit(chars[i])){
                chars2.add(chars[i]);
            }
            if(Operator.isOperator(chars[i])){
                if(statck.isEmpty()){
                    statck.push(chars[i]);
                } else {
                    if(notLower(statck.getLast(), chars[i])){
                        chars2.add(statck.pop());
                    }
                    statck.push(chars[i]);
                }
            }
        }

        System.out.println("后缀串：");
        for(Character c : chars2){
            System.out.print(c+",");
        }
        System.out.println();
        return null;
    }

    public boolean notLower(char higher, char lower){
       return Operator.parse(higher).notLowerThen(Operator.parse(lower));
    }
}
