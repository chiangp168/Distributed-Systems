import com.fasterxml.jackson.annotation.JsonProperty;

public class PurchaseItems {
//  @SerializedName("ItemID")
  @JsonProperty("ItemID")
  private String itemId;
//  @SerializedName("numberOfItems:")
  @JsonProperty("numberOfItems")
  private Integer numOfItems;

  private PurchaseItems() {
  }

  public PurchaseItems(String itemId, Integer numOfItems) {
    this.itemId = itemId;
    this.numOfItems = numOfItems;
  }

  public Integer getNumOfItems() {
    return numOfItems;
  }

  public String getItemId() {
    return itemId;
  }

  public void setNumOfItems(Integer numOfItems) {
    this.numOfItems = numOfItems;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  @Override
  public String toString() {
    return "{itemID=" + this.itemId + ", numOfItems=" + this.numOfItems + "}";
  }
}
