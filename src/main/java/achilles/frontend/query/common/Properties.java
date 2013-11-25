package achilles.frontend.query.common;

public class Properties {
	public static final String dburl = "jdbc:mysql://localhost/weibo";
	public static final String dbusername = "root";
	public static final String dbpassword = "root";
	
	//hailong region
	public static final Double northlat = 39.990062263151;
	public static final Double westlng = 116.321319424311;
	public static final Double southlat = 39.988379880220;
	public static final Double eastlng = 116.323185090565;
	//other
	public static int default_need_num = 2;
	
	public static final int queryperfile = 1;
	public static final int queryperrec = 2;
	public static final int querydiscountbyloc = 10;
	public static final int querydiscountbyuserid = 11;
	public static final int querydiscountbyLandU = 12; //query by loc and userid
	public static final int querydiscountbystatus = 13;
	public static final int querydiscountbyLandS = 14;
	
	public static final long validPeriod = 30*60*1000; //30min
}
