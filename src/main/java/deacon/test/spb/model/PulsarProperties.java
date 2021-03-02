package deacon.test.spb.model;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pulsar")
public class PulsarProperties {
    private String serviceUrl;
    private Integer ioThreads = 10;
    private Integer listenerThreads = 10;
    private List<String> jsonProducers;
    private List<String> stringProducers;
    private List<String> consumers;

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public Integer getIoThreads() {
        return ioThreads;
    }

    public void setIoThreads(Integer ioThreads) {
        this.ioThreads = ioThreads;
    }

    public Integer getListenerThreads() {
        return listenerThreads;
    }

    public void setListenerThreads(Integer listenerThreads) {
        this.listenerThreads = listenerThreads;
    }

    public List<String> getJsonProducers() {
        return jsonProducers;
    }

    public void setJsonProducers(List<String> jsonProducers) {
        this.jsonProducers = jsonProducers;
    }

    public List<String> getStringProducers() {
        return stringProducers;
    }

    public void setStringProducers(List<String> stringProducers) {
        this.stringProducers = stringProducers;
    }

    public List<String> getConsumers() {
        return consumers;
    }

    public void setConsumers(List<String> consumers) {
        this.consumers = consumers;
    }

}