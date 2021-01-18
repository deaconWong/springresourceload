package deacon.test.spb.controller;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/collect")
public class DataCollectTimeSyncController {

    @PostMapping(value = "/settime", produces =MediaType.APPLICATION_JSON_VALUE)
    @Cacheable(value ="testRedis1", cacheManager = "redisCacheManager")
    public String timeSync(@RequestBody String jsonParam) {

        return "testRedis-Cachable1";
    }

}
