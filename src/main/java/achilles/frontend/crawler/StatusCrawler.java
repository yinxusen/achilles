package achilles.frontend.crawler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import weibo4j.http.HttpClient;
import weibo4j.model.PostParameter;
import weibo4j.model.Status;
import weibo4j.model.StatusWapper;
import weibo4j.model.WeiboException;
import weibo4j.util.WeiboConfig;

import achilles.frontend.query.server.sqldb.ConnectionPool;
import achilles.frontend.query.server.sqldb.DB;

/**
 * this class is the driver to drive recommend module. this class crawl data
 * from weibo and insert them into database.
 * 
 * @author 朱忠良
 */
public class StatusCrawler {
	private final int user_status = 0;
	private final int friend_status = 1;
	protected final Connection conn;
	HttpClient client;

	public StatusCrawler() {
		this.conn = ConnectionPool.getInstance().getConnection();
		client = new HttpClient();
	}

	public void run() {
		try {
			while (true) {
				String[][] crawler_target = DB.getCrawlerTarget();
				for (int i = 0; i < crawler_target.length; i++) {
					if (crawler_target[i].length != 4) {
						break; // data exception
					}
					// every user execute follow code.
					String user_id = crawler_target[i][0];
					client.setToken(crawler_target[i][1]);
					String since_id = crawler_target[i][2];
					StatusWapper statuswapper;
					statuswapper = Status.constructWapperStatus(client.get(
							WeiboConfig.getValue("baseURL")
									+ "statuses/user_timeline.json",
							new PostParameter[] { new PostParameter("since_id",
									since_id) }));
					List<Status> statuses = statuswapper.getStatuses();
					this.handleStatus(statuses, user_status, user_id);
					since_id = crawler_target[i][3]; // ?
//					System.out.println("**********************************friend_since_id: "+since_id);
//					statuswapper = Status.constructWapperStatus(client.get(
//							WeiboConfig.getValue("baseURL")
//									+ "statuses/friends_timeline.json",
//							new PostParameter[] { new PostParameter("since_id",
//									since_id) }));
//					statuses = statuswapper.getStatuses();
//					this.handleStatus(statuses, friend_status, user_id);
//					DiscountQueryService.recommend();//recommend
					Thread.sleep(30000);// sleep 30 s
				}
			}
		} catch (WeiboException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * get user's statuses
	 * @param access_token
	 * @param since_id
	 * @return
	 * @throws WeiboException
	 */
	public static List<Status> crawlUserStatuses(String access_token,String since_id) throws WeiboException{
		HttpClient client = new HttpClient();
		client.setToken(access_token);
		StatusWapper statuswapper;
		statuswapper = Status.constructWapperStatus(client.get(
				WeiboConfig.getValue("baseURL")
						+ "statuses/user_timeline.json",
				new PostParameter[] { new PostParameter("since_id",
						since_id) }));
		return statuswapper.getStatuses();
	}
	/**
	 * get user's friends's statuses
	 * @param access_token
	 * @param since_id
	 * @return
	 * @throws WeiboException
	 */
	public static List<Status> crawlFriendsStatuses(String access_token,String since_id) throws WeiboException{
		HttpClient client = new HttpClient();
		client.setToken(access_token);
		StatusWapper statuswapper = Status.constructWapperStatus(client.get(
				WeiboConfig.getValue("baseURL")
						+ "statuses/friends_timeline.json",
				new PostParameter[] { new PostParameter("since_id",
						since_id) }));
		return statuswapper.getStatuses();
	}
	/**
	 * 
	 * @param statuses
	 * @param status_type
	 * @param user_id
	 * @return
	 * @throws SQLException
	 */
	public Status handleStatus(List<Status> statuses, int status_type,
			String user_id) throws SQLException {
		DB.insertStatus(statuses);
		Status latestStatus = statuses.size()>0?statuses.get(0):null;
		if (latestStatus != null) {
			if (status_type == user_status)
				DB.updateCrawlerUserTarget(latestStatus.getUser().getId(),
						latestStatus.getId()); // update latest status_id
			else if (status_type == friend_status) {
				DB.updateCrawlerFriendTarget(user_id,
						latestStatus.getId());// update latest friend_id
			}
		}
		return latestStatus;
	}

	public static void main(String[] args) {
		StatusCrawler sc = new StatusCrawler();
		sc.run();
	}
}
