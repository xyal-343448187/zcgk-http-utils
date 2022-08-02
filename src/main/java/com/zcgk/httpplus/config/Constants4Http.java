package com.zcgk.httpplus.config;

/****************************************************
 *
 * 常量
 *
 *
 * @author Francis
 * @date 2020/1/4 15:12
 * @version 1.0
 **************************************************/
public class Constants4Http {

    /**
     * 接口返回类型： json or string
     */
    public static final String RESPONSE_TYPE_4_JSON = "json";
    public static final String RESPONSE_TYPE_4_STRING = "string";


    /**
     * 接口数据返回类型： 单个 or 多个
     */
    public static final String RESPONSE_DATA_TYPE_4_OBJ = "obj";
    public static final String RESPONSE_DATA_TYPE_4_LIST = "list";


    /**
     * 接口入参 传参类型： form表单传单/url传参、 json传参、file文件上传
     */
    public static final String REQUEST_PARAM_TYPE_4_APPLICATION_FORM_URLENCODED = "x-www-form-urlencoded";
    public static final String REQUEST_PARAM_TYPE_4_FORM_OR_URL = "form/url";
    public static final String REQUEST_PARAM_TYPE_4_JSON = "json";
    public static final String REQUEST_PARAM_TYPE_4_FILE = "file";

}
