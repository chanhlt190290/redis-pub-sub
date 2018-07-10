/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oh.publisher.redis;

import redis.clients.jedis.Jedis;

/**
 *
 * @author trungchanh
 */
public class Publisher {

    private final Jedis publisherJedis;
    private final String channel;

    public Publisher(Jedis publisherJedis, String channel) {
        this.publisherJedis = publisherJedis;
        this.channel = channel;
    }

    public void publish(String message) {
        publisherJedis.publish(channel, message);
    }
}
