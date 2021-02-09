import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.PurchaseApi;
import io.swagger.client.model.Purchase;
import io.swagger.client.model.PurchaseItems;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class StoreThread implements Runnable {
  private final int MIN_ITEM_ID = 1;
  private final int AMOUNT = 1;
  private Integer storeID;
  private Integer numOfCustomers;
  private Integer maxItemID;
  private Integer numOfPurchase;
  private Integer numOfItems;
  private String date;
  private Integer IPAddress;
  private CountDownLatch phaseLatch;
  private CountDownLatch totalLatch;
  private CountDownLatch centralRegion;
  private CountDownLatch westRegion;
  private ShareStats stats;
  private BlockingQueue bq;

  public StoreThread(Integer storeID, Integer numOfCustomers, Integer maxItemID,
      Integer numOfPurchases, Integer numOfItems, String date, Integer IPAddresse,
      CountDownLatch phaseLatch, CountDownLatch totalLatch, CountDownLatch centralRegion,
      CountDownLatch westRegion, ShareStats stats, BlockingQueue bq){
    this.storeID = storeID;
    this.numOfCustomers = numOfCustomers;
    this.maxItemID = maxItemID;
    this.numOfPurchase = numOfPurchases;
    this.numOfItems = numOfItems;
    this.date = date;
    this.IPAddress = IPAddresse;
    this.phaseLatch = phaseLatch;
    this.totalLatch = totalLatch;
    this.centralRegion = centralRegion;
    this.westRegion = westRegion;
    this.stats = stats;
    this.bq = bq;
  }


  /**
   * When an object implementing interface <code>Runnable</code> is used to create a thread,
   * starting the thread causes the object's
   * <code>run</code> method to be called in that separately executing
   * thread.
   * <p>
   * The general contract of the method <code>run</code> is that it may take any action whatsoever.
   *
   * @see Thread#run()
   */
  @Override
  public void run() {
    ApiClient apiclient = new ApiClient();
    apiclient.setBasePath("http://localhost:8080/A1Server_war_exploded");
    PurchaseApi apiInstance = new PurchaseApi(apiclient);
    int successfulPost = 0;
    int failedPost = 0;
    int resCode;
    List<String> result = new ArrayList<String>();
    Timestamp startLatencyTime;
    Timestamp endLatencyTime;

    // sending total of numpurchases * 9 hours of post requests
    for (int i = 0; i < this.numOfPurchase * 9; i++) {
      //Generate the default number of items purchased (randomly select itemID) and set amount to 1.
      Purchase body = new Purchase();
      for (int j = 0; j < this.numOfItems; j++) {
        PurchaseItems item = new PurchaseItems();
        Integer rand_itemID = (int) (Math.random() * (maxItemID - MIN_ITEM_ID + 1) + MIN_ITEM_ID);
        item.setItemID(Integer.toString(rand_itemID));
        item.setNumberOfItems(AMOUNT);
        body.addItemsItem(item);
      }
//      System.out.println("This is store " + this.storeID + "with request # " + i);
      //select custID
      //(storeIDx1000) and (storeIDx1000)+number of customers/store
      Integer min = this.storeID * 1000;
      Integer max = this.storeID * 1000 + this.numOfCustomers;
      int custID = (int) (Math.random() * (max - min + 1) + min);

      // start of latency time
      startLatencyTime = new Timestamp(System.currentTimeMillis());
      // how to signal when reach 3 hours of purchases
      //send posts requests
      try {
        apiInstance.newPurchase(body, this.storeID, custID, date);
        endLatencyTime = new Timestamp(System.currentTimeMillis());
        successfulPost++;
        resCode = 200;
        //while loop to check if it reaches 3 * numofPurchases then notifyAll
        if (successfulPost == 3 * numOfPurchase) {
          this.centralRegion.countDown();
        } else if (successfulPost == 5 * numOfPurchase) {
          this.westRegion.countDown();
        }
      } catch (ApiException e) {
        endLatencyTime = new Timestamp(System.currentTimeMillis());
        failedPost++;
        resCode = e.getCode();
        //log error to stderr
        System.err.printf("Failed to Send a Request!");
        e.printStackTrace();
      }
      long latency = endLatencyTime.getTime() - startLatencyTime.getTime();
      String msg = startLatencyTime.toString() + ", POST, " + latency + ", " + resCode + "\n";
      result.add(msg);
    }

    //put latency data into blocking queue
    try {
      bq.put(result);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    //update total successful count using lock
    this.stats.addSuccessfulPosts(successfulPost);
    this.stats.addSuccessfulPosts(failedPost);

    //count down each thread
    this.phaseLatch.countDown();
    this.totalLatch.countDown();
  }
}