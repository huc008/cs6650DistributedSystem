import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Consumer extends Thread {

  private final BlockingQueue<SkiersFactory> dataBuffer;
  private final Integer numRequests;
  private final SkiersApi skierApi;
  private final AtomicInteger successful;
  private final AtomicInteger unsuccessful;

  private final CountDownLatch phaseFinish;

  public Consumer (BlockingQueue<SkiersFactory> dataBuffer, Integer numRequests, SkiersApi skiersApi, AtomicInteger successful,
      AtomicInteger unsuccessful, CountDownLatch phaseFinish) {
    this.dataBuffer = dataBuffer;
    this.numRequests = numRequests;
    this.skierApi = skiersApi;
    this.successful = successful;
    this.unsuccessful = unsuccessful;
    this.phaseFinish = phaseFinish;
  }

  @Override
  public void run() {

    for (int i = 0; i < this.numRequests; ++i) {
      try {
        SkiersFactory skier = this.dataBuffer.take();
        this.post(skier);
      } catch (InterruptedException | ApiException e) {
        throw new RuntimeException(e);
      }
      phaseFinish.countDown();
    }
  }

  private void post(SkiersFactory skier) throws ApiException {
    int retryTime = 0;
    while (retryTime < 5) {
      ApiResponse<Void> response = skierApi.writeNewLiftRideWithHttpInfo(skier.getLiftRide(),
          skier.getResortID(), skier.getSeasonID(), skier.getDayID(), skier.getSkierID());
      if (response.getStatusCode() == 201) {
        this.successful.getAndIncrement();
        break;
      } else {
        retryTime++;
      }
    }
    if (retryTime == 5) {
      this.unsuccessful.getAndIncrement();
    }
  }
}
