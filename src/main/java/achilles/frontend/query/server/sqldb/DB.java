package achilles.frontend.query.server.sqldb;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import weibo4j.model.Status;

import achilles.util.LocPaser;

public class DB {
	private static final double default_radius = 0.00001;
	private static SimpleDateFormat dateformat = new SimpleDateFormat(
			"yyyy-MM-dd hh:mm:ss");
	private static final long validtime = 30 * 60 * 1000; //30min

	/**
	 * 
	 * @return
	 */
	static java.sql.Connection conn = ConnectionPool.getInstance()
			.getConnection();

	public static String[][] getCrawlerTarget() {
		ArrayList<String[]> ret = new ArrayList<String[]>();
		try {
			java.sql.Statement stmt = conn.createStatement();
			String sql = "select * from t_crawler_target";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String[] curRet = new String[4];
				curRet[0] = rs.getString("user_id");
				curRet[1] = rs.getString("access_token");
				curRet[2] = rs.getString("status_id");
				curRet[3] = rs.getString("friend_since_id");
				ret.add(curRet);
			}
			String[][] strret = ret.toArray(new String[ret.size()][]);
			rs.close();
			stmt.close();
			
			return strret;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			return new String[0][];
		}
	}

	/**
	 * 
	 * @param userid
	 * @param status_id
	 * @return
	 */
	public static boolean updateCrawlerUserTarget(String userid,
			String status_id) {
		try {
			java.sql.Statement stmt = conn.createStatement();
			String sql = "update t_crawler_target SET status_id='" + status_id
					+ "' where user_id='" + userid + "'";
			boolean ret = stmt.execute(sql);
			stmt.close();
			
			return ret;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
			return false;
		}
	}

	public static boolean updateCrawlerFriendTarget(String userid,
			String status_id) {
		try {
			java.sql.Statement stmt = conn.createStatement();
			String sql = "update t_crawler_target SET friend_since_id='"
					+ status_id + "' where user_id='" + userid + "'";
			boolean ret = stmt.execute(sql);
			stmt.close();
			
			return ret;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
			return false;
		}
	}

	/**
	 * choose status with max_status_id of every user, and this status must be
	 * that created in 30 min.
	 * 
	 * @return
	 */
	public static String[][] getLatestStatus() {
		String sql = "select t_status_fresh.created_at,t_status_fresh.user_id,t_status_fresh.latitude,t_status_fresh.longitude,t_status_fresh.idstr "
				+ "from t_status_fresh,(select max(status_id) as maxstatusid,user_id from t_status_fresh group by user_id) as cndtab "
				+ "where cndtab.user_id=t_status_fresh.user_id and t_status_fresh.status_id=cndtab.maxstatusid";
		try {
			java.sql.Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			ArrayList<String[]> retarray = new ArrayList<String[]>();
			while (rs.next()) {
				String created_at_time = rs.getString("created_at");
				long longtime = dateformat.parse(created_at_time).getTime();
				if (System.currentTimeMillis() - longtime < validtime) {
					String[] item = new String[3];
					item[0] = rs.getString("user_id");
					item[1] = rs.getString("idstr");
					item[2] = String.valueOf(rs.getDouble("latitude")) + ","
							+ String.valueOf(rs.getDouble("longitude"));
					retarray.add(item);
				}
			}
			rs.close();
			sql = "TRUNCATE t_status_fresh";
			stmt.execute(sql);
			stmt.close();
			
			return retarray.toArray(new String[retarray.size()][]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
			return new String[0][];
		}
	}

	/**
	 * search nearyby shop,radius is ?
	 * 
	 * @param locinfo
	 *            lat,lng
	 * @return shop's name [0] and access-token [1]
	 */
	public static String[][] searchNearbyShops(String locinfo) {
		double[] loc = LocPaser.parseLoc(locinfo);
		String sql = "select shopid,shopname,access_token from hl_shop_info where (latitude-"
				+ loc[0]
				+ ")*(latitude-"
				+ loc[0]
				+ ")+(longitude-"
				+ loc[1]
				+ ")*(longitude-" + loc[1] + ")<" + default_radius;
		try {
			java.sql.Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			ArrayList<String> retarray0 = new ArrayList<String>();
			ArrayList<String> retarray1 = new ArrayList<String>();
			ArrayList<String> retarray2 = new ArrayList<String>();
			while (rs.next()) {
				retarray0.add(rs.getString("shopid"));
				retarray1.add(rs.getString("shopname"));
				retarray2.add(rs.getString("access_token"));
			}
			rs.close();
			stmt.close();
			
			return new String[][] {
					retarray0.toArray(new String[retarray0.size()]),
					retarray1.toArray(new String[retarray1.size()]),
					retarray2.toArray(new String[retarray2.size()]) };
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
			return new String[0][];
		}
	}

	/**
	 * 
	 * @param shopinfos
	 * @return shop's discounts,have bijection to shops
	 */
	public static String[][] getShopDiscount(String[][] shopinfos) {
		String[] shopids = shopinfos[0];
		String sql = "select text from t_status_info where user_id=?";
		try {
			String[][] ret = new String[shopids.length][];
			PreparedStatement pstmt = conn.prepareStatement(sql);
			for (int i = 0; i < shopids.length; i++) {
				ArrayList<String> discounts = new ArrayList<String>();
				pstmt.setString(1, shopids[i]);
				ResultSet rs = pstmt.executeQuery();
				while (rs.next()) {
					discounts.add(rs.getString("text"));
				}
				ret[i] = new String[1 + discounts.size()];
				ret[i][0] = shopinfos[2][i];// accesstoken;
				for (int j = 1; j < ret[i].length; j++) {
					ret[i][j] = discounts.get(j - 1);
				}
			}
			return ret;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new String[0][];
		}
	}

	/**
	 * maybe not used,system can get access-token by using searchNearbyShops
	 * 
	 * @param shopnames
	 * @return
	 */
	public static String[] getShopAccesstoken(String[] shopnames) {
		String sql = "select access_token from hl_shop_info where shopname='?'";
		try {
			String[] ret = new String[shopnames.length];
			PreparedStatement pstmt = conn.prepareStatement(sql);
			for (int i = 0; i < shopnames.length; i++) {
				pstmt.setString(1, shopnames[i]);
				ResultSet rs = pstmt.executeQuery();
				if (rs.next()) {
					ret[i] = rs.getString("access_token");
				}
			}
			return ret;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			return new String[0];
		}
	}

	public static void execute(String sql) {
		java.sql.Connection conn = ConnectionPool.getInstance().getConnection();
		try {
			java.sql.Statement stmt = conn.createStatement();
			stmt.execute(sql);
			stmt.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * insert statuses into database
	 * @param statuses
	 * @throws SQLException
	 */
	public static void insertStatus(List<Status> statuses) throws SQLException {
		String sql = "INSERT IGNORE INTO t_status_info VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement pstmtStatusInfo = conn.prepareStatement(sql);
		sql = "INSERT IGNORE INTO t_status_fresh VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement pstmtStatusFreshInfo = conn.prepareStatement(sql);
		sql = "INSERT IGNORE INTO t_status_json VALUES(?,?)";
		PreparedStatement pstmtStatusJson = conn.prepareStatement(sql);
		sql = "INSERT IGNORE INTO t_user_status_index VALUES(?,?,?,?)";
		PreparedStatement pstmtUserStatusIndex = conn.prepareStatement(sql);
		for (Status s : statuses) {
			// 获取微博的各个字段信息
			long id = Long.parseLong(s.getId());
			java.sql.Timestamp createdAt = new java.sql.Timestamp(s
					.getCreatedAt().getTime());
			// 微博信息表
			pstmtStatusInfo.setLong(1, id);
			pstmtStatusInfo.setLong(2, Long.parseLong(s.getMid()));
			pstmtStatusInfo.setString(3, String.valueOf(s.getIdstr()));
			pstmtStatusInfo.setTimestamp(4, createdAt);
			pstmtStatusInfo.setString(5, s.getText());
			pstmtStatusInfo.setBoolean(6, s.getSource() != null);
			pstmtStatusInfo.setBoolean(7, s.isFavorited());
			pstmtStatusInfo.setBoolean(8, s.isTruncated());
			pstmtStatusInfo.setLong(9, s.getInReplyToStatusId());
			pstmtStatusInfo.setLong(10, s.getInReplyToUserId());
			pstmtStatusInfo.setString(11, s.getInReplyToScreenName());
			pstmtStatusInfo.setString(12, s.getThumbnailPic());
			pstmtStatusInfo.setString(13, s.getBmiddlePic());
			pstmtStatusInfo.setString(14, s.getOriginalPic());
			pstmtStatusInfo.setString(15, s.getGeo());
			pstmtStatusInfo.setDouble(16, s.getLatitude());
			pstmtStatusInfo.setDouble(17, s.getLongitude());
			pstmtStatusInfo.setLong(18, Long.parseLong(s.getUser().getId()));
			pstmtStatusInfo.setLong(19, s.getRetweetedStatus() == null ? 0L
					: Long.parseLong(s.getRetweetedStatus().getId()));
			pstmtStatusInfo.setLong(20, s.getRepostsCount());
			pstmtStatusInfo.setLong(21, s.getCommentsCount());
			pstmtStatusInfo.setLong(22, 0); // TODO: attitudes_count
			pstmtStatusInfo.setString(23, s.getAnnotations());
			pstmtStatusInfo.setInt(24, s.getMlevel());
			pstmtStatusInfo.setBoolean(25, false); // TODO: visible
			pstmtStatusInfo.setBoolean(26, false);// TODO: pic_urls

			pstmtStatusInfo.addBatch();
			// fresh
			pstmtStatusFreshInfo.setLong(1, id);
			pstmtStatusFreshInfo.setLong(2, Long.parseLong(s.getMid()));
			pstmtStatusFreshInfo.setString(3, String.valueOf(s.getIdstr()));
			pstmtStatusFreshInfo.setTimestamp(4, createdAt);
			pstmtStatusFreshInfo.setString(5, s.getText());
			pstmtStatusFreshInfo.setBoolean(6, s.getSource() != null);
			pstmtStatusFreshInfo.setBoolean(7, s.isFavorited());
			pstmtStatusFreshInfo.setBoolean(8, s.isTruncated());
			pstmtStatusFreshInfo.setLong(9, s.getInReplyToStatusId());
			pstmtStatusFreshInfo.setLong(10, s.getInReplyToUserId());
			pstmtStatusFreshInfo.setString(11, s.getInReplyToScreenName());
			pstmtStatusFreshInfo.setString(12, s.getThumbnailPic());
			pstmtStatusFreshInfo.setString(13, s.getBmiddlePic());
			pstmtStatusFreshInfo.setString(14, s.getOriginalPic());
			pstmtStatusFreshInfo.setString(15, s.getGeo());
			pstmtStatusFreshInfo.setDouble(16, s.getLatitude());
			pstmtStatusFreshInfo.setDouble(17, s.getLongitude());
			pstmtStatusFreshInfo.setLong(18,
					Long.parseLong(s.getUser().getId()));
			pstmtStatusFreshInfo.setLong(
					19,
					s.getRetweetedStatus() == null ? 0L : Long.parseLong(s
							.getRetweetedStatus().getId()));
			pstmtStatusFreshInfo.setLong(20, s.getRepostsCount());
			pstmtStatusFreshInfo.setLong(21, s.getCommentsCount());
			pstmtStatusFreshInfo.setLong(22, 0); // TODO: attitudes_count
			pstmtStatusFreshInfo.setString(23, s.getAnnotations());
			pstmtStatusFreshInfo.setInt(24, s.getMlevel());
			pstmtStatusFreshInfo.setBoolean(25, false); // TODO: visible
			pstmtStatusFreshInfo.setBoolean(26, false);// TODO: pic_urls

			pstmtStatusFreshInfo.addBatch();
			// 原始json串
			pstmtStatusJson.setLong(1, id);
			pstmtStatusJson.setString(2, s.getJson());
			pstmtStatusJson.addBatch();
			
			long statusId = Long.valueOf(s.getId());
			long authorId = Long.valueOf(s.getUser().getId());
			pstmtUserStatusIndex.setLong(1, authorId);
			pstmtUserStatusIndex.setLong(2, statusId);
			pstmtUserStatusIndex.setLong(3, authorId);
			pstmtUserStatusIndex.setTimestamp(4, createdAt);
			pstmtUserStatusIndex.addBatch();
		}
		pstmtUserStatusIndex.executeBatch();
		pstmtUserStatusIndex.close();
		
		pstmtStatusInfo.executeBatch();
		pstmtStatusInfo.close();
		
		pstmtStatusFreshInfo.executeBatch();
		pstmtStatusFreshInfo.close();
		
		pstmtStatusJson.executeBatch();
		pstmtStatusJson.close();
	}

	public static void main(String[] args) throws ParseException {
		long time = dateformat.parse("2013-09-11 10:31:00").getTime();
		Date curdate = new Date(System.currentTimeMillis());
		System.out.println(curdate.getTime()+"\t"+time);
	}
}
