import io.swagger.client.*;
import io.swagger.client.auth.*;
import io.swagger.client.model.*;
import io.swagger.client.api.PurchaseApi;
import java.util.Random;
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

  public StoreThread(Integer storeID, Integer numOfCustomers, Integer maxItemID,
      Integer numOfPurchases, Integer numOfItems, String date, Integer IPAddresse,
      CountDownLatch phaseLatch, CountDownLatch totalLatch, CountDownLatch centralRegion,
      CountDownLatch westRegion, ShareStats stats){
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
    apiclient.setBasePath("http://localhost:8080/Assignment1_war_exploded");
    PurchaseApi apiInstance = new PurchaseApi(apiclient);
    int successfulPost = 0;
    int failedPost = 0;
    //Generate the default number of items purchased (randomly select itemID) and set amount to 1.
    Purchase body = new Purchase();
    for (int i = 0; i < this.numOfItems; i++){
      PurchaseItems item = new PurchaseItems();
      Integer rand_itemID = (int)(Math.random() * (maxItemID - MIN_ITEM_ID + 1) + MIN_ITEM_ID);
      item.setItemID(Integer.toString(rand_itemID));
      item.setNumberOfItems(AMOUNT);
      body.addItemsItem(item);
    }

    // sending total of numpurchases * 9 hours of post requests
    for (int i = 0; i < this.numOfPurchase * 9; i++) {
//      System.out.println("This is store " + this.storeID + "with request # " + i);
      //select custID
      //(storeIDx1000) and (storeIDx1000)+number of customers/store
      Integer min = this.storeID * 1000;
      Integer max = this.storeID * 1000 + this.numOfCustomers;
      int custID = (int)(Math.random() * (max - min + 1) + min);

      // how to signal when reach 3 hours of purchases
      //send posts requests
      try {
        apiInstance.newPurchase(body, this.storeID, custID, date);
        successfulPost ++;
        //while loop to check if it reaches 3 * numofPurchases then notifyAll
        if (successfulPost == 3 * numOfPurchase) {
          this.centralRegion.countDown();
        } else if (successfulPost == 5 * numOfPurchase) {
          this.westRegion.countDown();
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
    this.stats.addSuccessfulPosts(failedPost);

    //count down each thread
    this.phaseLatch.countDown();
    this.totalLatch.countDown();
  }
}