import io.swagger.client.*;
import io.swagger.client.auth.*;
import io.swagger.client.model.*;
import io.swagger.client.api.PurchaseApi;

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    Integer numThreadOne = (int ) Math.floor(maxStores / 4.0);
    Integer numThreadTwo = (int ) Math.floor(maxStores / 4.0);
    Integer numThreadThree = maxStores - numThreadOne - numThreadTwo;

    CountDownLatch totalLatch = new CountDownLatch(maxStores);

    CountDownLatch centralRegion = new CountDownLatch(1);
    CountDownLatch westRegion = new CountDownLatch(1);
    //Phase one
    CountDownLatch phase1Latch = new CountDownLatch(numThreadOne);
    System.out.println("Phase 1");
    for (int i = 1; i <= numThreadOne; i++) {
      // creation a thread object implmenting runnable
      // need  custID and itemIDs
      Runnable thread = new StoreThread(i, numOfCustomers, maxItemID, numOfPurchase, numOfItems, date, IPAddress, phase1Latch, totalLatch, centralRegion, westRegion);
      new Thread(thread).start();
    }
//    phase1Latch.await();
    //Phase two
    centralRegion.await();
    System.out.println("Centrl Activated");
    CountDownLatch phase2Latch = new CountDownLatch(numThreadTwo);
    for (int i = 3; i <= 4; i++) {
      // creation a thread object implmenting runnable
      // need  custID and itemIDs
      Runnable thread = new StoreThread(i, numOfCustomers, maxItemID, numOfPurchase, numOfItems, date, IPAddress, phase2Latch, totalLatch, centralRegion, westRegion);
      new Thread(thread).start();
    }
////    phase2Latch.await();
//    //Phase three
    westRegion.await();
    System.out.println("West Activated");
    CountDownLatch phase3Latch = new CountDownLatch(numThreadThree);
    for (int i = 5; i <= 6; i++) {
      // creation a thread object implmenting runnable
      // need  custID and itemIDs
      Runnable thread = new StoreThread(i, numOfCustomers, maxItemID, numOfPurchase, numOfItems, date, IPAddress, phase3Latch, totalLatch, centralRegion, westRegion);
      new Thread(thread).start();
    }
//    phase3Latch.await();
    totalLatch.await();
    //total number of successful requests sent
    //total number of unsuccessful requests (ideally should be 0)
    //the total run time (wall time) for all phases to complete. Calculate this by taking a timestamp before commencing Phase 1 and another after all Phase 3 threads are complete.
    //throughput = requests per second = total number of requests/wall time

  }
}