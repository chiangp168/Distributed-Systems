import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import io.swagger.client.model.TopItems;
import io.swagger.client.model.TopItemsStores;
import io.swagger.client.model.TopStores;
import io.swagger.client.model.TopStoresStores;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Store {
  private static final String EXCHANGE_NAME = "PurchaseDB";
  private static final Integer MAX_THREADS = 60;
  private static final String QUEUE_NAME = "StoreQueue";
  private static final String RPC_QUEUE_NAME = "RPCQueue";


  // Receive message from queue
  public static void main(String[] argv) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("RabbitMQServerIP");
    factory.setUsername("RabbitMQUserName");
    factory.setPassword("RabbitMQPassword");

    final Connection connection = factory.newConnection();

    ConcurrentHashMap<Integer, ConcurrentHashMap> allItemMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, ConcurrentHashMap> storeMap = new ConcurrentHashMap<>();

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
            NewPostItem item = objectMapper.readValue(message, NewPostItem.class);
            Integer storeID = item.getStoreID();
            Purchase purchase = item.getPurchase();
            storeMap.putIfAbsent(storeID, new ConcurrentHashMap<String, Integer>());
            ConcurrentHashMap<String, Integer> itemMap = storeMap.get(storeID);
            ConcurrentHashMap<Integer, Integer> topStoreMap;

            for(PurchaseItems p: purchase.getPurchaseItems()) {
              String itemID = p.getItemId();
              Integer numOfItems = p.getNumOfItems();
              Integer oldNum = itemMap.get(itemID);
              if(oldNum == null) {
                itemMap.put(itemID, numOfItems);
              } else {
                itemMap.replace(itemID, numOfItems + oldNum);
              }

              allItemMap.putIfAbsent(Integer.valueOf(itemID), new ConcurrentHashMap<Integer, Integer>());
              topStoreMap = allItemMap.get(Integer.valueOf(itemID));
//              System.out.println(topStoreMap);
              Integer oldItems = topStoreMap.get(storeID);
              if(oldItems == null) {
                topStoreMap.put(storeID, numOfItems);
              } else {
                topStoreMap.replace(storeID, numOfItems + oldItems);
              }

            }
          };
          channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
          });
        } catch (IOException ex) {
          Logger.getLogger(Store.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    };


    for (int i=0; i< MAX_THREADS; i++) {
      new Thread(runnable).start();
    }

    Channel channel = connection.createChannel();
    channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
    channel.queuePurge(RPC_QUEUE_NAME);
    channel.basicQos(1);

    System.out.println("[x] Awaiting RPC requests");

    Object monitor = new Object();
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      AMQP.BasicProperties replyProps = new AMQP.BasicProperties
          .Builder()
          .correlationId(delivery.getProperties().getCorrelationId())
          .build();

      String response = "";

      try {
        String message = new String(delivery.getBody(), "UTF-8");
        String[] messageParts = message.split(" ");
        if (messageParts[0].equals("Store")) {
          int storeID = Integer.parseInt(messageParts[1]);

          System.out.println(" [.] storeID(" + message + ")");
          String finalResult = top10PurchasesOfAStore(storeID, storeMap);
          response += finalResult;
          System.out.println(response);
        } else {
          int itemID = Integer.parseInt(messageParts[1]);
          System.out.println(" [.] itemID(" + message + ")");
          String finalResult = top10StoreOfAItem(itemID, allItemMap);
          response += finalResult;
          System.out.println(response);
        }
      } catch (RuntimeException e) {
        System.out.println(" [.] " + e.toString());
      } finally {
        channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        // RabbitMq consumer worker thread notifies the RPC server owner thread
        synchronized (monitor) {
          monitor.notify();
        }
      }
    };

    channel.basicConsume(RPC_QUEUE_NAME, false, deliverCallback, (consumerTag -> { }));

    while (true) {
      synchronized (monitor) {
        try {
          monitor.wait();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private static String top10PurchasesOfAStore(int storeID, ConcurrentHashMap<Integer, ConcurrentHashMap> storeMap)
      throws JsonProcessingException {
    ConcurrentHashMap<String, Integer> itemMap = storeMap.get(storeID);
    PriorityQueue<Map.Entry<String, Integer>> minHeap = new PriorityQueue<>(
        Comparator.comparingInt(Entry::getValue));

    for(Map.Entry<String, Integer> entry : itemMap.entrySet()) {
      if(minHeap.size() < 10) {
        minHeap.add(entry);
      } else {
        if(minHeap.peek().getValue() < entry.getValue()) {
          minHeap.poll();
          minHeap.add(entry);
        }
      }
    }

    List<TopItemsStores> top10List = new ArrayList<>();

    while(!minHeap.isEmpty()) {
      Map.Entry<String, Integer> topItemsMap = minHeap.poll();
      Integer itemID = Integer.valueOf(topItemsMap.getKey());
      Integer num = topItemsMap.getValue();
      TopItemsStores topItem = new TopItemsStores();
      topItem.setItemID(itemID);
      topItem.setNumberOfItems(num);
      top10List.add(topItem);
    }
    Collections.reverse(top10List);

    TopItems store = new TopItems();
    store.setStores(top10List);
    ObjectMapper objectMapper = new ObjectMapper();
    String finalResult = objectMapper.writeValueAsString(store);
    System.out.println(finalResult);
    return finalResult;
  }

  private static String top10StoreOfAItem(int itemID, ConcurrentHashMap<Integer, ConcurrentHashMap> allItemMap)
      throws JsonProcessingException {
    ConcurrentHashMap<Integer, Integer> oneStoreMap = allItemMap.get(itemID);
    PriorityQueue<Map.Entry<Integer, Integer>> minHeap = new PriorityQueue<>(
        Comparator.comparingInt(Entry::getValue));

    for(Map.Entry<Integer, Integer> entry : oneStoreMap.entrySet()) {
      if(minHeap.size() < 10) {
        minHeap.add(entry);
      } else {
        if(minHeap.peek().getValue() < entry.getValue()) {
          minHeap.poll();
          minHeap.add(entry);
        }
      }
    }

    List<TopStoresStores> top10StoreList = new ArrayList<>();

    while(!minHeap.isEmpty()) {
      Map.Entry<Integer, Integer> topStoresMap = minHeap.poll();
      Integer storeID = Integer.valueOf(topStoresMap.getKey());
      Integer num = topStoresMap.getValue();
      TopStoresStores topStore = new TopStoresStores();
      topStore.setStoreID(storeID);
      topStore.setNumberOfItems(num);
      top10StoreList.add(topStore);
    }
    Collections.reverse(top10StoreList);
    TopStores store = new TopStores();
    store.setStores(top10StoreList);
    ObjectMapper objectMapper = new ObjectMapper();
    String finalResult = objectMapper.writeValueAsString(store);
    System.out.println(finalResult);
    return finalResult;
  }
}
