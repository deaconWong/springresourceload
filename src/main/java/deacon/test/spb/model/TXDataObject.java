package deacon.test.spb.model;

import static com.alibaba.fastjson.JSON.parseObject;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;

/**
 * 通用数据通讯对象 commandid: 命令字id parameters: 通讯参数集合 result: 通讯是否成功标记
 * 
 * @author wanglm
 * @date 2020/07/14
 */
public class TXDataObject {

    private static final Logger log = LoggerFactory.getLogger(TXDataObject.class);

    private String commandid;

    private Map<String, Object> parameters;

    private boolean result;

    // 默认为响应数据
    private DataOperationType dataOperationType = DataOperationType.REQUEST;

    private TXDataObject(Builder builder) {
        // if commandid is null, then convert to empty string
        Assert.state(!Strings.isNullOrEmpty(builder.commandId), "commandid不能为空");
        this.commandid = builder.commandId;
        // if parameters is null, then convert to empty map
        this.parameters = Objects.isNull(builder.parameters) ? Collections.emptyMap() : builder.parameters;

        if (null != builder.operationType)
            this.dataOperationType = DataOperationType.valueOf(builder.operationType);

        // if parameters is empty return false; otherwise return true
        // dataOperationType 为REQUEST时, 默认为true
        this.result = !this.parameters.isEmpty() && Boolean.parseBoolean(parameters.getOrDefault("result",
            DataOperationType.REQUEST == this.dataOperationType).toString());
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getParameter(String key) {
        Object attr = parameters.get(key);
        log.debug("从数据集中获取属性值:{}-->{}", key, attr);
        return null == attr ? "" : String.valueOf(attr);
    }

    public void removeParameter(String paramkey) {
        log.debug("从数据集中删除属性值:{}-->{}", paramkey, this.parameters.remove(paramkey));
    }

    public static class Builder {
        private String commandId;

        private Map<String, Object> parameters;

        private String operationType;

        public Builder commandId(String commandId) {
            this.commandId = commandId;
            return this;
        }

        public Builder parameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder operationType(String operationType) {
            this.operationType = operationType;
            return this;
        }

        public TXDataObject build() {
            return new TXDataObject(this);
        }

        public TXDataObject build(String json) {
            try {
                JSONObject objectJson = parseObject(json);
                return commandId(objectJson.getString("kye1")).parameters(objectJson.getJSONObject("ke2")).build();
            } catch (Exception e) {
                log.error("根据json内容:{}转换数据通讯对象时失败:{}", json, e.getMessage(), e);
            }
            return build();
        }
    }

    public String getCommandid() {
        return commandid;
    }

    public boolean isResult() {
        return result;
    }

}
