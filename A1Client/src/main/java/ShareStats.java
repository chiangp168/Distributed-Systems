public class ShareStats {
  private Integer successfulPosts;
  private Integer failedPosts;

  public ShareStats(){
    this.successfulPosts = 0;
    this.failedPosts = 0;
  }

  public synchronized void addSuccessfulPosts(Integer posts){
    this.successfulPosts += posts;
  }

  public synchronized void addFailedPosts(Integer posts){
    this.failedPosts += posts;
  }

  public Integer getSuccessfulPosts() {
    return successfulPosts;
  }

  public Integer getFailedPosts() {
    return failedPosts;
  }
}
