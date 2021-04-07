import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeoutException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "Top10SoldAtStore")
public class Top10SoldAtStore extends HttpServlet {
  private final Integer URL_Length = 2;

  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("text/plain");
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
    if (!isUrlValid(urlParts)) {
      res.getWriter().write("bad url");
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {
      String response = "";
      try (RPCClient top10SoldRPC = new RPCClient()) {
        String storeID = (urlParts[1]);
        String requestString = "Store " + storeID;
//        System.out.println(" [x] Requesting" + requestString);
        response += top10SoldRPC.call("Store " + storeID);
//        System.out.println(" [.] Got '" + response + "'");
      } catch (IOException | TimeoutException | InterruptedException e) {
        e.printStackTrace();
      }
      PrintWriter out = res.getWriter();
      res.setContentType("application/json");
      res.setCharacterEncoding("UTF-8");
      out.print(response);
      out.flush();
      res.setStatus(HttpServletResponse.SC_OK);
    }
  }

  private boolean isUrlValid(String[] urlPath) {
    // TODO: validate the request url path according to the API spec
    // urlPath  = "/items/store/storeID"
    // urlParts = [, items, store, int32]
    if (urlPath.length < URL_Length) {return false;}
    if (urlPath[1].isEmpty()) {
      return false; }
    return isInteger(urlPath[1]);
  }

  private boolean isInteger(String input) {
    try{
      Integer.parseInt(input);
    }catch(NumberFormatException exception){
      return false;
    }
    return true;
  }
}
