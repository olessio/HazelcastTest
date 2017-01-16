import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import java.util.Random;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Server {

    public static final String MAP_NAME = "testmap";

    public static int PORT = 4321;
    public static int VALUE_SIZE = 800000;
    public static int ENTRY_COUNT = 100;

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Server.class, args);

        IMap<String, byte[]> testMap = context.getBean(HazelcastInstance.class).getMap(MAP_NAME);

        byte[] value = generateRandom(VALUE_SIZE);

        for (int i = 0; i < ENTRY_COUNT; i++) {
            testMap.put(buildKey(i), value);
        }

        System.out.println("done");
    }

    @Bean
    public HazelcastInstance hazelcastInstance() {
        NetworkConfig networkConfig = new NetworkConfig();
        networkConfig.setPort(PORT);

        Config config = new Config();
        config.setNetworkConfig(networkConfig);

        return Hazelcast.newHazelcastInstance(config);
    }

    private static byte[] generateRandom(int size) {
        byte[] result = new byte[size];
        Random random = new Random();
        random.nextBytes(result);
        return result;
    }

    public static String buildKey(int i) {
        return "key" + i;
    }
}
