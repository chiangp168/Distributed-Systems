import com.fasterxml.jackson.annotation.JsonProperty;

public class NewItem {
  @JsonProperty("storeID")
  private Integer storeID;
  @JsonProperty("custID")
  private Integer custID;
  @JsonProperty("date")
  private String date;
  @JsonProperty("items")
  private Purchase purchase;

  public NewItem(Integer storeID, Integer custID, String date, Purchase purchase){
    this.storeID = storeID;
    this.custID = custID;
    this.date = date;
    this.purchase = purchase;
  }

  private NewItem() {
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

  public Purchase getPurchase() {return purchase; }

}
