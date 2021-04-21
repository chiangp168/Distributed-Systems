import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;


public class PurchaseItemDao {

  private static DataSource ds;
  private static DataSource2 ds2;

  public PurchaseItemDao(DataSource ds, DataSource2 ds2) {
    this.ds = ds;
    this.ds2 = ds2;

  }

  public void createPurchase(NewItem newPurchase, Integer key) throws SQLException{
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    String uniqueID = UUID.randomUUID().toString();
    String insertQueryStatement = "INSERT INTO Purchase (RecordId, StoreId, CustId, PurchaseDate, Items) " +
        "VALUES (?,?,?,?,?)";
    try {
      if(key == 0) {
        conn = this.ds.getConnection();
      } else {
        conn = this.ds2.getConnection();
      }
      preparedStatement = conn.prepareStatement(insertQueryStatement);
      preparedStatement.setString(1, uniqueID);
      preparedStatement.setInt(2, newPurchase.getStoreID());
      preparedStatement.setInt(3, newPurchase.getCustID());
      preparedStatement.setString(4, newPurchase.getDate());
      preparedStatement.setString(5, newPurchase.getPurchase().toString());
      // execute insert SQL statement
      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
      throw e;
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
        if (preparedStatement != null) {
          preparedStatement.close();
        }
      } catch (SQLException se) {
        se.printStackTrace();
      }
    }
  }
}