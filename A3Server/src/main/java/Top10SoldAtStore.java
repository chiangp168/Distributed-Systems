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

    if (urlPath == null || urlPath.isEmpty()) {
      res.getWriter().write("Missing paramterers");
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String[] urlParts = urlPath.split("/");

    if (!isUrlValid(urlParts)) {
      res.getWriter().write("Missing paramterers");
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    } else {
      String response = "";
      try (RPCClient top10SoldRPC = new RPCClient()) {
        String storeID = (urlParts[1]);
        response += top10SoldRPC.call("Store " + storeID);

        if(response != null && response.length() != 0) {
          PrintWriter out = res.getWriter();
          res.setContentType("application/json");
          res.setCharacterEncoding("UTF-8");
          out.print(response);
          out.flush();
          res.setStatus(HttpServletResponse.SC_OK);
        } else {
          res.getWriter().write("Data Not Found");
          res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }


      } catch (IOException | TimeoutException | InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private boolean isUrlValid(String[] urlPath) {
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
