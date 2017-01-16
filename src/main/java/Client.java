import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Client {

    public static long SLOW_THRESHOLD = 100;

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Client.class, args);
        IMap<String, byte[]> testMap = context.getBean(HazelcastInstance.class).getMap(Server.MAP_NAME);

        for (int i = 0; i < Server.ENTRY_COUNT; i++) {
            Stopwatch stopwatch = Stopwatch.createStarted();
            byte[] value = testMap.get(Server.buildKey(i));
            long took = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
            Preconditions.checkNotNull(value);

            if (took > SLOW_THRESHOLD) {
                System.out.printf("%d: took %d ms\n", (i + 1), took);
            }
        }
        System.out.println("done");
    }

    @Bean
    public HazelcastInstance hazelcastInstance() {
        ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig();
        clientNetworkConfig.setAddresses(ImmutableList.of("127.0.0.1:" + Server.PORT));

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setNetworkConfig(clientNetworkConfig);

        return HazelcastClient.newHazelcastClient(clientConfig);
    }
}
