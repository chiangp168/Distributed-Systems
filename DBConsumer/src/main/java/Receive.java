import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Receive {

  private static final String EXCHANGE_NAME = "PurchaseDB";
  private static final Integer MAX_THREADS = 60;
  private static final String QUEUE_NAME = "DBQueue";

  public static void main(String[] argv) throws Exception {
    DataSource ds = new DataSource();
    DataSource2 ds2 = new DataSource2();
    ObjectMapper objectMapper = new ObjectMapper();
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("RabbitMQServerIP");
    factory.setUsername("RabbitMQUserName");
    factory.setPassword("RabbitMQPassword");
//    factory.setHost("localhost");
    final Connection connection = factory.newConnection();
    PurchaseItemDao purchaseDao = new PurchaseItemDao(ds, ds2);

    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          final Channel channel = connection.createChannel();
          channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
          channel.queueDeclare(QUEUE_NAME, false, false, false, null);
          channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");


          DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            NewItem item = objectMapper.readValue(message, NewItem.class);
            Integer storeID = item.getStoreID();
            Integer dbresult = storeID % 2;

            try {
              purchaseDao.createPurchase(item, dbresult);
            } catch (SQLException e) {
              e.printStackTrace();
            }
          };
          channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
          });
        } catch (IOException ex) {
          Logger.getLogger(Receive.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    };

    for (int i=0; i< MAX_THREADS; i++) {
      new Thread(runnable).start();
    }

  }
}