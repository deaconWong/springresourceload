package deacon.test.spb.service;

import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;

public interface ICustomerExpDataQueryService {

    @Cacheable(value ="customerList", keyGenerator = "stationKeyGenerator", cacheManager = "redisCacheManager")
    public List<Map<String, Object>> queryCustomerExpInfoData(Map<String, Object> parasMap);
}
