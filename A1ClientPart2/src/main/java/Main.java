import java.io.File;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

  public static void main(String[] args) throws InterruptedException {
    // Testing code
//    ApiClient apiclient = new ApiClient();
//    apiclient.setBasePath("http://localhost:8080/Assignment1_war_exploded");
//    PurchaseApi apiInstance = new PurchaseApi(apiclient);
//    Purchase body = new Purchase(); // Purchase | items purchased
//    Integer storeID = 56; // Integer | ID of the store the purchase takes place at
//    Integer custID = 56; // Integer | customer ID making purchase
//    //String date = "date_example"; // String | date of purchase
//    String date = "20210233"; // String | date of purchase
//    try {
//      apiInstance.newPurchase(body, storeID, custID, date);
////      ApiResponse<Void> response = apiInstance.newPurchaseWithHttpInfo(body, storeID, custID, date);
////
////      System.out.println(response.getStatusCode());
//
//    } catch (ApiException e) {
//      System.err.println("Exception when calling PurchaseApi#newPurchase");
//      e.printStackTrace();
//    }
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
    Integer IPAddress = cl.getIPAddress();
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
    CountDownLatch centralRegion = new CountDownLatch(1);
    CountDownLatch westRegion = new CountDownLatch(1);

    //Phase one
    CountDownLatch phase1Latch = new CountDownLatch(numThreadOne);
    System.out.println("Phase 1");
    Timestamp startWallTime = new Timestamp(System.currentTimeMillis());
    for (int i = 0; i < numThreadOne; i++) {
      // creation a thread object implmenting runnable
      // need  custID and itemIDs
      Runnable thread = new StoreThread(i, numOfCustomers, maxItemID, numOfPurchase, numOfItems,
          date, IPAddress, phase1Latch, totalLatch, centralRegion, westRegion, stats, bq);
      new Thread(thread).start();
    }

    //Phase two
//    centralRegion.await();
    System.out.println("Central Activated");
    CountDownLatch phase2Latch = new CountDownLatch(numThreadTwo);
    for (int i = 0; i < numThreadTwo; i++) {
      // creation a thread object implmenting runnable
      // need  custID and itemIDs
      Runnable thread = new StoreThread(i, numOfCustomers, maxItemID, numOfPurchase, numOfItems,
          date, IPAddress, phase2Latch, totalLatch, centralRegion, westRegion, stats, bq);
      new Thread(thread).start();
    }

//    //Phase three
//    westRegion.await();
    System.out.println("West Activated");
    CountDownLatch phase3Latch = new CountDownLatch(numThreadThree);
    for (int i = 0; i < numThreadThree; i++) {
      // creation a thread object implmenting runnable
      // need  custID and itemIDs
      Runnable thread = new StoreThread(i, numOfCustomers, maxItemID, numOfPurchase, numOfItems,
          date, IPAddress, phase3Latch, totalLatch, centralRegion, westRegion, stats, bq);
      new Thread(thread).start();
    }

    totalLatch.await();
    List<String> exit = Arrays.asList("exit");
    bq.put(exit);
    try {
      writer.join();
    } catch (InterruptedException e) {

    }

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
}