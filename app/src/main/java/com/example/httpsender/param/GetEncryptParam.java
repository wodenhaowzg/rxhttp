package com.example.httpsender.param;

import okhttp3.HttpUrl;
import rxhttp.wrapper.annotation.Param;
import rxhttp.wrapper.entity.KeyValuePair;
import rxhttp.wrapper.param.Method;
import rxhttp.wrapper.param.NoBodyParam;

/**
 * 加密get请求
 * User: ljx
 * Date: 2019-09-12
 * Time: 17:25
 */
@Param(methodName = "getEncrypt")
public class GetEncryptParam extends NoBodyParam {

    public GetEncryptParam(String url) {
        super(url, Method.GET);
    }

    public void test() {

    }

    @Override
    public HttpUrl getHttpUrl() {
        StringBuilder paramsBuilder = new StringBuilder(); //存储加密后的参数
        for (KeyValuePair pair : getKeyValuePairs()) {
            //这里遍历所有添加的参数，可对参数进行加密操作
            String key = pair.getKey();
            String value = pair.getValue().toString();
            //加密逻辑自己写

        }
        String simpleUrl = getSimpleUrl();  //拿到请求Url
        if (paramsBuilder.length() == 0) return HttpUrl.get(simpleUrl);
        return HttpUrl.get(simpleUrl + "?" + paramsBuilder);  //将加密后的参数和url组拼成HttpUrl对象并返回
    }
}
