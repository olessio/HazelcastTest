import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.internal.util.ThreadLocalRandom;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @author opoleshuk
 */
public class ServerAndClient {
    private static final String MAP_NAME = "testmap";
    private static final long SLOW_THRESHOLD = TimeUnit.MILLISECONDS.toNanos(100);
    private static final int PORT = 4321;

    private static final int VALUE_SIZE = 800000;
    private static final int ENTRY_COUNT = 100;

    private static final ThreadLocalRandom random = ThreadLocalRandom.current();


    public static void main(String[] args) {
        {
            HazelcastInstance server = serverInstance();
            IMap<String, byte[]> testMap = server.getMap(MAP_NAME);

            for (int i = 0; i < ENTRY_COUNT; i++) {
                byte[] value = generateRandom(VALUE_SIZE);
                testMap.put(buildKey(i), value);
            }
        }

        HazelcastInstance client = clientInstance();
        IMap<String, byte[]> clientMap = client.getMap(MAP_NAME);

        for (int i = 0; i < Server.ENTRY_COUNT; i++) {
            String key = buildKey(i);

            long time0 = System.nanoTime();
            byte[] value = clientMap.get(key);
            long took = System.nanoTime() - time0;

            if (took > SLOW_THRESHOLD) {
                System.out.printf("%d: took %d ms\n", (i + 1), TimeUnit.NANOSECONDS.toMillis(took));
            }
        }
        System.out.println("done");
        System.exit(0);
    }

    private static byte[] generateRandom(int size) {
        byte[] result = new byte[size];
        random.nextBytes(result);
        return result;
    }

    private static String buildKey(int i) {
        return "key" + i;
    }


    private static HazelcastInstance serverInstance() {
        NetworkConfig networkConfig = new NetworkConfig();
        networkConfig.setPort(PORT);
        networkConfig.setReuseAddress(true);

        networkConfig.getInterfaces().setEnabled(true)
                .addInterface("127.0.0.1");

        networkConfig.getJoin().getMulticastConfig().setEnabled(false);

        Config config = new Config();
        config.setNetworkConfig(networkConfig);

        return Hazelcast.newHazelcastInstance(config);
    }

    private static HazelcastInstance clientInstance() {
        ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig();
        clientNetworkConfig.setAddresses(Collections.singletonList("127.0.0.1:" + Server.PORT));

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setNetworkConfig(clientNetworkConfig);

        return HazelcastClient.newHazelcastClient(clientConfig);
    }

}
