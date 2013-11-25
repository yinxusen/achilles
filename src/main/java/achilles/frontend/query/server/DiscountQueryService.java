package achilles.frontend.query.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import achilles.frontend.query.common.Properties;
import achilles.frontend.query.server.publish.weibo.PublishWeibo;
import achilles.frontend.query.server.sqldb.DB;

public class DiscountQueryService {
	public static boolean toStop = false;
	// query shop discount by loc
	public static String[][] lQuery(String locinfo,int neednum) {
		String[][] shopinfos = DB.searchNearbyShops(locinfo);
		if(shopinfos.length<=0)
			return new String[0][];
		return DB.getShopDiscount(shopinfos);
	}
	//query by userid
	public static String[][] uQuery(String userid,int neednum){
		return new String[0][];
	}
	
	public static String[][] sQuery(String statuscontext,int neednum){
//		return DB.queryShopDiscountByStatus(statuscontext, neednum);
		return new String[0][];
	}
	
	public static String[][] landsQuery(String locinfo,String statuscontext,int neednum){
		return new String[0][];
	}
	
	public static String[][] landuQuery(String locinfo ,String userid,int neednum){
		return new String[0][];
	}

	public static String[][][] queryDiscountbylocs(String[] locs,int neednum) {
		String[][][] ret = new String[locs.length][][];
		for (int i = 0; i < locs.length; i++) {
			ret[i] = lQuery(locs[i],neednum);
		}
		return ret;
	}

	/**
	 * this method read sourcefile from sourcefilepath write destfile to
	 * destfilepath
	 * 
	 * @param sourcefilepath
	 * @param destfilepath
	 * @throws IOException
	 */
	public static void queryDiscount(String sourcefilepath, String destfilepath)
			throws IOException {
		File sourcefile = new File(sourcefilepath);
		DataInputStream dis = new DataInputStream(new FileInputStream(
				sourcefile));
		File destfile = new File(destfilepath);
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(
				destfile));

		while (dis.available() > 0) {
			@SuppressWarnings("deprecation")
			String sourceloc = dis.readLine();
			String[][] result = lQuery(sourceloc,Properties.default_need_num);
			dos.writeUTF(sourceloc);
			dos.writeInt(result.length);
			for (String[] disinfos : result)
				for(String disinfo:disinfos)
					dos.writeUTF(disinfo);
		}
		dis.close();
		dos.close();
	}

	public static class QueryServer {
		ServerSocket serversocket = null;

		public QueryServer(int serverport) throws IOException {
			serversocket = new ServerSocket(serverport);
		}

		private static void handler(Socket client) throws IOException {
			DataInputStream dis = new DataInputStream(client.getInputStream());
			DataOutputStream dos = new DataOutputStream(
					client.getOutputStream());
			int flag = dis.readInt();
			if (flag == Properties.querydiscountbyloc) {
				int neednum = dis.readInt();
				String locinfo = dis.readUTF();
				String[][] result = lQuery(locinfo,neednum);
				if (result == null)
					dos.writeInt(0);
				else {
					dos.writeInt(result.length);
					for (String[] disinfos : result)
						for(String disinfo:disinfos)
							dos.writeUTF(disinfo);
				}
			}else if (flag == Properties.querydiscountbystatus) {
				int neednum = dis.readInt();
				String statuscontext = dis.readUTF();
				String[][] result = sQuery(statuscontext,neednum);
				if (result == null)
					dos.writeInt(0);
				else {
					dos.writeInt(result.length);
					for (String[] disinfos : result)
						for(String disinfo:disinfos)
							dos.writeUTF(disinfo);
				}
			}else if (flag == Properties.querydiscountbyuserid) {
				int neednum = dis.readInt();
				String userid = dis.readUTF();
				String[][] result = uQuery(userid,neednum);
				if (result == null)
					dos.writeInt(0);
				else {
					dos.writeInt(result.length);
					for (String[] disinfos : result)
						for(String disinfo:disinfos)
							dos.writeUTF(disinfo);
				}
			}else if (flag == Properties.querydiscountbyLandS) {
				int neednum = dis.readInt();
				String locinfo = dis.readUTF();
				String statuscontext = dis.readUTF();
				String[][] result = landsQuery(locinfo,statuscontext,neednum);
				if (result == null)
					dos.writeInt(0);
				else {
					dos.writeInt(result.length);
					for (String[] disinfos : result)
						for(String disinfo:disinfos)
							dos.writeUTF(disinfo);
				}
			}else if (flag == Properties.querydiscountbyLandU) {
				int neednum = dis.readInt();
				String locinfo = dis.readUTF();
				String userid = dis.readUTF();
				String[][] result = landuQuery(locinfo,userid,neednum);
				if (result == null)
					dos.writeInt(0);
				else {
					dos.writeInt(result.length);
					for (String[] disinfos : result)
						for(String disinfo:disinfos)
							dos.writeUTF(disinfo);
				}
			}else if(flag == Properties.queryperfile){
				String locfilepath = dis.readUTF();
				String destfilepath = dis.readUTF();
				DiscountQueryService.queryDiscount(locfilepath, destfilepath);
				dos.writeUTF(destfilepath);
			}else
				;
			dis.close();
			dos.close();
		}

		public void service() {
			// TODO Auto-generated method stub
			while (true) {
				
				try {
					final Socket client = serversocket.accept();
					if(toStop)
						break;
					new Thread() {
						public void run() {
							try {
								QueryServer.handler(client);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								System.out
										.println("query handler exception: client="
												+ client.getInetAddress()
												+ "\ttime="
												+ new Date()
												+ "\n"
												+ e.getMessage());
							}
						}
					}.start();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out
							.println("query server - client handler exception:"
									+ e.getMessage());
				}
				if(toStop)
					break;
			}
		}
	}
	public static void recommend(){
		String[][] newstatuslocs = DB.getLatestStatus();
		//info format: info[0] is user_id ,info[1] is status_id,info[2] is locinfo.
		for(String[] locinfos : newstatuslocs){
			if(locinfos.length!=3)
				;
			else{
//				String userid = locinfos[0]; //for publish
				String status_id = locinfos[1];
				String locinfo = locinfos[2];
				String[][] results = DiscountQueryService.lQuery(locinfo, Properties.default_need_num);
				boolean pubresult = true;
				for(int i=0;i<results.length;i++){
					if(results[i].length>=2){
						String access_token = results[i][0];
						results[i][0] = "";
						String weiboinfo = "";
						for(int j=1;j<results[i].length;j++)
							weiboinfo+=results[i][j]+"\n";
						pubresult = PublishWeibo.comment(access_token, weiboinfo, status_id)&pubresult;
					}
				}
				System.out.println("comment result:"+pubresult);
			}
		}
	}
	public static void main(String[] args) throws IOException {
	}
}
