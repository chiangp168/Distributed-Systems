import com.google.gson.annotations.SerializedName;

public class NewItem {
  private Integer storeID;
  private Integer custID;
  private String date;
  private String items;

  public NewItem(Integer storeID, Integer custID, String date, String items){
    this.storeID = storeID;
    this.custID = custID;
    this.date = date;
    this.items = items;
  }

  public Integer getStoreID() {
    return storeID;
  }

  public Integer getCustID() {
    return custID;
  }

  public String getDate() {
    return date;
  }

  public String getItems() {
    return items;
  }

}
