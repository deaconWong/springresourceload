package deacon.test.spb.util;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

@Configuration
public class AcEomsDataCollectApplicationListener implements ApplicationListener<ContextRefreshedEvent> {
    
    private Logger logger = LoggerFactory.getLogger(AcEomsDataCollectApplicationListener.class);

    /**
     * 在启动的时候加载配置文件, 后续的任务中会改变对应配置项的内容
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            String[] activeProfiles = event.getApplicationContext().getEnvironment().getActiveProfiles();
            Assert.state(activeProfiles.length == 1, "未配置profile.active");
            String fileName =
                ResourceUtils.getURL("classpath:application-" + activeProfiles[0] + ".yml").toURI().getPath();

            File yml = new File(fileName);
            logger.info("使用ResourceUtils在jar中加载配置文件-->:{}" , yml.getCanonicalPath());
        } catch (Exception e) {
            logger.error("使用ResourceUtils在jar中加载配置文件时错误:{}", e.getMessage(), e);
        }

    }

}
