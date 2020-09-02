package com.baidu.shop.utils;

/**
 * @ClassName StringUtil
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/8/31
 * @Version V1.0
 **/
public class StringUtil {

    public static Boolean isEmpty(String str){

        return null == str || "".equals(str);
    }

    public static Boolean isNotEmpty(String str){

        return null != str && !"".equals(str);
    }

    //将string类型的数据转换成Integer类型
    public static Integer toInteger(String str){

        if(isNotEmpty(str)) return Integer.parseInt(str);
        return 0;
    }
}
