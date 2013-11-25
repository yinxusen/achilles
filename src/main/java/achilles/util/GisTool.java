package achilles.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import achilles.frontend.query.common.LocAddress;
import achilles.frontend.query.common.Properties;
import achilles.frontend.query.server.sqldb.DB;

public class GisTool {
	private static HttpClient client = new HttpClient();
	// private static final String v1prefix =
	// "http://api.map.baidu.com/geocoder?";
	private static final String v2prefix = "http://api.map.baidu.com/geocoder/v2/?";
	// private static final String v1end =
	// "&output=xml&key=ba376eda799ae16ffb8492c9b44af443";
	private static final String v2end = "&output=xml&ak=F77a4a711faf58562479885ddae9b560";

	private static final String locxmlpattern = "[.\\s]*<lat>(.*)</lat>[.\\s]*<lng>(.*)</lng>[.\\s]*";
	private static final Pattern locp = Pattern.compile(locxmlpattern);
	private static final String addxmlpattern = ".*<formatted_address>(.*)</formatted_address>.*<business>(.*)</business>.*<district>(.*)</district>.*<city>(.*)</city>.*<province>(.*)</province>.*";
	private static final Pattern addp = Pattern.compile(addxmlpattern);

	public static void main(String[] args) throws UnsupportedEncodingException {
//		System.out.println(GisTool.getAddrByLoc("39.988379880220,116.321319424311").getAddr_name());
//		System.out.println(GisTool.getLocByAddrName(str2hex("")));
		for (double i = Properties.southlat; i < Properties.northlat; i += 0.0001) {
			for (double j = Properties.westlng; j < Properties.eastlng; j += 0.0001) {
				String locinfo =  i+ "," + j;
				LocAddress addr = GisTool.getAddrByLoc(locinfo);
				if (addr != null) {
					if (addr.getAddr_name().equals(""))
						continue;
					else {
						System.out.println("addr is not null");
						String sql = "insert into test_gis SET shopname='"
								+ addr.getAddr_name() + "',longitude=" + j
								+ ",latitude=" + i;
						DB.execute(sql);
					}
				}
//				 System.out.println(locinfo+"\t"+DB.queryShopNameByLoc(locinfo)[0]);
			}
		}
	}

	/**
	 * 
	 * @param locinfo
	 *            lat,lng
	 * @return
	 */
	public static LocAddress getAddrByLoc(String locinfo) {
		String uri = v2prefix + "location=" + locinfo + v2end;
		GetMethod gmethod = new GetMethod(uri);
		gmethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 3000);
		try {
			int status = client.executeMethod(gmethod);
			if (status != 200)
				return null;
			else {
				return parseAddrName(gmethod.getResponseBodyAsString(1000));
			}
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			System.err.println(uri + ":\n" + e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(uri + ":\n" + e.getMessage());
		}
		return null;
	}

	/**
	 * get addrname from xml
	 * 
	 * @param xml
	 * @return
	 */
	public static LocAddress parseAddrName(String xml) {
		xml = xml.replaceAll("\\s", "");
		Matcher m = addp.matcher(xml);
		if (m.find()) {
			return new LocAddress(m.group(5), m.group(4), m.group(1),
					m.group(2), m.group(3));
		} else
			return null;
	}

	public static String getLocByAddrName(String addrname) {
		String uri = v2prefix + "address=" + addrname + v2end;
		GetMethod gmethod = new GetMethod(uri);
		gmethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 3000);
		try {
			int status = client.executeMethod(gmethod);
			if (status != 200)
				return null;
			else {
				String xml = gmethod.getResponseBodyAsString(1000);
				Matcher m = locp.matcher(xml);
				if (m.find())
					return m.group(1) + "," + m.group(2);
				else
					return null;
			}
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			System.err.println(uri + ":\n" + e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(uri + ":\n" + e.getMessage());
		}
		return null;
	}

	public static String str2hex(String str) {
		char[] digital = "0123456789ABCDEF".toCharArray();
		StringBuffer sb = new StringBuffer("");
		byte[] bs = str.getBytes();
		int bit;
		for (int i = 0; i < bs.length; i++) {
			bit = (bs[i] & 0x0f0) >> 4;
			sb.append("%" + digital[bit]);
			bit = bs[i] & 0x0f;
			sb.append(digital[bit]);
		}
		return sb.toString();
	}
}
