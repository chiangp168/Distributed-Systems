import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

  public static void main(String[] args) throws InterruptedException, IOException {
    // 8, 5, 10000, 10, 5, "20210101", 8080
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

    // Create a consumer object to take in data and write to a file
    BlockingQueue bq = new LinkedBlockingQueue();
    String file = "result.csv";
    WriteFile writeFile = new WriteFile(bq, file);
    Thread writer = new Thread(writeFile);
    writer.start();

    //Create CountDownLatch
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
        totalLatch, phase2Latch, phase3Latch, stats, bq);

    //Phase two
    phase2Latch.await();
    runPhase(phase2MinStore, phase2MaxStore, numOfCustomers, maxItemID, numOfPurchase, numOfItems, date, IPAddress,
        totalLatch, phase2Latch, phase3Latch, stats, bq);

    //Phase three
    phase3Latch.await();
    runPhase(phase3MinStore, phase3MaxStore, numOfCustomers, maxItemID, numOfPurchase, numOfItems, date, IPAddress,
        totalLatch, phase2Latch, phase3Latch, stats, bq);

    totalLatch.await();
    Timestamp endWallTime = new Timestamp(System.currentTimeMillis());

    List<String> exit = Arrays.asList("exit");
    bq.put(exit);
    try {
      writer.join();
    } catch (InterruptedException e) {

    }

    //Calculate the mean
    Integer totalTime = 0;
    Integer maxTime = Integer.MIN_VALUE;

    List<Integer> allTime = writeFile.getAllTime();
    for(Integer time: allTime) {
      totalTime += time;
      if (time > maxTime) {
          maxTime = time;
       }
    }

    Integer mean = totalTime / allTime.size();

    System.out.println("Mean Response Time: " + mean + " ms");
    System.out.println("Max Response Time: " + maxTime + " ms");

    //Calculate median
    Collections.sort(allTime);
    Double median;
    Integer arraySize = allTime.size();
    if (arraySize % 2 == 0) {
      Integer sum = allTime.get(arraySize / 2 - 1) + allTime.get(arraySize / 2);
      median = sum / 2.0;
    } else {
      median = (double) allTime.get(arraySize / 2);
    }
    System.out.println("Median Response Time: " + median + " ms");

//    calculate 99 percentile
    Integer ninetyNine = allTime.get((int)Math.ceil((0.99 * arraySize)));
    System.out.println("99 Percentile: " + ninetyNine + " ms");

    System.out.println("Number of Successful Posts: " + stats.getSuccessfulPosts());
    System.out.println("Number of Failed Posts: " + stats.getFailedPosts());
    System.out.println("Total Post Requests: " + (stats.getSuccessfulPosts() + stats.getFailedPosts()));
    long wallTime = (endWallTime.getTime() - startWallTime.getTime()) / 1000;
    System.out.println("Wall Time is: " + wallTime + " seconds");
    System.out.println("Throughput is: " + (stats.getSuccessfulPosts() / wallTime) + " requests/seconds");
  }

  private static void runPhase (Integer startingstoreID, Integer numThread, Integer numOfCustomers,
      Integer maxItemID, Integer numOfPurchase, Integer numOfItems, String date, String IPAddress,
      CountDownLatch totalLatch, CountDownLatch phase2Latch, CountDownLatch phase3Latch,
      ShareStats stats, BlockingQueue bq){
    for (int i = startingstoreID; i < numThread; i++) {
      Runnable thread = new StoreThread(i, numOfCustomers, maxItemID, numOfPurchase, numOfItems,
          date, IPAddress, totalLatch, phase2Latch, phase3Latch, stats, bq);
      new Thread(thread).start();
    }
  }
}