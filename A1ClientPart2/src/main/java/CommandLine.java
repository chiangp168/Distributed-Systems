import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommandLine {
  private final int TWOINPUT = 2;
  private final int SEVENINPUTS = 7;
  private final int MINSTORE = 4;
  private final int MINITEM = 1;
  private final int MAXITEM = 20;
  private Integer maxStores;
  private Integer numOfCustomers;
  private Integer maxItemID;
  private Integer numOfPurchase;
  private Integer numOfItems;
  private String date;
  private String IPAddress;

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
    this.IPAddress = null;
  }

  public boolean validateInput(String[] inputs) {
    if (inputs.length != TWOINPUT && inputs.length != SEVENINPUTS) {
      return false;}
    if(inputs.length == TWOINPUT) {
      try{
        this.maxStores = Integer.parseInt(inputs[0]);
        if (this.maxStores < MINSTORE) {
          return false;
        }
        this.IPAddress = inputs[1];
        if (this.IPAddress == null){
          return false;
        }
      }catch(NumberFormatException exception){
        return false;
      }
    } else {
      try {
        this.maxStores = Integer.parseInt(inputs[0]);
        if (this.maxStores < MINSTORE) {
          return false;
        }
        this.numOfCustomers = Integer.parseInt(inputs[1]);
        if (this.numOfCustomers < 0) {
          return false;
        }
        this.maxItemID = Integer.parseInt(inputs[2]);
        if (this.maxItemID < 0) {
          return false;
        }
        this.numOfPurchase = Integer.parseInt(inputs[3]);
        if (this.numOfPurchase < 0) {
          return false;
        }
        this.numOfItems = Integer.parseInt(inputs[4]);
        if(this.numOfItems < MINITEM || this.numOfItems > MAXITEM) {
          return false;
        }
        this.date = inputs[5];
        boolean validDate = isDateValid(this.date);
        if (!validDate) {
          return false;
        }
        this.IPAddress = inputs[6];
        if (this.IPAddress == null){
          return false;
        }
      } catch (NumberFormatException exception) {
        System.out.println("Please enter integer values for <max Stores>, <num of Customers>," +
            "<max itemID>, <num of Purchases>, <num of Items range 1-20>, <date in yyyyMMdd>" +
            "and <port num>");
        return false;
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }
    return true;
  }

  private boolean isDateValid(String input) throws ParseException {
    // reference https://stackoverflow.com/questions/20231539/java-check-the-date-format-of-current-string-is-according-to-required-format-or
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    format.setLenient(false);
    Date inputDate = null;
    try{
      inputDate = format.parse(input);
    } catch(NumberFormatException exception){
      System.out.println("Wrong Date Format");
      return false;
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

  public String getIPAddress() {
    return IPAddress;
  }
}