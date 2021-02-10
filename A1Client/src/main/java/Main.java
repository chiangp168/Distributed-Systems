import java.sql.Timestamp;
import java.util.concurrent.CountDownLatch;

public class Main {

  public static void main(String[] args) throws InterruptedException {
    // 8, 5, 10000, 10, 5, "20210101", localhost
    CommandLine cl = new CommandLine();
    if (!cl.validateInput(args)) {
      System.out.println("Invalid inputs");
    }

    Integer maxStores = cl.getMaxStores();
    Integer numOfCustomers = cl.getNumOfCustomers();
    Integer maxItemID = cl.getMaxItemID();
    Integer numOfPurchase = cl.getNumOfPurchase();
    Integer numOfItems = cl.getNumOfItems();
    String date = cl.getDate();
    String IPAddress = cl.getIPAddress();
    ShareStats stats = new ShareStats();

    Integer numThreadOne = (int ) Math.floor(maxStores / 4.0);
    Integer numThreadTwo = (int ) Math.floor(maxStores / 4.0);
    Integer numThreadThree = maxStores - numThreadOne - numThreadTwo;

    CountDownLatch totalLatch = new CountDownLatch(maxStores);
    CountDownLatch phase2Latch = new CountDownLatch(1);
    CountDownLatch phase3Latch = new CountDownLatch(1);

    Integer phase1MinStore = 1;
    Integer phase1MaxStore = phase1MinStore + numThreadOne;
    Integer phase2MinStore = phase1MaxStore;
    Integer phase2MaxStore = phase2MinStore + numThreadTwo;
    Integer phase3MinStore = phase2MaxStore;
    Integer phase3MaxStore = phase3MinStore + numThreadThree;

    Timestamp startWallTime = new Timestamp(System.currentTimeMillis());
    //Phase one
    runPhase(phase1MinStore, phase1MaxStore, numOfCustomers, maxItemID, numOfPurchase, numOfItems, date, IPAddress,
        totalLatch, phase2Latch, phase3Latch, stats);

    //Phase two
    phase2Latch.await();
    runPhase(phase2MinStore, phase2MaxStore, numOfCustomers, maxItemID, numOfPurchase, numOfItems, date, IPAddress,
        totalLatch, phase2Latch, phase3Latch, stats);

    //Phase three
    phase3Latch.await();
    runPhase(phase3MinStore, phase3MaxStore, numOfCustomers, maxItemID, numOfPurchase, numOfItems, date, IPAddress,
        totalLatch, phase2Latch, phase3Latch, stats);

    totalLatch.await();
    Timestamp endWallTime = new Timestamp(System.currentTimeMillis());

    //total number of successful requests sent
    System.out.println("Number of Successful Posts: " + stats.getSuccessfulPosts());
    //total number of unsuccessful requests (ideally should be 0)
    System.out.println("Number of Failed Posts: " + stats.getFailedPosts());
    //the total run time (wall time) for all phases to complete. Calculate this by taking a timestamp before commencing Phase 1 and another after all Phase 3 threads are complete.
    long wallTime = (endWallTime.getTime() - startWallTime.getTime()) / 1000;
    System.out.println("Wall Time is: " + wallTime + " seconds");
    //throughput = requests per second = total number of requests/wall time
    Integer totalRequests = maxStores * 9 * numOfPurchase;
    System.out.println("Total Post Requests: " + totalRequests);
    System.out.println("Throughput is: " + (totalRequests / wallTime) + " requests/seconds");

  }

  private static void runPhase (Integer startingstoreID, Integer numThread, Integer numOfCustomers,
      Integer maxItemID, Integer numOfPurchase, Integer numOfItems, String date, String IPAddress,
      CountDownLatch totalLatch, CountDownLatch phase2Latch, CountDownLatch phase3Latch, ShareStats stats){
    for (int i = startingstoreID; i < numThread; i++) {
      Runnable thread = new StoreThread(i, numOfCustomers, maxItemID, numOfPurchase, numOfItems,
          date, IPAddress, totalLatch, phase2Latch, phase3Latch, stats);
      new Thread(thread).start();
    }
  }
}