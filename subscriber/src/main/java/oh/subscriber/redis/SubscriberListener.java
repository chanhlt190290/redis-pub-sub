package oh.subscriber.redis;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;

@Component
public class SubscriberListener implements DisposableBean, Runnable {

    private static Thread THREAD;
    private boolean isRunning = true;

    @Autowired
    Jedis jedis;

    @Autowired
    SubscriberHandler handler;

    @Value("${redis.channel}")
    private String redisChannel;

    private SubscriberListener() {

        Thread thread = getThreadByName("Subscriber");
        if (thread != null) {
            THREAD = thread;
        } else {
            THREAD = new Thread(this);
            THREAD.setName("Subscriber");
        }

    }

    private Thread getThreadByName(String threadName) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals(threadName)) {
                return t;
            }
        }
        return null;
    }

    @Override
    public void run() {
        if (isRunning) {
            try {
                System.out.println("Subscriber listener started.");
                jedis.subscribe(handler, redisChannel);
                System.out.println("Subscriber listener ended.");
            } catch (Exception e) {
                System.out.println("Subscriber listener failed.");
                e.printStackTrace();
            }
        } else {
            System.out.println("Subscriber listener stopped.");
        }
    }

    @Override
    public void destroy() {
        isRunning = false;
    }

    public void start() {
        if (!THREAD.isAlive()) {
            THREAD.start();
        }
        isRunning = true;
    }

}
