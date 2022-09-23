package rxhttp.wrapper.exception;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Response;
import okhttp3.ResponseBody;
import rxhttp.wrapper.OkHttpCompat;

/**
 * 异常处理帮助类
 * User: ljx
 * Date: 2018/11/21
 * Time: 09:30
 */
public class ExceptionHelper {

    /**
     * 根据Http执行结果过滤异常
     *
     * @param response Http响应体
     * @return ResponseBody
     * @throws IOException 请求失败异常、网络不可用异常
     */
    @NotNull
    public static ResponseBody throwIfFatal(Response response) throws IOException {
        ResponseBody rawBody = response.body();
        if (!response.isSuccessful()) {
            try {
                ResponseBody bufferBody = OkHttpCompat.buffer(rawBody);
                response = response.newBuilder().body(bufferBody).build();
                throw new HttpStatusCodeException(response);
            } finally {
                rawBody.close();
            }
        }
        return rawBody;
    }
}
