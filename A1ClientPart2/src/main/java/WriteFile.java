import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class WriteFile implements Runnable {
  private BlockingQueue bq;
  private String file;
  private List<Integer> allTime = new ArrayList<Integer>();


  public WriteFile (BlockingQueue bq, String file) {
    this.bq = bq;
    this.file = file;

  }

  /**
   * When an object implementing interface <code>Runnable</code> is used to create a thread,
   * starting the thread causes the object's
   * <code>run</code> method to be called in that separately executing
   * thread.
   * <p>
   * The general contract of the method <code>run</code> is that it may take any action whatsoever.
   *
   * @see Thread#run()
   */
  @Override
  public void run() {
    List<String> msg;
    Boolean isFinished = false;
    try {
      BufferedWriter bw = new BufferedWriter(new FileWriter(this.file));
      String header = "start_time,request_type,latency,response_code \n";
      bw.write(header);
      while(!isFinished) {
        try {
          msg = (List<String>) bq.take();
          for (String str: msg) {
            if(str.equals("exit")) {
              isFinished = true;
              break;
            }
            String [] splitString = str.split(",");
            Integer responseTime = Integer.parseInt(splitString[2]);
            allTime.add(responseTime);
            bw.write(str);
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      bw.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public List<Integer> getAllTime() {
    return allTime;
  }
}
