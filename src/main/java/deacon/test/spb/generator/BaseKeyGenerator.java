package deacon.test.spb.generator;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.interceptor.KeyGenerator;

import com.alibaba.fastjson.JSON;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

/**
 * @author wanglm
 * @date 2021/12/31
 */
public abstract class BaseKeyGenerator implements KeyGenerator {
    protected static final String CITY = "city";

    protected static final String PROVINCE = "province";

    public Object generate(Object target, Method method, Object... params) {
        return getKey(params);
    }

    protected abstract Object getKey(Object[] params);

    @SuppressWarnings("unchecked")
    protected String mergeConditions(Map<String, Object> params) {
        List<Map<String, Object>> areaList = (List<Map<String, Object>>)params.get("area");
        // 解析区域条件参数
        List<String> tempList = Lists.newArrayList();
        StringBuilder keyBuilder = null;
        for (Map<String, Object> areaMap : areaList) {
            keyBuilder = new StringBuilder();
            keyBuilder.append(MoreObjects.firstNonNull(areaMap.get(PROVINCE), "")).append("-")
                .append(MoreObjects.firstNonNull(areaMap.get(CITY), ""));
            tempList.add(keyBuilder.toString());
        }
        //过滤参数都为空字符串的参数之后排序
        List<String> filteredList = tempList.stream().filter(key -> key.length() > 1)
            .sorted(Comparator.comparing(String::toString)).collect(Collectors.toList());
        if (filteredList.isEmpty()) {
            return "全国";
        } else {
            return JSON.toJSONString(filteredList);
        }
    }

}
