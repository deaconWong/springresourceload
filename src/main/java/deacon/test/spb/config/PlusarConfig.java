package deacon.test.spb.config;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import deacon.test.spb.model.PulsarProperties;

@Configuration
@EnableConfigurationProperties(PulsarProperties.class)
public class PlusarConfig {
    private Logger log = LoggerFactory.getLogger(PlusarConfig.class);

    /**
     * 注册PulsarClient
     */
    @Bean
    @ConditionalOnMissingBean
    public PulsarClient pulsarClient(PulsarProperties pulsarProperties) throws PulsarClientException {
        if (!StringUtils.isEmpty(pulsarProperties.getServiceUrl())) {
            log.debug("PulsarClient创建完成");
            return PulsarClient.builder().serviceUrl(pulsarProperties.getServiceUrl()).ioThreads(pulsarProperties
                .getIoThreads()).listenerThreads(pulsarProperties.getListenerThreads()).build();

        }
        return null;
    }
}