package achilles.frontend.query.server.sqldb;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;

public class ConnectionPool {
    private static final Logger logger = Logger.getLogger(ConnectionPool.class);
    /** 单例模式, 懒汉式. */
    private static ConnectionPool instance;

    public static ConnectionPool getInstance() {
        if (instance == null) {
            instance = new ConnectionPool();
        }
        return instance;
    }

    /** 连接池. */
    private final DataSource ds = new DataSource();

    private ConnectionPool() {
        try {
            /** 数据库配置. */
            final Properties props = new Properties();
            props.load(Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("database.properties"));

            String ip = props.getProperty("ip");
            String port = props.getProperty("port");
            String dbname = props.getProperty("dbname");
            String userName = props.getProperty("account");
            String password = props.getProperty("password");

            ds.setDriverClassName("com.mysql.jdbc.Driver");
            ds.setUrl("jdbc:mysql://" + ip + ":" + port + "/" + dbname);
            ds.setUsername(userName);
            ds.setPassword(password);
            ds.setInitialSize(5);
            ds.setMaxActive(10);
            ds.setMaxIdle(5);
            ds.setMinIdle(2);
        } catch (FileNotFoundException e) {
            logger.debug(e.getMessage());
        } catch (IOException e) {
            logger.debug(e.getMessage());
        }
    }

    public Connection getConnection() {
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            logger.debug(e.getMessage());
            return null;
        }
    }

    // Unit Test
    public static void main(String[] args) {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select * from t_status_info");
            int cnt = 1;
            while (rs.next()) {
                System.out.println((cnt++) + ". Host:" + rs.getString("idstr"));
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            logger.debug(e.getMessage());
        } finally {
            if (conn != null)
                try {
                    conn.close();
                } catch (Exception ignore) {
                }
        }
    }
}
