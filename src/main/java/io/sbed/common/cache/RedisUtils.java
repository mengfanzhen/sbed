package io.sbed.common.cache;

import com.google.gson.Gson;
import io.sbed.common.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author
 * @Description: (Redis工具类)
 * @date 2017-6-23 15:07
 */
@Component
public class RedisUtils {

    //是否开启redis缓存  true开启   false关闭
//    @Value("${spring.redis.open: #{false}}")
//    private static boolean open;

//    @Autowired
    private static RedisTemplate<Object, Object> redisTemplate;
//    @Autowired
    private static ValueOperations<Object, Object> valueOperations;
//    @Autowired
    private static HashOperations<Object, Object, Object> hashOperations;
//    @Autowired
    private static ListOperations<Object, Object> listOperations;
//    @Autowired
    private static SetOperations<Object, Object> setOperations;
//    @Autowired
    private static ZSetOperations<Object, Object> zSetOperations;

    /**  不设置过期时长 */
    public final static long NOT_EXPIRE = -1;

    private final static Gson gson = new Gson();

    public static boolean exists(String key) {
//        if(!open){
//            return false;
//        }

        return redisTemplate.hasKey(key);
    }

    public static void set(String key, Object value, long expire){
//        if(!open){
//            return;
//        }

        valueOperations.set(key, toJson(value));
        if(expire != NOT_EXPIRE){
            redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        }
    }

    public static void set(String key, Object value){
//        if(!open){
//            return;
//        }

        set(key, value, Constant.Time.Second.day_1);
    }

    public static <T> T get(String key, Class<T> clazz, long expire) {
//        if(!open){
//            return null;
//        }

        String value = (String)valueOperations.get(key);
        if(expire != NOT_EXPIRE){
            redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        }
        return value == null ? null : fromJson(value, clazz);
    }

    public static <T> T get(String key, Class<T> clazz) {
//        if(!open){
//            return null;
//        }

        return get(key, clazz, NOT_EXPIRE);
    }

    public static String get(String key, long expire) {
//        if(!open){
//            return null;
//        }

        String value = (String)valueOperations.get(key);
        if(expire != NOT_EXPIRE){
            redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        }
        return value;
    }

    public static String get(String key) {
//        if(!open){
//            return null;
//        }

        return get(key, NOT_EXPIRE);
    }

    public static void delete(String key) {
//        if(!open){
//            return;
//        }

        if(exists(key)){
            redisTemplate.delete(key);
        }
    }

    public static void delete(String... keys) {
//        if(!open){
//            return;
//        }

        for (String key : keys) {
            redisTemplate.delete(key);
        }
    }

    public static void deletePattern(String pattern) {
//        if(!open){
//            return;
//        }

        Set<Object> keys = redisTemplate.keys(pattern);
        if (keys.size() > 0) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * Object转成JSON数据
     */
    private static String toJson(Object object){
//        if(!open){
//            return null;
//        }

        if(object instanceof Integer || object instanceof Long || object instanceof Float ||
                object instanceof Double || object instanceof Boolean || object instanceof String){
            return String.valueOf(object);
        }
        return gson.toJson(object);
    }

    /**
     * JSON数据，转成Object
     */
    private static <T> T fromJson(String json, Class<T> clazz){
//        if(!open){
//            return null;
//        }

        return gson.fromJson(json, clazz);
    }


//=================================================setter/getter================================================

    @Autowired
    @Qualifier("redisTemplate")
    public void setRedisTemplate(RedisTemplate<Object, Object> redisTemplate) {
        RedisUtils.redisTemplate = redisTemplate;
    }
    @Autowired
    @Qualifier("valueOperations")
    public void setValueOperations(ValueOperations<Object, Object> valueOperations) {
        RedisUtils.valueOperations = valueOperations;
    }
    @Autowired
    @Qualifier("hashOperations")
    public void setHashOperations(HashOperations<Object, Object, Object> hashOperations) {
        RedisUtils.hashOperations = hashOperations;
    }
    @Autowired
    @Qualifier("listOperations")
    public void setListOperations(ListOperations<Object, Object> listOperations) {
        RedisUtils.listOperations = listOperations;
    }
    @Autowired
    @Qualifier("setOperations")
    public void setSetOperations(SetOperations<Object, Object> setOperations) {
        RedisUtils.setOperations = setOperations;
    }
    @Autowired
    @Qualifier("zSetOperations")
    public void setzSetOperations(ZSetOperations<Object, Object> zSetOperations) {
        RedisUtils.zSetOperations = zSetOperations;
    }
}
