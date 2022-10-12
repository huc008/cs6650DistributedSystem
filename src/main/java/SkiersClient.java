import static java.lang.Math.round;

import io.swagger.client.ApiClient;
import io.swagger.client.api.SkiersApi;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class SkiersClient {
  private static final String BASE_PATH = "http://localhost:8080/cs6650_lab_servlets_war_exploded";
//  private static final String BASE_PATH = "http://52.43.243.190:8080/cs6650_lab_servlets_war_exploded";

  private static final Integer NUM_REQUESTS = 10;

  public static void main(String[] args) throws InterruptedException {
    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(BASE_PATH);
    SkiersApi skiersApi = new SkiersApi(apiClient);
    BlockingQueue<SkiersFactory> dataBuffer = new LinkedBlockingQueue<>();

    //There is one thread producer keep generating the NUM_POSTS of skiers data
    //and put the generated skiers data into dataBuffer waiting for processes by consumers
    new Thread(new Producer(dataBuffer, NUM_REQUESTS)).start();

    //There are multi-thread consumer working concurrently to process the data in the dataBuffer
    int phase1Threads = 3;
    int phase1Requests = 10;
    int phase2Trigger = phase1Threads/4;

    long start = System.currentTimeMillis();
    CountDownLatch phase2Signal = new CountDownLatch(phase2Trigger);
    MultiThreadWrapper phase1 = new MultiThreadWrapper("Phase 1", phase1Threads, phase1Requests, skiersApi,
        phase2Signal, dataBuffer);
    phase1.startPhase();

//    int phase2Threads = 64;
//    int phase2Requests = 1500;
//    CountDownLatch phase3Signal = new CountDownLatch((int) (phase2Threads * 0.1));
//    MultiThreadWrapper phase2 = new MultiThreadWrapper("Phase 2", phase2Threads, phase2Requests, skiersApi,
//        phase3Signal, dataBuffer);
//    phase2Signal.await();
//    phase2.startPhase();
    long end = System.currentTimeMillis();
//
    phase1.finishPhase();
//    phase2.finishPhase();
    phase1.phaseOutputs();
//    phase2.phaseOutputs();
    double runtime = (end - start)*0.001;
    System.out.println("============ PART 1 OUTPUT =============");
    System.out.println("Total run time to send 100k requests: " + round(runtime) + "/s");
    System.out.println("Total throughput to send 100k requests: " + round(NUM_REQUESTS /runtime) + "/s");
  }
}
