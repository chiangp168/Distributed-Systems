import Model.NewItem;
import Model.Purchase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

@WebServlet(name = "Servlet")
public class PostServlet extends HttpServlet{

  // /purchase/{storeID}/customer/{custID}/date/{date}
  private final String CUSTOMER_String = "customer";
  private final String DATE_String = "date";
  private final Integer URL_Length = 6;
  private static final String EXCHANGE_NAME = "PurchaseDB";
  private static ObjectPool<Channel> pool;
  public static Connection connection;
  private static ConnectionFactory factory;
  private static ChannelFactory channelFactory;
  private Channel firstChannel;

  public void init() throws ServletException{
    factory = new ConnectionFactory();
    factory.setHost("RabbitMQServerIP");
    factory.setUsername("RabbitMQUserName");
    factory.setPassword("RabbitMQPassword");
//    factory.setHost("localhost");
    firstChannel = null;
    try {
      connection = factory.newConnection();
      channelFactory = new ChannelFactory(connection);
      pool = new GenericObjectPool<Channel>(channelFactory);
      firstChannel = pool.borrowObject();
      firstChannel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json"); //set to Json
    String urlPath = req.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("Missing paramterers");
      return;
    }

    String[] urlParts = urlPath.split("/");;
    // and now validate url path and return the response status code
    try {
      if (!isUrlValid(urlParts)) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.getWriter().write("Missing paramterers");
      } else {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = req.getReader();
        try {
          String line;
          while ((line = reader.readLine()) != null) {
            sb.append(line).append('\n');
          }
        } finally {
          reader.close();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Purchase onePurchase =  objectMapper.readValue(sb.toString(), Purchase.class);
        NewItem newItem = new NewItem(Integer.parseInt(urlParts[1]), Integer.parseInt(urlParts[3]),
            urlParts[5], onePurchase);
        String newItemString = objectMapper.writeValueAsString(newItem);

        Channel channel = null;
        try {
          channel = pool.borrowObject();
//          channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
          channel.basicPublish(EXCHANGE_NAME, "", null, newItemString.getBytes("UTF-8"));
          res.setStatus(HttpServletResponse.SC_OK);
          res.getWriter().write("Request posted");
        } catch (Exception e) {
          throw new RuntimeException("Unable to borrow channel from pool" + e.toString());
        } finally {
          if (null != channel) {
            try {
              pool.returnObject(channel);
            } catch (Exception e) {
              e.toString();
            }
          }
        }
      }
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }


  private boolean isUrlValid(String[] urlPath) throws ParseException {
    // urlPath  = "purchase/{storeID}/customer/{custID}/date/{date}"
    // urlParts = [, purchase, int32, customer, int32, date, string]
    if (urlPath.length < URL_Length) {return false;}
    if (!urlPath[2].equals(CUSTOMER_String) || !urlPath[4].equals(DATE_String) || urlPath[5].isEmpty()) {
      return false; }
    return isInteger(urlPath[1]) && isInteger(urlPath[3]) && isDateValid(urlPath[5]);
  }

  private boolean isInteger(String input) {
    try{
      Integer.parseInt(input);
    }catch(NumberFormatException exception){
      return false;
    }
    return true;
  }

  private boolean isDateValid(String input) throws ParseException {
    // reference https://stackoverflow.com/questions/20231539/java-check-the-date-format-of-current-string-is-according-to-required-format-or
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    format.setLenient(false);
    Date inputDate = null;
    try{
      inputDate = format.parse(input);
    } catch(NumberFormatException exception){
      System.out.println("Wrong Date Format");
      return false;
    }
    return true;
  }

  @Override
  public void destroy() {
    try {
      connection.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
