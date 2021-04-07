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
    ObjectMapper objectMapper = new ObjectMapper();
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("3.86.12.228");
    factory.setUsername("chiangp");
    factory.setPassword("4143");
//    factory.setHost("localhost");
    final Connection connection = factory.newConnection();
    PurchaseItemDao purchaseDao = new PurchaseItemDao(ds);

    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          final Channel channel = connection.createChannel();
          channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
          channel.queueDeclare(QUEUE_NAME, false, false, false, null);
//          String queueName = channel.queueDeclare().getQueue();
          channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");
//          channel.queueDeclare(QUEUE_NAME, true, false, false, null);
          // max one message per receiver
//          System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");

          DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            NewItem item = objectMapper.readValue(message, NewItem.class);
//            System.out.println(
//                "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message
//                    + "'");
//            PurchaseItemDao purchaseDao = new PurchaseItemDao();
            try {
              purchaseDao.createPurchase(item);
            } catch (SQLException e) {
              e.printStackTrace();
            }
          };
          // process messages
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