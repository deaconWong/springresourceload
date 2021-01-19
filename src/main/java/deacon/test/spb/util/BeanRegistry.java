package deacon.test.spb.util;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.AnnotationScopeMetadataResolver;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.ScopeMetadata;
import org.springframework.context.annotation.ScopeMetadataResolver;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class BeanRegistry {
    
    private Logger log = LoggerFactory.getLogger(BeanRegistry.class);

    private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

    private BeanNameGenerator beanNameGenerator = AnnotationBeanNameGenerator.INSTANCE;

    private BeanDefinitionRegistry registry;
    
//    @Autowired AnCheDefaultListableBeanFactory anCheDefaultListableBeanFactory;

    public void register(Map<String, Class<? extends Object>> componentClasses) {
        if(null == registry) {
//            registry = anCheDefaultListableBeanFactory;
            registry =  (BeanDefinitionRegistry)(((ConfigurableApplicationContext)SpringContextUtil.getApplicationContext())
                    .getBeanFactory());
        }
        for (Entry<String, Class<? extends Object>> entry : componentClasses.entrySet()) {
            registerBean(entry.getValue(), entry.getKey());
            
        }
//        anCheDefaultListableBeanFactory.preInstantiateSingletons();
    }

    /**
     * Register a bean from the given bean class, deriving its metadata from class-declared annotations.
     * 
     * @param beanClass
     *            the class of the bean
     */
    public void registerBean(Class<?> beanClass, String beanName) {
        doRegisterBean(beanClass, beanName, null, null, null);
    }

    private <T> void doRegisterBean(Class<T> beanClass, @Nullable String name,
        @Nullable Class<? extends Annotation>[] qualifiers, @Nullable Supplier<T> supplier,
        @Nullable BeanDefinitionCustomizer[] customizers) {

        AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(beanClass);
        abd.setInstanceSupplier(supplier);
        ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
        abd.setScope(scopeMetadata.getScopeName());
        String beanName = (name != null ? name : this.beanNameGenerator.generateBeanName(abd, this.registry));

        AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);
        if (qualifiers != null) {
            for (Class<? extends Annotation> qualifier : qualifiers) {
                if (Primary.class == qualifier) {
                    abd.setPrimary(true);
                } else if (Lazy.class == qualifier) {
                    abd.setLazyInit(true);
                } else {
                    abd.addQualifier(new AutowireCandidateQualifier(qualifier));
                }
            }
        }
        if (customizers != null) {
            for (BeanDefinitionCustomizer customizer : customizers) {
                customizer.customize(abd);
            }
        }

        BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
        definitionHolder = applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
        BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, this.registry);
    }

    BeanDefinitionHolder applyScopedProxyMode(ScopeMetadata metadata, BeanDefinitionHolder definition,
        BeanDefinitionRegistry registry) {

        ScopedProxyMode scopedProxyMode = metadata.getScopedProxyMode();
        if (scopedProxyMode.equals(ScopedProxyMode.NO)) {
            return definition;
        }
        boolean proxyTargetClass = scopedProxyMode.equals(ScopedProxyMode.TARGET_CLASS);
        return ScopedProxyCreator.createScopedProxy(definition, registry, proxyTargetClass);
    }

    /**
     * 向Spring容器中注入bean(构造器注入)
     *
     * @param beanName
     *            bean名称
     * @param beanClass
     *            bean Class对象
     * @param constructorArgs
     *            参数
     * @param <T>
     */
    public <T> boolean registerBean(String beanName, final Class<T> beanClass, Object... constructorArgs) {
        boolean result = false;
        if (null == beanClass) {
            return result;
        }
        Map<String, Class<?>> controllerMap = (Map<String, Class<?>>)constructorArgs[0];

        Map<String, Object> failOverMap = null;
        // 构建BeanDefinitionBuilder
        try {
            // 获取spring容器中的IOC容器
            DefaultListableBeanFactory defaultListableBeanFactory =
                (DefaultListableBeanFactory)SpringContextUtil.getApplicationContext().getAutowireCapableBeanFactory();

            // 向IOC容器中注入bean对象
            // beanName = StrUtils.lowerFirst(beanName);
//            defaultListableBeanFactory.getBean(IEncodeCacheService.class);
            boolean containsBean = defaultListableBeanFactory.containsBean(beanName);
            List<String> dependentBeansList = null;
            // defaultListableBeanFactory.getBean("iCheckItemsAJService")
            // defaultListableBeanFactory.getBean(IEncodeCacheService.class)
            // defaultListableBeanFactory.getBean("systemService")
            int step = 1;
            if (containsBean) {
                log.debug("{} 已经加载到VM中的bean:{}", step++, defaultListableBeanFactory.getBean(beanName).toString());
                // Return the name of all beans which depend on the specified bean
                dependentBeansList = Arrays.asList(defaultListableBeanFactory.getDependentBeans(beanName));

                log.debug("{} 完成bean:{}的依赖项查找共:{}项", step++, defaultListableBeanFactory.getBean(beanName).toString(),
                    dependentBeansList.size());
                // 移除bean的定义和实例
                defaultListableBeanFactory.removeBeanDefinition(beanName);
                log.debug("{} 删除bean:{}", step++, beanName);
            }

            // 注册并装配对应属性
            defaultListableBeanFactory.autowireBeanProperties(ManualRegistBeanUtil.registerBean(
                (ConfigurableApplicationContext)SpringContextUtil.getApplicationContext(), beanName, beanClass,
                new Object[] {}), 2, true);
            log.debug("{} 加载并装配后的bean:{}", step, defaultListableBeanFactory.getBean(beanName).toString());
            if (null != dependentBeansList && !dependentBeansList.isEmpty()) {
                String componentName = beanClass.getCanonicalName();
                controllerMap.putAll(refresh(dependentBeansList, defaultListableBeanFactory, componentName));
            }

            result = true;
        } catch (BeanDefinitionStoreException e) {
            log.error("BeanDefinitionStoreException:{}", e.getMessage(), e);
        } catch (NoSuchBeanDefinitionException e) {
            log.error("NoSuchBeanDefinitionException:{}", e.getMessage(), e);
        } catch (BeansException e) {
            // SpringContextUtil.getBean()
            log.error("BeansException:{}", e.getMessage(), e);
        } catch (IllegalStateException e) {
            log.error("IllegalStateException:{}", e.getMessage(), e);
        }
        return result;
    }

    public Map<String, Class<? extends Object>> sortBeanMap(Map<String, Class<? extends Object>> beanClassMap) {

        // 对beanClassMap中的class根据依赖项的多少进行倒序排序
        Map<String, Class<? extends Object>> sortedMap = new LinkedHashMap<>();

        // 获取spring容器中的IOC容器

        DefaultListableBeanFactory defaultListableBeanFactory =
            (DefaultListableBeanFactory)SpringContextUtil.getApplicationContext().getAutowireCapableBeanFactory();

        beanClassMap.entrySet().stream()
            .sorted(Map.Entry.<String, Class<? extends Object>>comparingByKey((key1, key2) -> {
                String beanName = StrUtils.lowerFirst(key1);
                boolean containsBean = defaultListableBeanFactory.containsBean(beanName);
                int dependentOnKey1Cnt = 0;
                if (containsBean) {
                    dependentOnKey1Cnt = beanName.startsWith("indexModule") ? Integer.MAX_VALUE :
                    // Arrays.asList(defaultListableBeanFactory.getDependentBeans(beanName)).size();
                    Arrays.asList(defaultListableBeanFactory.getDependentBeans(beanName)).size();
                    log.debug("beanName :{} 的依赖项有:{}", beanName, dependentOnKey1Cnt);
                }

                beanName = StrUtils.lowerFirst(key2);
                containsBean = defaultListableBeanFactory.containsBean(beanName);
                int dependentOnKey2Cnt = 0;
                if (containsBean) {
                    dependentOnKey2Cnt = beanName.startsWith("indexModule") ? Integer.MAX_VALUE
                        : Arrays.asList(defaultListableBeanFactory.getDependentBeans(beanName)).size();
                    log.debug("beanName :{} 的依赖项有:{}", beanName, dependentOnKey2Cnt);
                }
                return Integer.valueOf(dependentOnKey1Cnt).compareTo(Integer.valueOf(dependentOnKey2Cnt));
            }).reversed()).forEachOrdered(e -> sortedMap.put(StrUtils.lowerFirst(e.getKey()), e.getValue()));

        return sortedMap;
    }

    private Map<String, Class<?>> refresh(List<String> dependentBeansList,
        DefaultListableBeanFactory defaultListableBeanFactory, String componentName) {
        BeanDefinition existBean = null;
        Map<String, Class<?>> m = new ConcurrentHashMap<>();
        String beanSimpleName = null;
        try {
            for (String beanName : dependentBeansList) {
                if (beanName.indexOf('.') < 0 && defaultListableBeanFactory.containsBean(beanName)) {
                    existBean = defaultListableBeanFactory.getBeanDefinition(beanName);
                    // 需要更新的cotroller和service, 相同组件下的不处理
                    if (!isSameComponentType(componentName, existBean.getBeanClassName())) {
                        beanSimpleName = existBean.getClass().getSimpleName();
                        m.put(beanSimpleName, existBean.getClass());
                    }
                }
            }
        } catch (Exception e) {
            log.error("装配已加载的bean:{},时出错:{}", beanSimpleName, e.getMessage(), e);
        }
        return m;
    }

    private static final String PACKAGE_PREFIX = "com.anche.jiance.";

    /**
     * 判断是否是同一组件下的类, 相同时不需要额外处理
     * 
     * @param componentName
     * @param klassFullName
     * @return
     */
    private boolean isSameComponentType(String componentName, String klassFullName) {
        boolean res = false;
        String componentSimpleName = componentName.replace(PACKAGE_PREFIX, "");
        componentSimpleName = componentSimpleName.substring(0, componentSimpleName.indexOf('.'));
        String klassSimpleName = klassFullName.replace(PACKAGE_PREFIX, "");
        klassSimpleName = klassSimpleName.substring(0, klassSimpleName.indexOf('.'));
        if (klassSimpleName.equals(componentSimpleName)) {
            res = true;
        }
        return res;
    }

}
