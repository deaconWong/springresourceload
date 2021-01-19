package deacon.test.spb.classload;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import deacon.test.spb.util.BeanRegistry;
import deacon.test.spb.util.CacheUtil;
import deacon.test.spb.util.SpringContextUtil;

/**
 * 将外部jar包中的requestmapping注册进spring容器中的工具类; 并实例化其中的引用对象及注入
 * 
 * @author deacon
 *
 */
public class AncheMappingRegulator {
    private Logger log = LoggerFactory.getLogger(AncheMappingRegulator.class);
    
    @Autowired
    private BeanRegistry registry;

    @Autowired
    private CacheUtil cacheUtil;

    /**
     * 添加requestmapping, 也就是controller的处理
     * 
     * @param classmap
     *            class集合
     * @param context
     *            spring容器上下文
     */
    public void addMappingService(Map<String, AncheClassLoader> classmap, ApplicationContext context) {
        if (classmap.size() > 0) {
            for (Entry<String, AncheClassLoader> entry : classmap.entrySet()) {
                try {
                    log.info("<<<<<<<<<<<<<<<<<<<<load jar :" + entry.getKey() + " mapping info >>>>>>>>>>>>>>>>>");
                    AncheClassLoader classLoader = entry.getValue();
                    List<Map<String, Class<?>>> mappedServiceList;
                    mappedServiceList = classLoader.loadJarMappingService();
                    for (Map<String, Class<?>> mappedService : mappedServiceList) {
                        classLoader.setMappingInfoList(this.addController(mappedService, context));
                    }
                    log.debug("addMappingService 添加接口映射完成");
                } catch (Exception e) {
                    log.error("添加接口映射时出错:{}", e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 处理serviceBean的加载
     * 
     * @param classmap
     */
    public void addMappingBean(Map<String, AncheClassLoader> classmap) {
        if (classmap.size() > 0) {
            for (Entry<String, AncheClassLoader> entry : classmap.entrySet()) {
                try {
                    log.info("<<<<<<<<<<<<<<<<<<<<load jar :" + entry.getKey() + " bean info >>>>>>>>>>>>>>>>>");
                    AncheClassLoader classLoader = entry.getValue();
                    List<Map<String, Class<?>>> mappedBeanList = classLoader.loadJarBeanService(cacheUtil);
                    for (Map<String, Class<?>> mappedBean : mappedBeanList) {
                        this.addBean(mappedBean);
                    }
                    log.debug("addMappingBean 添加serviceBean完成");
                } catch (Exception e) {
                    log.error("添加serviceBean时出错:{}", e.getMessage(), e);
                }
            }
        }
    }

    private void addBean(Map<String, Class<? extends Object>> classmap) {
        // 将bean注册到context下
        registry.register(classmap);
        for (Entry<String, Class<? extends Object>> entry : classmap.entrySet()) {
            try {
                log.debug("beanName:{}, hashcode:{}", entry.getKey(),
                    SpringContextUtil.getApplicationContext().getBean(entry.getKey()).hashCode());
            } catch (Exception e) {
                log.error("beanName:{}, error:{}", entry.getKey(), e.getMessage());
            }
        }

    }

    private List<RequestMappingInfo> addController(Map<String, Class<?>> classmap, ApplicationContext context) {
        List<RequestMappingInfo> mappingInfoList = new ArrayList<>();
        RequestMappingHandlerMapping requestMappingHandlerMapping = null;
        Method getMappingForMethod = null;

        String beanName = null;
        Method[] methodArr = null;
        DefaultListableBeanFactory defaultListableBeanFactory = null;
        Object newInstance = null;
        for (Entry<String, Class<?>> entry : classmap.entrySet()) {
            try {
                if (null != newInstance)
                    newInstance = null;
                // 默认beanname
                beanName = lowerFirst(entry.getValue().getSimpleName());

                defaultListableBeanFactory = (DefaultListableBeanFactory)SpringContextUtil.getApplicationContext()
                    .getAutowireCapableBeanFactory();
                defaultListableBeanFactory.setAllowBeanDefinitionOverriding(true);
                methodArr = entry.getValue().getDeclaredMethods();

                // 存在时先删除
                Assert.notNull(beanName, "beanName 不能为空");
                checkExistBeanDefinition(context, beanName, methodArr, defaultListableBeanFactory, entry);

                registry.registerBean(entry.getValue(), beanName);
                newInstance = SpringContextUtil.getApplicationContext().getBean(beanName);

                if (null != newInstance) {
                    requestMappingHandlerMapping = context.getBean(RequestMappingHandlerMapping.class);
                    getMappingForMethod = ReflectionUtils.findMethod(RequestMappingHandlerMapping.class,
                        "getMappingForMethod", Method.class, Class.class);
                    getMappingForMethod.setAccessible(true);
                    for (Method method : newInstance.getClass().getDeclaredMethods()) {
                        if (method.getAnnotation(RequestMapping.class) != null) {
                            RequestMappingInfo mappingInfo = (RequestMappingInfo)getMappingForMethod
                                .invoke(requestMappingHandlerMapping, method, newInstance.getClass());
                            // 重新注册
                            requestMappingHandlerMapping.registerMapping(mappingInfo, newInstance, method);
                            mappingInfoList.add(mappingInfo);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("处理动态添加接口映射时出错:{}", e.getMessage(), e);
            }
        }
        return mappingInfoList;
    }

    /**
     * 判断是否存在beandefinition, 如果而在就注销对应定义信息
     * 
     * @param context
     * @param beanName
     * @param methodArr
     * @param defaultListableBeanFactory
     * @param entry
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private void checkExistBeanDefinition(ApplicationContext context, String beanName, Method[] methodArr,
        DefaultListableBeanFactory defaultListableBeanFactory, Entry<String, Class<?>> entry)
        throws IllegalAccessException, InvocationTargetException {
        RequestMappingHandlerMapping requestMappingHandlerMapping;
        Method getMappingForMethod;
        if (defaultListableBeanFactory.containsBeanDefinition(beanName)) {// defaultListableBeanFactory.getBean(beanName)
            requestMappingHandlerMapping = context.getBean(RequestMappingHandlerMapping.class);
            getMappingForMethod = ReflectionUtils.findMethod(RequestMappingHandlerMapping.class, "getMappingForMethod",
                Method.class, Class.class);
            getMappingForMethod.setAccessible(true);

            for (Method method : methodArr) {
                if (method.getAnnotation(RequestMapping.class) != null) {
                    RequestMappingInfo mappingInfo = (RequestMappingInfo)getMappingForMethod
                        .invoke(requestMappingHandlerMapping, method, entry.getValue());
                    // 注销旧的映射
                    requestMappingHandlerMapping.unregisterMapping(mappingInfo);
                }
            }
            defaultListableBeanFactory.removeBeanDefinition(beanName);
        }
    }

    /**
     * 删除所有注册的requestmapping
     * 
     * @param classmap
     * @param context
     */
    public void removeAllMappingService(Map<String, AncheClassLoader> classmap, ApplicationContext context) {
        AncheClassLoader ancheClassLoader = null;
        if (classmap.size() > 0) {
            for (Entry<String, AncheClassLoader> entry : classmap.entrySet()) {
                try {
                    ancheClassLoader = entry.getValue();
                    if (ancheClassLoader.getMappingInfoList() != null
                        && !ancheClassLoader.getMappingInfoList().isEmpty()) {
                        this.removeMapping(ancheClassLoader.getMappingInfoList(), context);
                    }
                } catch (Exception e) {
                    log.error("删除接口映射时出错:{}", e.getMessage(), e);
                }

            }
        }
    }

    private void removeMapping(List<? extends Object> list, ApplicationContext context) {
        RequestMappingHandlerMapping requestMappingHandlerMapping = context.getBean(RequestMappingHandlerMapping.class);
        for (Object o : list) {
            RequestMappingInfo info = (RequestMappingInfo)o;
            log.info("remove mapping :" + info.toString());
            requestMappingHandlerMapping.unregisterMapping(info);
        }

    }

    private String lowerFirst(CharSequence str) {
        if (null == str) {
            return null;
        }
        if (str.length() > 0) {
            char firstChar = str.charAt(0);
            if (Character.isUpperCase(firstChar)) {
                return Character.toLowerCase(firstChar) + subSuf(str, 1);
            }
        }
        return str.toString();
    }

    private boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    private String subSuf(CharSequence string, int fromIndex) {
        if (isEmpty(string)) {
            return null;
        }
        return sub(string, fromIndex, string.length());
    }

    private String sub(CharSequence str, int fromIndex, int toIndex) {
        if (isEmpty(str)) {
            return null == str ? null : str.toString();
        }
        int len = str.length();

        if (fromIndex < 0) {
            fromIndex = len + fromIndex;
            if (fromIndex < 0) {
                fromIndex = 0;
            }
        } else if (fromIndex > len) {
            fromIndex = len;
        }

        if (toIndex < 0) {
            toIndex = len + toIndex;
            if (toIndex < 0) {
                toIndex = len;
            }
        } else if (toIndex > len) {
            toIndex = len;
        }

        if (toIndex < fromIndex) {
            int tmp = fromIndex;
            fromIndex = toIndex;
            toIndex = tmp;
        }

        if (fromIndex == toIndex) {
            return "";
        }

        return str.toString().substring(fromIndex, toIndex);
    }
}
