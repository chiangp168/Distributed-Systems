public class CommandLine {
  private final int ONEINPUT = 1;
  private final int SEVENINPUTS = 7;
  private Integer maxStores;
  private Integer numOfCustomers;
  private Integer maxItemID;
  private Integer numOfPurchase;
  private Integer numOfItems;
  private String date;
  private Integer IPAddress;

//  maximum number of stores to simulate (maxStores)
//  number of customers/store (default 1000). This is the range of custIDs per store
//  maximum itemID - default 100000
//  number of purchases per hour: (default 60) - thus is numPurchases
//  number of items for each purchase (range 1-20, default 5)
//  date - default to 20210101
//  IP/port address of the server

  public CommandLine() {
    this.maxStores = 0;
    this.numOfCustomers = 1000;
    this.maxItemID = 100000;
    this.numOfPurchase = 60;
    this.numOfItems = 5;
    this.date = "20210101";
    this.IPAddress = 8080;
  }

  public boolean validateInput(String[] inputs) {
    if (inputs.length != ONEINPUT && inputs.length != SEVENINPUTS) {
      return false;}
    if(inputs.length == 1) {
      try{
        this.maxStores = Integer.parseInt(inputs[0]);
      }catch(NumberFormatException exception){
        return false;
      }
    } else {
      try {
        this.maxStores = Integer.parseInt(inputs[0]);
        this.numOfCustomers = Integer.parseInt(inputs[1]);
        this.maxItemID = Integer.parseInt(inputs[2]);
        this.numOfPurchase = Integer.parseInt(inputs[3]);
        this.numOfItems = Integer.parseInt(inputs[4]);
        if(this.numOfItems < 1 || this.numOfItems > 20) {
          System.out.println("Num of Items should range between 1-20.");
          return false;
        }
        this.date = inputs[5];
        this.IPAddress = Integer.parseInt(inputs[6]);
      } catch (NumberFormatException exception) {
        System.out.println("Please enter integer values for <max Stores>, <num of Customers>," +
            "<max itemID>, <num of Purchases>, <num of Items range 1-20>, <date in yyyyMMdd>" +
            "and <port num>");
        return false;
      }
    }
    return true;
  }

  public Integer getMaxStores() {
    return maxStores;
  }

  public Integer getNumOfCustomers() {
    return numOfCustomers;
  }

  public Integer getMaxItemID() {
    return maxItemID;
  }

  public Integer getNumOfPurchase() {
    return numOfPurchase;
  }

  public Integer getNumOfItems() {
    return numOfItems;
  }

  public String getDate() {
    return date;
  }

  public Integer getIPAddress() {
    return IPAddress;
  }
}