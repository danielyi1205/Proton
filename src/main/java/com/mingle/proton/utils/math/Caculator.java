package com.mingle.proton.utils.math;

import com.mingle.proton.utils.log.MyLogger;
import com.mingle.proton.utils.math.ErrorEntity.ErrorPic;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Daniel on 2015/12/11.
 * @author Daniel - ymx_gd@163.com
 * @since 0.0.1
 */
public class Caculator {
    /** 验证用 正则 */
    public static final String VALIDATE_REGX = "[^0-9\\*\\+-/\\(\\)\\s]";
     /** 查找用 正则 */
    public static final String FIND_REGX = "[0-9]+|[\\*\\+-/\\(\\)]";

    public static final String FIND_DIGIT_REGX = "[0-9]+";


    public static final MyLogger logger = MyLogger.getSimpleLogger(Caculator.class.getName());

    private Caculator(){};
    public static Caculator newIntance(){
        return new Caculator();
    }

    public static void log(String s){
        logger.println(s);
    }
    public static void log(Object s){
        logger.println(String.valueOf(s));
    }


    /**
     * 从字符串中分割出算子
     * @param s
     * @return
     */
    public LinkedList<String> listOperator(String s){
        Pattern p = Pattern.compile(FIND_REGX);
        Matcher m = p.matcher(s);
        LinkedList<String> list = new LinkedList<>();
        while(m.find()) {
            String find = m.group();
            list.add(find);
        }
        return list;
    }

    private static ErrorEntity validate(String s) {
        Pattern pp = Pattern.compile(Caculator.VALIDATE_REGX);
        Matcher mm = pp.matcher(s);
        ErrorEntity errorEntity = new ErrorEntity();
        errorEntity.setErrorPics(new ArrayList<ErrorPic>());
        while(mm.find()){
            String invalid =  mm.group();
            ErrorPic errorPic = new ErrorPic();
            errorPic.setStartIndex(mm.start());
            errorPic.setStr(invalid);
            errorEntity.getErrorPics().add(errorPic);
        }
        return errorEntity;
    }

    public boolean isDigit(String s){
        Pattern pp = Pattern.compile(Caculator.FIND_DIGIT_REGX);
        Matcher mm = pp.matcher(s);
        return mm.matches();
    }

    public Number caculate(String expression){
        if(StringUtils.isBlank(expression)){
            //字符串验证
            return null;
        }
        ErrorEntity errorEntity = validate(expression);
        if(errorEntity.countError()>0){
            log("错误 ： " + errorEntity.defualtErrorMsg());
            return null;
        }
        LinkedList<String> listA = listOperator(expression);
        log("原始串：" + String.valueOf(listA));

        LinkedList<String> statckTemp = new LinkedList();
        LinkedList<String> listB = new LinkedList();

        while(!listA.isEmpty()){
            String ele = listA.poll();
            if(isDigit(ele)){
                listB.offer(ele);
            }
            else if(Operator.isOperator(ele)){
                if(statckTemp.isEmpty()){
                    statckTemp.push(ele);
                } else {
                    if(notLower(statckTemp.getLast(), ele)){
                        listB.offer(statckTemp.pop());
                    }
                    statckTemp.push(ele);
                }
            }
        }

        //清空 中转栈，并取出数据
        while(!statckTemp.isEmpty()){
            listB.offer(statckTemp.pop());
        }

        log("原始表：" + listA.size());
        log("结果表：" + listB.size());
        log("中间表：" + statckTemp.size());
        log("后缀串：" + listB);

        while(!listB.isEmpty()){
            String ele = listB.poll();
            if(isDigit(ele)){
                statckTemp.push(ele);
            }
            else if(Operator.isOperator(ele)){
                Number n1 = Double.parseDouble(statckTemp.pop());
                Number n2 = Double.parseDouble(statckTemp.pop());

                Number result = Operator.parse(ele).closeLog().operate(n2,n1);
                statckTemp.push(String.valueOf(result));
            }
        }

        return Double.parseDouble(statckTemp.pop());
    }

    public boolean notLower(String higher, String lower){
       return Operator.parse(higher).notLowerThen(Operator.parse(lower));
    }
}
