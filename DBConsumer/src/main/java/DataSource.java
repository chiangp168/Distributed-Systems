import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
public class DataSource {

  private static HikariConfig config = new HikariConfig();
  private static HikariDataSource ds;
//  private static final String HOST_NAME = System.getProperty("MySQL_IP_ADDRESS");
//  private static final String PORT = System.getProperty("MySQL_PORT");
//  private static final String DATABASE = "SupermarketApp";
//  private static final String USERNAME = System.getProperty("DB_USERNAME");
//  private static final String PASSWORD = System.getProperty("DB_PASSWORD");
//  private static final String HOST_NAME = "localhost";
//  private static final String PORT = "3306";
//  private static final String DATABASE = "SupermarketApp";
//  private static final String USERNAME = "root";
//  private static final String PASSWORD = "4143";
  private static final String HOST_NAME = "database-1.cqfv2ulxelqg.us-east-1.rds.amazonaws.com";
  private static final String PORT = "3306";
  private static final String DATABASE = "SupermarketApp";
  private static final String USERNAME = "chiangp";
  private static final String PASSWORD = "Hsuan#410043";

  static {
    String url = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", HOST_NAME, PORT, DATABASE);
    config.setDriverClassName("com.mysql.cj.jdbc.Driver");
    config.setJdbcUrl(url);
    config.setUsername(USERNAME);
    config.setPassword(PASSWORD);
    config.addDataSourceProperty( "cachePrepStmts" , "true" );
    config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
    config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
    config.setConnectionTimeout(1500);
    config.setMaximumPoolSize(10);
    ds = new HikariDataSource(config);
  }

  public DataSource() {}

  public static Connection getConnection() throws SQLException {
    return ds.getConnection();
  }

}