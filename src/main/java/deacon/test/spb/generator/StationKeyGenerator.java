package deacon.test.spb.generator;

import java.util.Map;

import org.springframework.stereotype.Service;

/**
 * 检测信息查询时, 缓存key生成策略
 * 
 * @author wanglm
 * @date 2021/12/30
 */
@Service("stationKeyGenerator")
public class StationKeyGenerator extends BaseKeyGenerator {

    @SuppressWarnings("unchecked")
    public String getKey(Object[] params) {
        Map<String, Object> areaMap = (Map<String, Object>)params[0];
        return mergeConditions(areaMap);
    }

}