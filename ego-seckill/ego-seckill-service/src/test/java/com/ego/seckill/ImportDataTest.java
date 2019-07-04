package com.ego.seckill;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 〈〉
 *
 * @author coach tam
 * @email 327395128@qq.com
 * @create 2019/4/11
 * @since 1.0.0
 * 〈坚持灵活 灵活坚持〉
 */
@SpringBootTest(classes = EgoSeckillApplication.class)
@RunWith(SpringRunner.class)
public class ImportDataTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private static final String KEY_PREFIX = "ego:seckill:stock";

    @Test
    public void test(){
        BoundHashOperations<String,Object,Object> hashOperations = this.stringRedisTemplate.boundHashOps(KEY_PREFIX);
        hashOperations.put("2600242","10");
    }
}
