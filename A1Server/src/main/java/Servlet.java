import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

public class Servlet extends HttpServlet {

  // /purchase/{storeID}/customer/{custID}/date/{date}
  private Gson gson = new Gson();
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
    // (and maybe also some value if input is valid)

    try {
      if (!isUrlValid(urlParts)) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.getWriter().write("Invalid Input");
      } else {
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("URL works!");
      }
    } catch (ParseException e) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("Date Not Found");
    }

  }


  private boolean isUrlValid(String[] urlPath) throws ParseException {
    // TODO: validate the request url path according to the API spec
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
      System.out.println("Wrong");
      return false;
    }
    return true;
  }
}
