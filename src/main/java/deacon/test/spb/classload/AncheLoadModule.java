package deacon.test.spb.classload;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AncheLoadModule {
    private Logger log = LoggerFactory.getLogger(AncheLoadModule.class);
    
    
    private static final Map<String, AncheClassLoader> LOADMAP = new ConcurrentHashMap<>();
    private static final Map<String, Long> MODIFIED_TIME = new HashMap<>();
    private String jarUrl;
    
    public boolean loadJarFile() {
        if (jarUrl != null) {
            return !loadJarFile(jarUrl).isEmpty();
        }
        log.error("loadjarfile 配置文件中对应文件加载路径为空");
        return false;
    }

    public List<URL> loadJarFile(String url) {
        List<URL> rulList = getJarFile(url);
        if (!rulList.isEmpty()) {
            URL[] urlArr = new URL[rulList.size()];
            urlArr = rulList.toArray(urlArr);
            if (LOADMAP.size() > 0) {
                removeJarAll();
            }
            LOADMAP.put(url, new AncheClassLoader(urlArr));
        }
        log.debug("loadJarFile(String url) 配置文件中对应文件加载路径:{}中的组件jar加载完成...", url);
        return rulList;
    }

    public void removeJarAll() {
        try {
            for (Entry<String, AncheClassLoader> m : LOADMAP.entrySet()) {
                m.getValue().unloadJarFiles();
                m.getValue().close();
            }
        } catch (IOException e) {
            log.error("关闭classloader时出现系统错误:{}", e.getMessage(), e);
        }
        log.debug("已加载完成的jar集合中jar剩余:" + LOADMAP.size());
    }

    /**
     * 处理对应url下的jar文件的加载逻辑, 形成文件url格式后返回
     * 
     * @param url
     * @return
     */
    private List<URL> getJarFile(String url) {
        log.debug("从指定的路径:{}加载对应业务组件jar包", url);
        List<URL> urlList = new ArrayList<>();
        File file = new File(url);

        log.debug("运行环境：" + System.getProperty("os.name"));
        if (file.exists() && file.isDirectory()) {
            // 只加载jar
            handleFileUrlPatter(url, urlList, file);
        } else {
            log.error("url exception:" + url);
        }
        // 要排序, models/proxy两个要先加载
        List<URL> finalList = new LinkedList<>();
        for (URL urlTerm : urlList) {
            if (urlTerm.getPath().indexOf("models") != -1) {
                finalList.add(urlTerm);
                break;
            }
        }
        for (URL urlTerm : urlList) {
            if (urlTerm.getPath().indexOf("proxy") != -1) {
                finalList.add(urlTerm);
                break;
            }
        }
        for (URL urlTerm : urlList) {
            if (!urlTerm.getPath().contains("models") && !urlTerm.getPath().contains("proxy")) {
                finalList.add(urlTerm);
            }
        }
        return finalList;
    }

    /**
     * 根据文件Md5值来判断是否对jar进行加载
     * 
     * @param url
     * @param urlList
     * @param file
     */
    private void handleFileUrlPatter(String url, List<URL> urlList, File file) {
        File[] fileArr = file.listFiles(f -> f.isFile() && f.getName().endsWith(".jar"));
        String urlString = null;
        int index = 1;
//        Long fileMd5 = null;
        try {
            for (File f : fileArr) {
//                fileMd5 = f.lastModified();
                // // 检验文件的MD5值, 如果不相同, 则重新加载
//                if (fileMd5.compareTo(MODIFIED_TIME.getOrDefault(f.getCanonicalPath(), 0L)) > 0) {
                    if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
                        urlString = "jar:file:/" + url + "/" + f.getName() + "!/";
                    } else {
                        urlString = "jar:file:" + url + "/" + f.getName() + "!/";
                    }
                    urlList.add(new URL(urlString));
//                    MODIFIED_TIME.put(f.getCanonicalPath(), fileMd5);
                    log.info("匹配到需要加载处理的文件{}路径:{}", index++, urlString);
//                } else {
//                    // 文件未发现改变时, 就要删除cache中的文件, 避免再次加载
//                    JarURLConnection jarUrlConn = null;
//                    for (Iterator<JarURLConnection> iter = LOADMAP.get(url).getCachedJarFiles().iterator();
//                        iter.hasNext();) {
//                        jarUrlConn = iter.next();
//                        if (jarUrlConn.getJarFileURL().getFile().indexOf(f.getName()) != -1) {
//                            jarUrlConn.getJarFile().close();
//                            iter.remove();
//                            break;
//                        }
//                    }
//                }
            }

        } catch (Exception e) {
            log.error("文件加载路径出错:{}error url:{}", e.getMessage(), urlString, e);
        } finally {

        }
    }

    public Map<String, AncheClassLoader> getClassLoaderMap() {
        return LOADMAP;
    }

    public String getJarUrl() {
        return jarUrl;
    }

    public void setJarUrl(String jarUrl) {
        this.jarUrl = jarUrl;
    }

}
