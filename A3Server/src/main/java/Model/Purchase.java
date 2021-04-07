package Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Purchase {
  @JsonProperty("items")
  private List<PurchaseItems> purchaseItems;

  private Purchase() {
  }

  public Purchase(List<PurchaseItems> purchaseItems) {
    this.purchaseItems = purchaseItems;
  }

  public List<PurchaseItems> getPurchaseItems() {
    return this.purchaseItems;
  }

  public void setPurchaseItems(List<PurchaseItems> purchaseItems) {
    this.purchaseItems = purchaseItems;
  }

  @Override
  public String toString() {
    return "{items: " + purchaseItems + " }";
  }
}
