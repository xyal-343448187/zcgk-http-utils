package com.zcgk.httpplus.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zcgk.httpplus.config.Constants4Http;
import com.zcgk.httpplus.core.domain.vo.resp.JavaBean4Integer;
import com.zcgk.httpplus.core.domain.vo.resp.JavaBean4String;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;

/****************************************************
 *
 * http 调用工具类
 *
 *
 * @author Francis
 * @date 2020/7/3 10:12
 * @version 1.0
 **************************************************/
@Slf4j
public class HClientUtils {

    /**
     * http-get 请求 api 封装
     *
     *
     * @param url 接口url。如：http://127.0.0.1:8080/user?args={args} 此处的{args}与paramMap中的key对应
     * @param clazz 返回值的泛型。 t.class
     * @param paramMap 参数键值对。如：[{"args": "自定义值"}] 此处的key与paramMap中的key对应
     * @param resultMap 返回成功的判断标识。如：[{"code": "200"}]  or [{"state": "true"}] 等。根据实际情况填写
     * @param dataFieldName 返回值的字段名。如：data
     * @param responseType 返回类型。json or string。此处固定。不可自定义
     * @param responseDataType 返回值有一个还是多个。obj or list。此处固定，不可自定义
     * @param <T> 泛型
     * @return tList 若返回值仅一个，调用方 tList.get(0) 取值即可。
     */
    public static <T> List<T> getApi (String url, Class<T> clazz, Map<String, Object> paramMap,
                                      Map<String, String> resultMap, String dataFieldName,
                                      String responseType, String responseDataType) {
        List<T> tList = null;

        // 处理请求参数
        paramMap = CollectionUtils.isEmpty(paramMap) ? new HashMap<>() : paramMap;

        switch (responseType) {
            case Constants4Http.RESPONSE_TYPE_4_JSON:
                if (Constants4Http.RESPONSE_DATA_TYPE_4_OBJ.equals(responseDataType)) {
                    // 返回单个
                    T t = get4Json2Obj(url, clazz, paramMap, resultMap, dataFieldName);

                    List<T> tempList = new ArrayList<>();
                    tempList.add(t);
                    tList = tempList;
                } else if (Constants4Http.RESPONSE_DATA_TYPE_4_LIST.equals(responseDataType)){
                    // 返回多个
                    tList = get4Json2List(url, clazz, paramMap, resultMap, dataFieldName);
                }

                break;
            case Constants4Http.RESPONSE_TYPE_4_STRING:
                if (Constants4Http.RESPONSE_DATA_TYPE_4_OBJ.equals(responseDataType)) {
                    // 返回单个
                    T t = get4String2Obj(url, clazz, paramMap, resultMap, dataFieldName);

                    List<T> tempList = new ArrayList<>();
                    tempList.add(t);
                    tList = tempList;
                } else if (Constants4Http.RESPONSE_DATA_TYPE_4_LIST.equals(responseDataType)) {
                    // 返回多个
                    tList = get4String2List(url, clazz, paramMap, resultMap, dataFieldName);
                }
                break;
            default:
                break;
        }

        return tList;
    }


    /**
     * http-post 请求 api 封装
     *      传参方式： form表单 或 url参数
     *
     * @param url 接口url。如：http://127.0.0.1:8080/user
     * @param clazz 返回值的泛型。 t.class
     * @param paramMap 参数键值对。如：[{"args": "自定义值"}] 此处的key与paramMap中的key对应
     * @param resultMap 返回成功的判断标识。如：[{"code": "200"}]  or [{"state": "true"}] 等。根据实际情况填写
     * @param dataFieldName 返回值的字段名。如：data
     * @param requestParamType 传参方式。form表单/url参数 or json。此处固定。不可自定义
     * @param responseType 返回类型。json or string。此处固定。不可自定义
     * @param responseDataType 返回值有一个还是多个。obj or list。此处固定，不可自定义
     * @param <T> 泛型
     * @return tList 若返回值仅一个，调用方 tList.get(0) 取值即可。
     */
    public static <T> List<T> postApi (String url, Class<T> clazz, Map<String, Object> paramMap,
                                      Map<String, String> resultMap, String dataFieldName,
                                      String requestParamType, String responseType, String responseDataType) {
        HttpHeaders header = new HttpHeaders();
        return postApi(url, clazz, paramMap, resultMap, dataFieldName, header, requestParamType, responseType, responseDataType);
    }



