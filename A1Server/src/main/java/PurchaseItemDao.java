import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;



public class PurchaseItemDao {

  private static DataSource ds;

  public void createPurchase(NewItem newPurchase) throws SQLException{
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    String uniqueID = UUID.randomUUID().toString();
    String insertQueryStatement = "INSERT INTO Purchase (RecordId, StoreId, CustId, PurchaseDate, Items) " +
        "VALUES (?,?,?,?,?)";
    try {
      conn = ds.getConnection();
      preparedStatement = conn.prepareStatement(insertQueryStatement);
      preparedStatement.setString(1, uniqueID);
      preparedStatement.setInt(2, newPurchase.getStoreID());
      preparedStatement.setInt(3, newPurchase.getCustID());
      preparedStatement.setString(4, newPurchase.getDate());
      preparedStatement.setString(5, newPurchase.getItems());
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