import io.swagger.client.*;
import io.swagger.client.model.*;
import io.swagger.client.api.PurchaseApi;
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
  private String url;
  private CountDownLatch totalLatch;
  private CountDownLatch phase2Latch;
  private CountDownLatch phase3Latch;
  private ShareStats stats;

  public StoreThread(Integer storeID, Integer numOfCustomers, Integer maxItemID,
      Integer numOfPurchases, Integer numOfItems, String date, String IPAddress,
      CountDownLatch totalLatch, CountDownLatch phase2Latch,
      CountDownLatch phase3Latch, ShareStats stats){
    this.storeID = storeID;
    this.numOfCustomers = numOfCustomers;
    this.maxItemID = maxItemID;
    this.numOfPurchase = numOfPurchases;
    this.numOfItems = numOfItems;
    this.date = date;
    this.url = url(IPAddress);
    this.totalLatch = totalLatch;
    this.phase2Latch = phase2Latch;
    this.phase3Latch = phase3Latch;
    this.stats = stats;
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
    apiclient.setBasePath(this.url);
    PurchaseApi apiInstance = new PurchaseApi(apiclient);
    int successfulPost = 0;
    int failedPost = 0;

    // sending total of numpurchases * 9 hours of post requests
    for (int i = 0; i < this.numOfPurchase * 9; i++) {
      //Generate the default number of items purchased (randomly select itemID) and set amount to 1.
      Purchase body = new Purchase();
      for (int j = 0; j < this.numOfItems; j++){
        PurchaseItems item = new PurchaseItems();
        Integer rand_itemID = (int)(Math.random() * (maxItemID - MIN_ITEM_ID + 1) + MIN_ITEM_ID);
        item.setItemID(Integer.toString(rand_itemID));
        item.setNumberOfItems(AMOUNT);
        body.addItemsItem(item);
      }

      //select custID (storeIDx1000) and (storeIDx1000)+number of customers/store
      Integer min = this.storeID * 1000;
      Integer max = this.storeID * 1000 + this.numOfCustomers;
      int custID = (int)(Math.random() * (max - min + 1) + min);

      // how to signal when reach 3 hours of purchases
      //send posts requests
      try {
        apiInstance.newPurchase(body, this.storeID, custID, date);
        successfulPost ++;
        //while loop to check if it reaches 3 * numofPurchases then notifyAll
        if ((successfulPost + failedPost) == 3 * numOfPurchase) {
          this.phase2Latch.countDown();
        } else if ((successfulPost + failedPost) == 5 * numOfPurchase) {
          this.phase3Latch.countDown();
        }
      } catch (ApiException e) {
        failedPost ++;
        //log error to stderr
        System.err.printf("Failed to Send a Request!");
        e.printStackTrace();
      }

    }
    //update total successful count using lock
    this.stats.addSuccessfulPosts(successfulPost);
    this.stats.addFailedPosts(failedPost);
    this.totalLatch.countDown();
  }

  private static String url (String IPAddress) {
    if (IPAddress.equals("localhost")) {
      return "http://localhost:8080/A1Server_war_exploded";
    } else {
      return "http://" + IPAddress + ":8080/A1Server_war";
    }
  }
}