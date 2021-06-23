package deacon.test.spb.std;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @description 全局处理响应数据--响应增强类
 * 接口返回统一响应体 + 异常也返回统一响应体，
 * 其实这样已经很好了，但还是有可以优化的地方。
 *
 * 先创建一个类加上注解使其成为全局处理类。
 * 然后继承ResponseBodyAdvice接口重写其中的方法，
 * 即可对我们的controller进行增强操作
 */

// 可以改为@ControllerAdvice注解来拦截所有Controller的处理结果
@RestControllerAdvice(basePackages = {"cn.anche.service.eoms.api"}) // 注意哦，这里要加上需要扫描的包
public class ResponseControllerAdvice implements ResponseBodyAdvice<Object> {

    // 1.关于哪些请求要执行beforeBodyWrite，返回true执行，返回false不执行
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> aClass) {
        // 如果接口返回的类型本身就是ResultVO那就没有必要进行额外的操作，返回false
 		System.out.println("=================="+!returnType.getParameterType().equals(ResultVO.class));
        return !returnType.getParameterType().equals(ResultVO.class);
    }

    // 2.如果接口返回的类型本身不是ResultVO，那就将原本的数据包装在ResultVO里再返回
    @Override
    public Object beforeBodyWrite(Object data, MethodParameter returnType, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest request, ServerHttpResponse response) {
        // String类型不能直接包装，所以要进行些特别的处理
        if (returnType.getParameterType().equals(String.class)) {
            System.out.println(data instanceof String);
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                System.out.println("===========进入了包装数据======");
                // 将String转换，再将数据包装在ResultVO里，再转换为json字符串响应给前端
                return objectMapper.writeValueAsString(new ResultVO<>(data));
            } catch (JsonProcessingException e) {
                throw new APIException("返回String类型错误");
            }
        }
        // 将原本的数据包装在ResultVO里
        return new ResultVO<>(data);
    }
}
