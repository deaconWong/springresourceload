package deacon.test.spb.classload;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import deacon.test.spb.enums.ComponentEnums;
import deacon.test.spb.util.CacheUtil;
import deacon.test.spb.util.StrUtils;

/**
 * 插件类加载器，在插件目录中搜索jar包，并为发现的资源(jar)构造一个类加载器,将对应的jar添加到classpath中
 * 
 * @author deacon
 *
 */
public class AncheClassLoader extends URLClassLoader {
    private Logger log = LoggerFactory.getLogger(AncheClassLoader.class);
    
    /**
     *
     */
    private static final String CLASS_SUFFIX = ".class";

    private List<JarURLConnection> cachedJarFiles = new ArrayList<>();

//    private List<String> serviceList = new ArrayList<>();

//    private List<String> beanList = new ArrayList<>();

    private List<RequestMappingInfo> mappingInfoList;

    public AncheClassLoader(URL[] fileUrlArr) {
        super(new URL[] {}, findParentClassLoader());
        for (URL fileUrl : fileUrlArr) {
            this.addURLFile(fileUrl);
        }
    }

    /**
     * 将指定的文件url添加到类加载器的classpath中去，并缓存jar connection，方便以后卸载jar
     * 
     * @param 一个可向类加载器的classpath中添加的文件url
     */
    public void addURLFile(URL fileURL) {
        try {
            // 打开并缓存文件url连接
            URLConnection uc = fileURL.openConnection();
            if (uc instanceof JarURLConnection) {
                uc.setUseCaches(false);
                cachedJarFiles.add((JarURLConnection)uc);
            }
        } catch (Exception e) {
            log.error("Failed to cache plugin JAR file: " + fileURL.toExternalForm(), e);
        }
        addURL(fileURL);
    }

    /**
     * 卸载jar包
     * 
     * @param urlConnection
     */
    public void unloadJarFiles() {
        try {
            for (JarURLConnection url : cachedJarFiles) {
                log.warn("Unloading plugin JAR file " + url.getJarFile().getName());
                url.getJarFile().close();
            }
        } catch (Exception e) {
            log.error("Failed to unload JAR file", e);
        }
    }

    /**
     * 定位基于当前上下文的父类加载器
     * 
     * @return 返回可用的父类加载器.
     */
    private static ClassLoader findParentClassLoader() {
        return ClassLoader.getSystemClassLoader();
    }

    public List<Map<String, Class<?>>> loadJarMappingService() {
        Map<String, Class<?>> classes = new ConcurrentHashMap<>();
        File file = null;
        URI fileUri = null;
        List<Map<String, Class<?>>> mappedCalssList = new LinkedList<>();
        try {
            for (JarURLConnection url : cachedJarFiles) {
                fileUri = url.getJarFileURL().toURI();
                file = new File(url.getJarFileURL().toURI());
                handleMappingLoad(classes, file);
            }
            mappedCalssList.add(classes);
        } catch (URISyntaxException e) {
            log.error("url:{}对应的路径下的文件不存在!", fileUri, e);
        }
        return mappedCalssList;
    }

