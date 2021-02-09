import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Purchase {
  @SerializedName("items")
  private List<PurchaseItems> purchaseItems;

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
    return "Purchase Class {items: " + purchaseItems + " }";
  }
}
