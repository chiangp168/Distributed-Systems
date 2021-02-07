public class Item {
  private String itemId;
  private Integer numOfItems;

  public Item(String itemId, Integer numOfItems) {
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
}
