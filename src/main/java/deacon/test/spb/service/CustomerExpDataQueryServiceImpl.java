 package deacon.test.spb.service;

import java.util.List;
import java.util.Map;

import org.springframework.aop.framework.AopContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class CustomerExpDataQueryServiceImpl implements ICustomerExpDataQueryService {

    @Override
    public List<Map<String, Object>> queryCustomerExpInfoData(Map<String, Object> parasMap) {
        //注解只有通过接口调用时才起作用
        CustomerExpDataQueryServiceImpl currentProxy = (CustomerExpDataQueryServiceImpl)AopContext.currentProxy();
        return null;
    }

}
