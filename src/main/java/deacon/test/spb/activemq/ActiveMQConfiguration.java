package deacon.test.spb.activemq;

import java.util.Arrays;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.SimpleJmsListenerContainerFactory;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;

@Configuration
@EnableJms
public class ActiveMQConfiguration {
    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    @Value("${spring.activemq.user}")
    private String username;

    @Value("${spring.activemq.password}")
    private String password;
    
    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public ConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(username, password, brokerUrl);
        factory.setTrustedPackages(Arrays.asList("cn.anche"));
        return factory;
    }

    @Bean("jmsMessagingTemplate")
    public JmsMessagingTemplate jmsMessageTemplate() {
        return new JmsMessagingTemplate(connectionFactory());
    }

    @Bean("topicMessagingTemplate")
    public JmsMessagingTemplate topicMessagingTemplate() {
        JmsMessagingTemplate topicMessagingTemplate = new JmsMessagingTemplate(connectionFactory());
        JmsTemplate topicTemplate = new JmsTemplate(connectionFactory());
        topicTemplate.setPubSubDomain(true);
        topicMessagingTemplate.setJmsTemplate(topicTemplate);
        return topicMessagingTemplate;
    }

    // 在Queue模式中，对消息的监听需要对containerFactory进行配置
    @Bean("queueContainerFactory")
    public JmsListenerContainerFactory<?> queueJmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleJmsListenerContainerFactory factory = new SimpleJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPubSubDomain(false);
        return factory;
    }

    // 在Topic模式中，对消息的监听需要对containerFactory进行配置
    @Bean("topicContainerFactory")
    public JmsListenerContainerFactory<?> topicJmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleJmsListenerContainerFactory factory = new SimpleJmsListenerContainerFactory();
        factory.setClientId("datacenter_msg_" + applicationName);
        factory.setConnectionFactory(connectionFactory);
        // factory.setConcurrency("10-100");
        factory.setPubSubDomain(true);
        factory.setSessionTransacted(true);
        factory.setAutoStartup(true);
        //开启持久化订阅, 能获取到历史消息
        factory.setSubscriptionDurable(true);
        return factory;
    }
    
 // 在Topic模式中，对消息的监听需要对containerFactory进行配置
    @Bean("fileContenttopicFactory")
    public JmsListenerContainerFactory<?> fileContentListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleJmsListenerContainerFactory factory = new SimpleJmsListenerContainerFactory();
        factory.setClientId("datacenter_file_" + applicationName);
        factory.setConnectionFactory(connectionFactory);
        // factory.setConcurrency("10-100");
        factory.setPubSubDomain(true);
        factory.setSessionTransacted(true);
        factory.setAutoStartup(true);
        //开启持久化订阅, 能获取到历史消息
        factory.setSubscriptionDurable(true);
        return factory;
    }
    
}