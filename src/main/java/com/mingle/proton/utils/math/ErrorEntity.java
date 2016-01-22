package com.mingle.proton.utils.math;

/**
 * Created by Daniel on 2016/1/6.
 *
 * @author Daniel - ymx_gd@163.com
 * @since 0.0.1
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * 错误体
 */
public class ErrorEntity{
    private List<ErrorPic> errorPics;

    //错误数量
    public int countError(){
        if(this.errorPics == null || this.errorPics.isEmpty()){
            return 0;
        }
        return this.errorPics.size();
    }

    public String defualtErrorMsg(){
        if(this.countError() <=0){
            return "NO ERROR";
        }
        try {
            return new ObjectMapper().writeValueAsString(this.errorPics);
        } catch(JsonProcessingException e) {
            e.printStackTrace();
        }
        return "NO ERROR";
    }

    public List<ErrorPic> getErrorPics() {
        return errorPics;
    }

    public void setErrorPics(List<ErrorPic> errorPics) {
        this.errorPics = errorPics;
    }


    /**
     * 错误子
     */
    public static class ErrorPic{
        private String str;
        private int startIndex;

        public String getStr() {
            return str;
        }

        public void setStr(String str) {
            this.str = str;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public void setStartIndex(int startIndex) {
            this.startIndex = startIndex;
        }
    }
}