    /**
     * @param m
     * @param file
     */
    protected void handleMappingLoad(Map<String, Class<?>> classes, File file) {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                if (!ze.isDirectory() && ze.getName().endsWith(CLASS_SUFFIX)) {
                    String classname =
                        ze.getName().replace("BOOT-INF/classes", "").replace("/", ".").replace(CLASS_SUFFIX, "");
                    Class<?> c = Class.forName(classname, false, this);
                    if (c.getAnnotation(RestController.class) != null || c.getAnnotation(Controller.class) != null) {
                        classes.put(c.getSimpleName(), c);
//                        serviceList.add(c.getCanonicalName().toLowerCase());
                        log.debug("load class to map:" + classname + " key:" + c.getSimpleName().toLowerCase());
                    }
                }
            }
        } catch (Exception e) {
            log.error("处理接口映射时出错:{}", e.getMessage(), e);
        }
    }

    /**
     * 处理servicebean的加载, 每个jar单独处理
     * 
     * @param cacheUtil
     * 
     * @return
     */
    public List<Map<String, Class<?>>> loadJarBeanService(CacheUtil cacheUtil) {
        Map<String, Class<?>> classes = new ConcurrentHashMap<>();;
        File file = null;
        URI fileUri = null;
        List<Map<String, Class<?>>> mappedCalssList = new LinkedList<>();
        try {
            for (JarURLConnection url : cachedJarFiles) {
                fileUri = url.getJarFileURL().toURI();
                file = new File(url.getJarFileURL().toURI());
                handleServiceLoad(classes, file, cacheUtil);
            }
        } catch (URISyntaxException e) {
            log.error("url:{}对应的路径下的文件不存在!", fileUri, e);
        }
        mappedCalssList.add(classes);
        return mappedCalssList;
    }

    /**
     * @param classes
     * @param file
     * @param cacheUtil
     * @throws IOException
     */
    private void handleServiceLoad(Map<String, Class<?>> classes, File file, CacheUtil cacheUtil) {

        try (JarFile jarFile = new JarFile(file);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                if (!ze.isDirectory() && ze.getName().endsWith(CLASS_SUFFIX)) {
                    String classname =
                        ze.getName().replace("BOOT-INF/classes", "").replace("/", ".").replace(CLASS_SUFFIX, "");
                    log.debug("classname:{}", classname);
                    processBeanInfo(classes, classname, cacheUtil);
                }
            }
        } catch (Exception e) {
            log.error("error", e);
        }
    }

    /**
     * 只处理@Component @Service
     * 
     * @param classes
     * @param classname
     * @param cacheUtil
     */
    protected void processBeanInfo(Map<String, Class<?>> classes, String classname, CacheUtil cacheUtil) {
        String beanName;
        boolean isInit = true;
        try {
            Class<?> c = Class.forName(classname, true, this);

            if (c.getAnnotation(Service.class) != null || c.getAnnotation(Component.class) != null) {
                beanName = c.getAnnotation(Service.class) == null ? c.getAnnotation(Component.class).value()
                    : c.getAnnotation(Service.class).value();

                ConditionalOnProperty conditionalOnProperty = c.getAnnotation(ConditionalOnProperty.class);
                if (null != conditionalOnProperty) {
                    String key = conditionalOnProperty.prefix() + "." + String.valueOf(conditionalOnProperty.name()[0]);
                    String propertyName = conditionalOnProperty.havingValue();
                    // 需要根据key从缓存中获取配置信息, 测试时写死为default
                    /*if (!propertyName.equals(cacheUtil.getCache(key))) {*/
                    if (!propertyName.equals("default")) {
                        isInit = false;
                    }
                }

                if (isInit) {
                    if (c.getInterfaces().length > 0) {
                        for (Class<?> i : c.getInterfaces()) {
                            classes.put(!"".equals(beanName) ? beanName : StrUtils.lowerFirst(i.getCanonicalName()), c);
                            log.info("开始加载key:{}对应的服务对象:{}", !"".equals(beanName) ? beanName : i.getName(), classname);
                        }
                    } else {
                        classes.put(!"".equals(beanName) ? beanName : StrUtils.lowerFirst(c.getSimpleName()), c);
                    }
//                    beanList.add(c.getCanonicalName().toLowerCase());
                }
            }
        } catch (ClassNotFoundException e) {
            log.error("处理serviceBeanName是出错:{}", e.getMessage(), e);
        }
    }

    // private String lowerFirst(String str){
    // char [] chars = str.toCharArray();
    // chars[0] += 32;
    // return String.valueOf(chars);
    // }

    public List<? extends Object> getMappingInfoList() {
        return mappingInfoList;
    }

    public void setMappingInfoList(List<RequestMappingInfo> mappingInfoList) {
        this.mappingInfoList = mappingInfoList;
    }

    public List<JarURLConnection> getCachedJarFiles() {
        return cachedJarFiles;
    }

    public void setCachedJarFiles(List<JarURLConnection> cachedJarFiles) {
        this.cachedJarFiles = cachedJarFiles;
    }

    protected boolean filter(String name, boolean isClassName) {
        if (name.toLowerCase().contains("vehiclelogin")) {
            return false;
        }
        return isClassName;
    }

    private byte[] loadClassData(String klassName) {
        String klassShortName = klassName.replace("com.anche.jiance.", "");
        String componentName = klassShortName.substring(0, klassShortName.indexOf('.'));
        if (klassName.startsWith("com.anche.jiance." + componentName))
            try (
                JarFile jarFile = new JarFile(
                    "f:/loader/" + ComponentEnums.valueOf(componentName.toUpperCase()).getValue() + "-2.0.jar");
                InputStream is =
                    jarFile.getInputStream(jarFile.getJarEntry(klassName.replace(".", "/") + CLASS_SUFFIX));) {
                if (null != is) {
                    byte[] by = new byte[is.available()];
                    int count = 0;
                    while ((count = is.read(by)) > 0) {
                        log.debug("从jar:{}读取了class:{}共:{}字节", jarFile.getName(), klassName, count);
                    }
                    return by;
                }
            } catch (Exception e) {
                log.error("读取clss:{} 文件流失败:{}", klassName, e.getMessage(), e);
            }
        return new byte[0];
    }

//    @Override
//    public Class<?> loadClass(String name) throws ClassNotFoundException {
//        Class<?> klass;
//        try {
//            byte[] loadJarData = loadClassData(name);
//            if (loadJarData.length == 0) {
//                return super.loadClass(name);
//            } else {
//                klass = defineClass(name, loadJarData, 0, loadJarData.length);
//                // resolveClass(klass);
//                return klass;
//            }
//        } catch (Exception e) {
//            log.error("ssss");
//        }
//        return null;
//    }

}