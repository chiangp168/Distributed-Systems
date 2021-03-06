import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Servlet extends HttpServlet {

  // /purchase/{storeID}/customer/{custID}/date/{date}
  private final String CUSTOMER_String = "customer";
  private final String DATE_String = "date";
  private final Integer URL_Length = 6;

  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json"); //set to Json
    String urlPath = req.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing paramterers");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    try {
      if (!isUrlValid(urlParts)) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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
        Purchase onePurchase =  new ObjectMapper().readValue(sb.toString(), Purchase.class);
        PurchaseItemDao purchaseDao = new PurchaseItemDao();
        purchaseDao.createPurchase(new NewItem(Integer.parseInt(urlParts[1]),
            Integer.parseInt(urlParts[3]), urlParts[5], onePurchase.toString()));
        res.setStatus(HttpServletResponse.SC_OK);
      }
    } catch (ParseException e) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } catch (SQLException e) {
      e.printStackTrace();
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      throw new IOException(e);
    }

  }

  private boolean isUrlValid(String[] urlPath) throws ParseException {
    // urlPath  = "purchase/{storeID}/customer/{custID}/date/{date}"
    // urlParts = [purchase, int32, customer, int32, date, string]
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
}
