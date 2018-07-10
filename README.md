# Redis Pub-Sub
### Set up Redis server
1. Creating the Redis Deployment (**redis-deployment.yaml**)
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis
  labels:
    app: redis
spec:
  selector:
    matchLabels:
      app: redis
  replicas: 1
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
      - name: redis
        image: redis:4
        ports:
        - containerPort: 6379
```
2. Run Redis as instances
```shell
kubectl apply -f redis-deployment.yaml
```
3. Create the Redis Service (**redis-service.yaml**)
```yaml
apiVersion: v1
kind: Service
metadata:
  name: redis
  labels:
    app: redis
spec:
  ports:
  - port: 6379
    targetPort: 6379
  selector:
    app: redis
```
4. Publish Redis instances as service
```shell
kubectl apply -f redis-service.yaml
```
5. Check service running
```shell
kubectl get pods
redis-76d55d8d56-mhw67            1/1       Running   0          26m
```
```shell
kubectl get services
redis             ClusterIP      10.19.248.150   <none>          6379/TCP       3s
```

### Set up Publisher
1. Create Spring Boot project with Jedis dependency
```xml
<dependency>
	<groupId>redis.clients</groupId>
	<artifactId>jedis</artifactId>
</dependency>
```
2. Create Publisher class
```java
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
```
3. Create Bean for Publisher
```java
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
	Publisher getPublisher() {
		return new Publisher(getRedisClient(), redisChannel);
	}
```
4. Publish messages with Publisher
```java
public class ContractController {

    @Autowired
    Publisher publisher;

    @Autowired
    ContractRepo contractRepo;

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addContract(@RequestParam("name") String name, @RequestParam("type") int type) {
        Contract contract = new Contract();
        contract.setName(name);
        contract.setType(type);
        Contract data = contractRepo.save(contract);
        publisher.publish("There is a new contract has been added!");
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
}
```


### Set up Subscriber
1. Create Spring Boot project with Jedis dependency
```xml
<dependency>
	<groupId>redis.clients</groupId>
	<artifactId>jedis</artifactId>
</dependency>
```
2. Create SubscriberHandler class
```java
	public class SubscriberHandler extends JedisPubSub {

		@Override
		public void onMessage(String channel, String message) {
			System.out.println("channel: " + channel + ", message: " + message);
		}

		@Override
		public void onPMessage(String pattern, String channel, String message) {
		}

		@Override
		public void onSubscribe(String channel, int subscribedChannels) {
		}

		@Override
		public void onUnsubscribe(String channel, int subscribedChannels) {
		}

		@Override
		public void onPUnsubscribe(String pattern, int subscribedChannels) {
		}

		@Override
		public void onPSubscribe(String pattern, int subscribedChannels) {
		}
	}
```
3. Create SubscriberListener class
```java
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
```
4. Create Bean for SubscriberHandler
```java
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
```
5. Start SubscriberListener
```java
	@Autowired
    SubscriberListener listener;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

	@Bean
    public CommandLineRunner commandLineRunner(org.springframework.context.ApplicationContext ctx) {
        return args -> {
            listener.start();
        };
    }
```


### Test result
1. Create new contract on publis
```shell
curl -X POST http://localhost:8080/add  -d "name=new contract&type=1"
```
2. Check output in subscriber console
```shell
Subscriber listener started.
channel: test-channel, message: There is a new contract has been added!
```