package deacon.test.spb.util;

import static org.springframework.beans.factory.support.AbstractBeanDefinition.AUTOWIRE_BY_TYPE;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * bean 注入
 */
public class ManualRegistBeanUtil {
    static String SCOPE_SINGLETON = "singleton";

    /**
     * Scope identifier for the standard prototype scope: {@value}.
     * <p>
     * Custom scopes can be added via {@code registerScope}.
     * 
     * @see #registerScope
     */
    static String SCOPE_PROTOTYPE = "prototype";

    /**
     * 主动向Spring容器中注册bean
     *
     * @param applicationContext
     *            Spring容器
     * @param name
     *            BeanName
     * @param clazz
     *            注册的bean的类性
     * @param args
     *            构造方法的必要参数，顺序和类型要求和clazz中定义的一致
     * @param <T>
     * @return 返回注册到容器中的bean对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T registerBean(ConfigurableApplicationContext applicationContext, String name, Class<T> clazz,
        Object[] args) {

        if (applicationContext.containsBean(name)) {
            Object bean = applicationContext.getBean(name);
            if (bean.getClass().isAssignableFrom(clazz)) {
                return (T)bean;
            } else {
                throw new RuntimeException("BeanName 重复 " + name);
            }
        }
        BeanDefinitionRegistry beanFactory = (BeanDefinitionRegistry)applicationContext.getBeanFactory();
        // Class<?>[] beanClazzs = clazz.getInterfaces();//反射获取需要代理的接口的clazz列表
        // for (Class beanClazz : beanClazzs) {
        // BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        // GenericBeanDefinition definition = (GenericBeanDefinition) builder.getRawBeanDefinition();
        // definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_NO);
        // beanFactory.registerBeanDefinition(name, definition);
        // }

        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        for (Object arg : args) {
            beanDefinitionBuilder.addConstructorArgValue(arg);
        }

        GenericBeanDefinition beanDefinition = (GenericBeanDefinition)beanDefinitionBuilder.getRawBeanDefinition();
        beanDefinition.setAutowireMode(AUTOWIRE_BY_TYPE);
        beanFactory.registerBeanDefinition(name, beanDefinition);
        return applicationContext.getBean(name, clazz);
    }
}
