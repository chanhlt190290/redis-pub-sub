/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oh.subscriber;

import oh.subscriber.redis.SubscriberListener;
import oh.subscriber.redis.SubscriberHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 *
 * @author trungchanh
 */
@SpringBootApplication
public class Application {

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private String redisPort;

    @Autowired
    SubscriberListener listener;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    Jedis getRedisClient() {
        Jedis jedis = getJedisPool().getResource();
        return jedis;
    }

    @Bean
    JedisPool getJedisPool() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        final JedisPool jedisPool = new JedisPool(poolConfig, redisHost, Integer.parseInt(redisPort), 0);
        return jedisPool;
    }

    @Bean
    SubscriberHandler getSubscriberHandler() {
        return new SubscriberHandler();
    }

    @Bean
    public CommandLineRunner commandLineRunner(org.springframework.context.ApplicationContext ctx) {
        return args -> {
            listener.start();
        };
    }

}