    /**
     * http-post 请求 api 封装
     *      传参方式： form表单 或 url参数
     *
     * @param url 接口url。如：http://127.0.0.1:8080/user
     * @param clazz 返回值的泛型。 t.class
     * @param paramMap 参数键值对。如：[{"args": "自定义值"}] 此处的key与paramMap中的key对应
     * @param resultMap 返回成功的判断标识。如：[{"code": "200"}]  or [{"state": "true"}] 等。根据实际情况填写
     * @param dataFieldName 返回值的字段名。如：data
     * @param header 请求头
     * @param requestParamType 传参方式。form表单/url参数 or json。此处固定。不可自定义
     * @param responseType 返回类型。json or string。此处固定。不可自定义
     * @param responseDataType 返回值有一个还是多个。obj or list。此处固定，不可自定义
     * @param <T> 泛型
     * @return tList 若返回值仅一个，调用方 tList.get(0) 取值即可。
     */
    public static <T> List<T> postApi (String url, Class<T> clazz, Map<String, Object> paramMap,
                                       Map<String, String> resultMap, String dataFieldName, HttpHeaders header,
                                       String requestParamType, String responseType, String responseDataType) {
        List<T> tList = null;
        HttpEntity request = null;

        // 封装请求体
        paramMap = CollectionUtils.isEmpty(paramMap) ? new HashMap<>() : paramMap;

        if (Constants4Http.REQUEST_PARAM_TYPE_4_FORM_OR_URL.equals(requestParamType)
                || Constants4Http.REQUEST_PARAM_TYPE_4_FILE.equals(requestParamType)
                || Constants4Http.REQUEST_PARAM_TYPE_4_APPLICATION_FORM_URLENCODED.equals(requestParamType)) {
            if (Constants4Http.REQUEST_PARAM_TYPE_4_FILE.equals(requestParamType)) {
                // 上传文件 处理请求头
                header.setContentType(MediaType.MULTIPART_FORM_DATA);
            }
            if (Constants4Http.REQUEST_PARAM_TYPE_4_APPLICATION_FORM_URLENCODED.equals(requestParamType)) {
                // x-www-form-urlencoded
                header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            }

            // 处理请求参数
            MultiValueMap<String, Object> requestParamMap = new LinkedMultiValueMap<>();
            // 此处避免用foreach 安卓调用会出问题
            for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                requestParamMap.add(entry.getKey(), entry.getValue());
            }

            request = new HttpEntity<MultiValueMap<String, Object>>(requestParamMap, header);
        } else if (Constants4Http.REQUEST_PARAM_TYPE_4_JSON.equals(requestParamType)){
            // 处理请求头
            header.setContentType(MediaType.APPLICATION_JSON);

            // 处理请求参数
            JSONObject requestParamMap = new JSONObject();
            // 此处避免用foreach 安卓调用会出问题
            for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                requestParamMap.put(entry.getKey(), entry.getValue());
            }
//            paramMap.forEach(requestParamMap::put);

