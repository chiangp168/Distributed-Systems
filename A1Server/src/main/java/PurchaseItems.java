import com.google.gson.annotations.SerializedName;

public class PurchaseItems {
  @SerializedName("itemID")
  private String itemId;
  @SerializedName("numberOfItems:")
  private Integer numOfItems;

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
    return "PurchaseItems Class {itemID=" + itemId + ", numOfItems=" + numOfItems + "}";
  }
}
