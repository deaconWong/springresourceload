package deacon.test.spb.classload;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import deacon.test.spb.util.CacheUtil;

@Service
public class DynamicLoadJarTask implements ApplicationListener<ContextRefreshedEvent> {
    private Logger log = LoggerFactory.getLogger(AncheClassLoader.class);
    
    private AncheLoadModule ancheLoadJar;

    private ApplicationContext context;

    @Autowired
    private CacheUtil cacheUtil;
    static final AtomicBoolean LOADING = new AtomicBoolean(false);

    @Value("${jar.load.interval:60}")
    // 任务间隔时间最好是从缓存中去获取
    private final int PERIOD = 60;

    private static final ScheduledThreadPoolExecutor executor;

    static {
        executor = new ScheduledThreadPoolExecutor(1,
            new ThreadFactoryBuilder().setNameFormat("Anche-loader").setDaemon(true).build());
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        //this.context = event.getApplicationContext();
        // 初始化载入需要被热加载的外部jar包
        //this.ancheLoadJar = this.context.getBean(AncheLoadModule.class);
        //this.loadjar();
//        if (!executor.isTerminated() && !executor.isTerminating() && !executor.isShutdown()) {
//            executor.scheduleAtFixedRate(this::loadjar, PERIOD, PERIOD, TimeUnit.SECONDS);
//        }

    }

    private void loadjar() {
        if (!LOADING.get()) {
            try {
                LOADING.set(true);
                //ExtClasspathLoader.loadClasspath();

                if (this.ancheLoadJar.loadJarFile()) {
                    handleMapping();
                }
            } catch (Exception e) {
            	log.debug("加载jar包异常：",e);
                executor.shutdownNow();
            } finally {
                LOADING.set(false);
            }
        }
    }

    private void handleMapping() {
        AncheMappingRegulator ancheMappingRegulator = this.context.getBean(AncheMappingRegulator.class);
        // 加载@AutoDiscoverClass的bean, 作为后续contrller中注入使用
        ancheMappingRegulator.addMappingBean(this.ancheLoadJar.getClassLoaderMap());

        ancheMappingRegulator.addMappingService(this.ancheLoadJar.getClassLoaderMap(), this.context);

    }

}