            request = new HttpEntity<JSONObject>(requestParamMap, header);
        } else {
            log.error("post请求，传参方式 值不符合规范");
            return null;
        }

        switch (responseType) {
            case Constants4Http.RESPONSE_TYPE_4_JSON:
                // 接口返回类型为json
                if (Constants4Http.RESPONSE_DATA_TYPE_4_OBJ.equals(responseDataType)) {
                    // 返回单个
                    T t = post4Json2Obj(url, clazz, request, resultMap, dataFieldName);

                    List<T> tempList = new ArrayList<>();
                    tempList.add(t);
                    tList = tempList;
                } else if (Constants4Http.RESPONSE_DATA_TYPE_4_LIST.equals(responseDataType)){
                    // 返回多个
                    tList = post4Json2List(url, clazz, request, resultMap, dataFieldName);
                }
                break;
            case Constants4Http.RESPONSE_TYPE_4_STRING:
                // 接口返回类型为string
                if (Constants4Http.RESPONSE_DATA_TYPE_4_OBJ.equals(responseDataType)) {
                    // 返回单个
                    T t = post4String2Obj(url, clazz, request, resultMap, dataFieldName);

                    List<T> tempList = new ArrayList<>();
                    tempList.add(t);
                    tList = tempList;
                } else if (Constants4Http.RESPONSE_DATA_TYPE_4_LIST.equals(responseDataType)) {
                    // 返回多个
                    tList = post4String2List(url, clazz, request, resultMap, dataFieldName);
                }
                break;
            default:
                break;
        }

        return tList;
    }





    /*******************************************************************************************************************
     *************************************************以下为辅助方法 无需关注**********************************************
     ******************************************************************************************************************/
    /**
     * 请求方式： http-get
     * 返回方式：json
     * 返回值：obj
     * 带参数请求示例： url: http://127.0.0.1:8080/queryTest?args={args}
     *                paramMap: [{"args": "自定义值"}]
     *                resultMap: [{"code": "200"}]
     *                备注： {args} 与 paramMap中的key 对应
     *                      若返回值是String，则参数传 JavaBean4String.class
     *
     * @param <T> 泛型
     * @param url url
     * @param clazz t.class
     * @param paramMap 参数
     * @param resultMap 返回是否成功的标志
     * @param dataFieldName 返回的 值字段名
     * @return tList
     */
    private static <T> T get4Json2Obj (String url, Class<T> clazz, Map<String, Object> paramMap, Map<String, String> resultMap, String dataFieldName) {
        JSONObject result = null;
        RestTemplate client = new RestTemplate();

        JSONObject body = client.getForEntity(url, JSONObject.class, paramMap).getBody();
        assert body != null;
        if (CollectionUtils.isEmpty(resultMap) || StringUtils.isEmpty(dataFieldName)) {
            // 直接返回data
            result = body;
        } else {
            // 有 code、msg 等一系列值
            // 判断结果返回值
            Map<String, String> msgMap = handleResponseState(resultMap, body);
            if (!CollectionUtils.isEmpty(msgMap)) {
                // 返回值判断不成功
                log.error("接口返回错误：{}。\n接口返回完整信息：{}", msgMap, body);
                return null;
            }

            if (clazz.equals(JavaBean4String.class)) {
                // 返回值是String
                result = (JSONObject) JSON.toJSON(new JavaBean4String(body.getString(dataFieldName)));
            } else if (clazz.equals(JavaBean4Integer.class)) {
                // 返回值是Integer
                result = (JSONObject) JSON.toJSON(new JavaBean4Integer(body.getInteger(dataFieldName)));
            } else {
                result = body.getJSONObject(dataFieldName);
            }
        }

        if (Objects.isNull(result)) {
            return null;
        } else {
            return result.toJavaObject(clazz);
        }
    }


    /**
     * 请求方式： http-get
     * 返回方式：json
     * 返回值：List<obj>
     * 带参数请求示例： url: http://127.0.0.1:8080/queryTest?args={args}
     *                paramMap: [{"args": "自定义值"}]
     *                resultMap: [{"code": "200"}]
     *                备注： {args} 与 paramMap中的key 对应
     *
     * @param url url
     * @param clazz t.class
     * @param paramMap 参数
     * @param resultMap 返回是否成功的标志
     * @param dataFieldName 返回的 值字段名
     * @param <T> 泛型
     * @return tList
     */
    private static <T> List<T> get4Json2List (String url, Class<T> clazz, Map<String, Object> paramMap, Map<String, String> resultMap, String dataFieldName) {
        RestTemplate client = new RestTemplate();

        if (CollectionUtils.isEmpty(resultMap) || StringUtils.isEmpty(dataFieldName)) {
            // 直接返回data
            JSONArray body = client.getForEntity(url, JSONArray.class, paramMap).getBody();

            assert body != null;
            return body.toJavaList(clazz);
        } else {
            // 有 code、msg 等一系列值
            JSONObject body = client.getForEntity(url, JSONObject.class, paramMap).getBody();
            assert body != null;
            JSONArray result = null;

            // 判断结果返回值
            Map<String, String> msgMap = handleResponseState(resultMap, body);
            if (!CollectionUtils.isEmpty(msgMap)) {
                // 返回值判断不成功
                log.error("接口返回错误：{}。\n接口返回完整信息：{}", msgMap, body);
                return null;
            }

            result = body.getJSONArray(dataFieldName);

            if (Objects.isNull(result)) {
                return null;
            } else {
                return result.toJavaList(clazz);
            }
        }
    }


    /**
     * 请求方式： http-get
     * 返回方式：string
     * 返回值：obj
     * 带参数请求示例： url: http://127.0.0.1:8080/queryTest?args={args}
     *                paramMap: [{"args": "自定义值"}]
     *                resultMap: [{"code": "200"}]
     *                备注： {args} 与 paramMap中的key 对应
     *
     * @param url url
     * @param clazz t.class
     * @param paramMap 参数
     * @param resultMap 返回是否成功的标志
     * @param dataFieldName 返回的 值字段名
     * @param <T> 泛型
     * @return tList
     */
    private static <T> T get4String2Obj (String url, Class<T> clazz, Map<String, Object> paramMap, Map<String, String> resultMap , String dataFieldName) {
        JSONObject result = null;
        RestTemplate client = new RestTemplate();
        client.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        String body = client.getForEntity(url, String.class, paramMap).getBody();
        if (CollectionUtils.isEmpty(resultMap) || StringUtils.isEmpty(dataFieldName)) {
            // 直接返回data
            result = (JSONObject) JSONObject.parse(body);
        } else {
            // 有 code、msg 等一系列值
            JSONObject jsonBody = JSONObject.parseObject(body);

            // 判断结果返回值
            Map<String, String> msgMap = handleResponseState(resultMap, jsonBody);

            if (!CollectionUtils.isEmpty(msgMap)) {
                // 返回值判断不成功
                log.error("接口返回错误：{}。\n接口返回完整信息：{}", msgMap, jsonBody);
                return null;
            }

            if (clazz.equals(JavaBean4String.class)) {
                // 返回值是String
                result = (JSONObject) JSON.toJSON(new JavaBean4String(jsonBody.getString(dataFieldName)));
            } else if (clazz.equals(JavaBean4Integer.class)) {
                // 返回值是Integer
                result = (JSONObject) JSON.toJSON(new JavaBean4Integer(jsonBody.getInteger(dataFieldName)));
            } else {
                result = JSONObject.parseObject(jsonBody.get(dataFieldName).toString());
            }
        }

        if (Objects.isNull(result)) {
            return null;
        } else {
            return result.toJavaObject(clazz);
        }
    }


    /**
     * 请求方式： http-get
     * 返回方式：string
     * 返回值：List<obj>
     * 带参数请求示例： url: http://127.0.0.1:8080/queryTest?args={args}
     *                paramMap: [{"args": "自定义值"}]
     *                resultMap: [{"code": "200"}]
     *                备注： {args} 与 paramMap中的key 对应
     *
     * @param url url
     * @param clazz t.class
     * @param paramMap 参数
     * @param resultMap 返回是否成功的标志
     * @param dataFieldName 返回的 值字段名
     * @param <T> 泛型
     * @return tList
     */
    private static <T> List<T> get4String2List (String url, Class<T> clazz, Map<String, Object> paramMap, Map<String, String> resultMap, String dataFieldName) {
        JSONArray result = null;
        RestTemplate client = new RestTemplate();
        client.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        String body = client.getForEntity(url, String.class, paramMap).getBody();
        if (CollectionUtils.isEmpty(resultMap) || StringUtils.isEmpty(dataFieldName)) {
            // 直接返回data
            result = (JSONArray) JSONArray.parse(body);
        } else {
            // 有code、msg等一系列值
            JSONObject jsonBody = JSONObject.parseObject(body);

            // 判断结果返回值
            Map<String, String> msgMap = handleResponseState(resultMap, jsonBody);

            if (!CollectionUtils.isEmpty(msgMap)) {
                // 返回值判断不成功
                log.error("接口返回错误：{}。\n接口返回完整信息：{}", msgMap, jsonBody);
                return null;
            }

            result = (JSONArray) jsonBody.get(dataFieldName);
        }

        if (Objects.isNull(result)) {
            return null;
        } else {
            return result.toJavaList(clazz);
        }
    }


    /**
     * 请求方式： http-post
     * 传参方式： form表单 or url后面跟参数
     * 返回方式： string
     * 返回值：   obj
     * 带参数请求示例： url: http://127.0.0.1:8080/queryTest
     *                paramMap: [{"args": "自定义值"}]
     *                resultMap: [{"code": "200"}]
     *
     * @param url url
     * @param clazz t.class
     * @param resultMap 返回是否成功的标志
     * @param dataFieldName 返回的 值字段名
     * @param <T> 泛型
     * @return tList
     */
    private static <T> T post4String2Obj (String url, Class<T> clazz, HttpEntity request, Map<String, String> resultMap, String dataFieldName) {
        JSONObject result = null;
        RestTemplate client = new RestTemplate();

        String body = client.postForEntity(url, request, String.class).getBody();
        if (CollectionUtils.isEmpty(resultMap) || StringUtils.isEmpty(dataFieldName)) {
            // 直接返回data
            result = (JSONObject) JSONObject.parse(body);
        } else {
            // 有 code、msg 等一系列值
            JSONObject jsonBody = JSONObject.parseObject(body);

            // 判断结果返回值
            Map<String, String> msgMap = handleResponseState(resultMap, jsonBody);
            if (!CollectionUtils.isEmpty(msgMap)) {
                // 返回值判断不成功
                log.error("接口返回错误：{}。\n接口返回完整信息：{}", msgMap, jsonBody);
                return null;
            }

            if (clazz.equals(JavaBean4String.class)) {
                // 返回值是String
                result = (JSONObject) JSON.toJSON(new JavaBean4String(jsonBody.getString(dataFieldName)));
            } else if (clazz.equals(JavaBean4Integer.class)) {
                // 返回值是Integer
                result = (JSONObject) JSON.toJSON(new JavaBean4Integer(jsonBody.getInteger(dataFieldName)));
            } else {
                result = (JSONObject) jsonBody.get(dataFieldName);
            }
        }

        if (Objects.isNull(result)) {
            return null;
        } else {
            return result.toJavaObject(clazz);
        }
    }


    /**
     * 请求方式： http-post
     * 传参方式： form表单 or url后面跟参数
     * 返回方式： string
     * 返回值：   list
     * 带参数请求示例： url: http://127.0.0.1:8080/queryTest
     *                paramMap: [{"args": "自定义值"}]
     *                resultMap: [{"code": "200"}]
     *
     * @param url url
     * @param clazz t.class
     * @param resultMap 返回是否成功的标志
     * @param dataFieldName 返回的 值字段名
     * @param <T> 泛型
     * @return tList
     */
    private static <T> List<T> post4String2List (String url, Class<T> clazz, HttpEntity request, Map<String, String> resultMap, String dataFieldName) {
        JSONArray result = null;
        RestTemplate client = new RestTemplate();

        String body = client.postForEntity(url, request, String.class).getBody();
        if (CollectionUtils.isEmpty(resultMap) || StringUtils.isEmpty(dataFieldName)) {
            // 直接返回data
            result = (JSONArray) JSONArray.parse(body);
        } else {
            // 有 code、msg 等一系列值
            JSONObject jsonBody = JSONObject.parseObject(body);

            // 判断结果返回值
            Map<String, String> msgMap = handleResponseState(resultMap, jsonBody);

            if (!CollectionUtils.isEmpty(msgMap)) {
                // 返回值判断不成功
                log.error("接口返回错误：{}。\n接口返回完整信息：{}", msgMap, jsonBody);
                return null;
            }

            result = (JSONArray) jsonBody.get(dataFieldName);
        }

        if (Objects.isNull(result)) {
            return null;
        } else {
            return result.toJavaList(clazz);
        }
    }


    /**
     * 请求方式： http-post
     * 传参方式： form表单 or url后面跟参数
     * 返回方式： json
     * 返回值：   obj
     * 带参数请求示例： url: http://127.0.0.1:8080/queryTest
     *                paramMap: [{"args": "自定义值"}]
     *                resultMap: [{"code": "200"}]
     *
     * @param url url
     * @param clazz t.class
     * @param resultMap 返回是否成功的标志
     * @param dataFieldName 返回的 值字段名
     * @param <T> 泛型
     * @return tList
     */
    private static <T> T post4Json2Obj (String url, Class<T> clazz, HttpEntity request, Map<String, String> resultMap, String dataFieldName) {
        JSONObject result = null;
        RestTemplate client = new RestTemplate();

        JSONObject body = client.postForEntity(url, request, JSONObject.class).getBody();
        assert body != null;
        if (CollectionUtils.isEmpty(resultMap) || StringUtils.isEmpty(dataFieldName)) {
            // 直接返回data
            result = body;
        } else {
            // 有 code、msg 等一系列值
            // 判断结果返回值
            Map<String, String> msgMap = handleResponseState(resultMap, body);
            if (!CollectionUtils.isEmpty(msgMap)) {
                // 返回值判断不成功
                log.error("接口返回错误：{}。\n接口返回完整信息：{}", msgMap, body);
                return null;
            }

            if (clazz.equals(JavaBean4String.class)) {
                // 返回值是String
                result = (JSONObject) JSON.toJSON(new JavaBean4String(body.getString(dataFieldName)));
            } else if (clazz.equals(JavaBean4Integer.class)) {
                // 返回值是Integer
                result = (JSONObject) JSON.toJSON(new JavaBean4Integer(body.getInteger(dataFieldName)));
            } else {
                result = body.getJSONObject(dataFieldName);
            }
        }

        if (Objects.isNull(result)) {
            return null;
        } else {
            return result.toJavaObject(clazz);
        }
    }


    /**
     * 请求方式： http-post
     * 传参方式： form表单 or url后面跟参数
     * 返回方式： json
     * 返回值：   list
     * 带参数请求示例： url: http://127.0.0.1:8080/queryTest
     *                paramMap: [{"args": "自定义值"}]
     *                resultMap: [{"code": "200"}]
     *
     * @param url url
     * @param clazz t.class
     * @param resultMap 返回是否成功的标志
     * @param dataFieldName 返回的 值字段名
     * @param <T> 泛型
     * @return tList
     */
    private static <T> List<T> post4Json2List (String url, Class<T> clazz, HttpEntity request, Map<String, String> resultMap, String dataFieldName) {
        RestTemplate client = new RestTemplate();

        if (CollectionUtils.isEmpty(resultMap) || StringUtils.isEmpty(dataFieldName)) {
            // 直接返回data
            JSONArray body = client.postForEntity(url, request, JSONArray.class).getBody();

            assert body != null;
            return body.toJavaList(clazz);
        } else {
            // 有 code、msg 等一系列值
            JSONObject body = client.postForEntity(url, request, JSONObject.class).getBody();
            assert body != null;
            JSONArray result = null;

            // 判断结果返回值
            Map<String, String> msgMap = handleResponseState(resultMap, body);
            if (!CollectionUtils.isEmpty(msgMap)) {
                // 返回值判断不成功
                log.error("接口返回错误：{}。\n接口返回完整信息：{}", msgMap, body);
                return null;
            }

            result = body.getJSONArray(dataFieldName);

            if (Objects.isNull(result)) {
                return null;
            } else {
                return result.toJavaList(clazz);
            }
        }
    }


    /**
     * 判断接口返回值
     *
     * @param resultMap 要求返回值的键值对 如：[{"code": "200"}]
     * @param body http接口返回的JSONObject。 如：判断 body 中， key=code时，value是否为200。如果不是，组装msg信息返回
     * @return msg 接口返回值失败匹配的详情。如：code, 要求返回200，实际返回500
     */
    private static Map<String, String> handleResponseState(Map<String, String> resultMap, JSONObject body) {
        Map<String, String> msgMap = new HashMap<>();
//        resultMap.forEach((key, value) -> {
//            String conditionValue = String.valueOf(body.get(key));
//            if (!value.equals(conditionValue)) {
//                msgMap.put(key, "返回的值为：" + conditionValue + "; 要求的值为：" + value);
//            }
//        });
        for (Map.Entry<String, String> entry : resultMap.entrySet()) {
            String conditionValue = String.valueOf(body.get(entry.getKey()));
            if (!entry.getValue().equals(conditionValue)) {
                msgMap.put(entry.getKey(), "返回的值为：" + conditionValue + "; 要求的值为：" + entry.getValue());
            }
        }

        return msgMap;
    }
}
