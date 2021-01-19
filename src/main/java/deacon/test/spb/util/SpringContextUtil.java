package deacon.test.spb.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class SpringContextUtil implements ApplicationContextAware {
    private static ApplicationContext applicationContext;// spring上下文

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }

    /**
     * 获取指定的bean对象
     *
     * @param name
     *            bean名称
     * @param clazz
     *            类class对象
     * @param <T>
     * @return Bean对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name, Class<T> clazz) {
        // applicationContext非空
        Assert.isTrue((applicationContext != null), "应用上下文不能为空");
        Object bean = applicationContext.getBean(name);
        if (bean == null) {
            return null;
        }
        return (T)bean;
    }

    /**
     * 获取bean对象实例
     *
     * @param clazz
     *            类class对象
     * @param <T>
     * @return Bean对象
     */
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    /**
     * 获取bean对象实例
     *
     * @param name
     *            实例名称
     * @return
     */
    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    /**
     * 获取 applicationContext 的值
     *
     * @return applicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static String getActiveProfile() {
        return applicationContext.getEnvironment().getActiveProfiles()[0];
    }
}
