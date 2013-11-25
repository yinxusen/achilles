package achilles.frontend.query.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import achilles.frontend.query.common.Properties;

public class DiscountQueryClient {
	Socket client = null;

	public DiscountQueryClient(String serverip, int serverport)
			throws UnknownHostException, IOException {
		client = new Socket(serverip, serverport);
	}
	/**
	 * query discount by location
	 * @param locinfo like 1,2 longtitude=1,latitude=2
	 * @param neednum
	 * @return
	 * @throws Exception
	 */
	public String[] queryDiscountByLoc(String locinfo,int neednum) throws Exception{
		if (client == null)
			throw new Exception("client == null");
		DataOutputStream dos = new DataOutputStream(client.getOutputStream());
		DataInputStream dis = new DataInputStream(client.getInputStream());
		dos.writeInt(Properties.querydiscountbyloc);
		dos.writeInt(neednum);
		dos.writeUTF(locinfo);
		int resultlen = dis.readInt();
		String[] result = new String[resultlen];
		for (int i = 0; i < resultlen; i++)
			result[i] = dis.readUTF();
		dos.close();
		dis.close();
		return result;
	}
	/**
	 * query discount by userid.
	 * @param userid
	 * @param neednum
	 * @return
	 * @throws Exception
	 */
	public String[] queryDiscountByUserId(String userid,int neednum) throws Exception{
		if (client == null)
			throw new Exception("client == null");
		DataOutputStream dos = new DataOutputStream(client.getOutputStream());
		DataInputStream dis = new DataInputStream(client.getInputStream());
		dos.writeInt(Properties.querydiscountbyuserid);
		dos.writeInt(neednum);
		dos.writeUTF(userid);
		int resultlen = dis.readInt();
		String[] result = new String[resultlen];
		for (int i = 0; i < resultlen; i++)
			result[i] = dis.readUTF();
		dos.close();
		dis.close();
		return result;
	}
	/**
	 * query discount by userid and locinfo ,return neednum result.
	 * @param userid
	 * @param locinfo
	 * @param neednum
	 * @return
	 * @throws Exception
	 */
	public String[] queryDiscountByLandU(String locinfo,String userid,int neednum) throws Exception {
		if (client == null)
			throw new Exception("client == null");
		DataOutputStream dos = new DataOutputStream(client.getOutputStream());
		DataInputStream dis = new DataInputStream(client.getInputStream());
		dos.writeInt(Properties.querydiscountbyLandU);
		dos.writeInt(neednum);
		dos.writeUTF(locinfo);
		dos.writeUTF(userid);
		int resultlen = dis.readInt();
		String[] result = new String[resultlen];
		for (int i = 0; i < resultlen; i++)
			result[i] = dis.readUTF();
		dos.close();
		dis.close();
		return result;
	}
	/**
	 * query by status text, follow user's interest
	 * @param statustext
	 * @param neednum
	 * @return
	 * @throws Exception
	 */
	public String[] queryDiscountByStatus(String statustext,int neednum) throws Exception{
		if (client == null)
			throw new Exception("client == null");
		DataOutputStream dos = new DataOutputStream(client.getOutputStream());
		DataInputStream dis = new DataInputStream(client.getInputStream());
		dos.writeInt(Properties.querydiscountbystatus);
		dos.writeInt(neednum);
		dos.writeUTF(statustext);
		int resultlen = dis.readInt();
		String[] result = new String[resultlen];
		for (int i = 0; i < resultlen; i++)
			result[i] = dis.readUTF();
		dos.close();
		dis.close();
		return result;
	}
	/**
	 * query by loc and status.
	 * @param statustext
	 * @param locinfo
	 * @param neednum
	 * @return
	 * @throws Exception
	 */
	public String[] queryDiscountByLandS(String locinfo,String statustext,int neednum) throws Exception{
		if (client == null)
			throw new Exception("client == null");
		DataOutputStream dos = new DataOutputStream(client.getOutputStream());
		DataInputStream dis = new DataInputStream(client.getInputStream());
		dos.writeInt(Properties.querydiscountbyLandS);
		dos.writeInt(neednum);
		dos.writeUTF(locinfo);
		dos.writeUTF(statustext);
		int resultlen = dis.readInt();
		String[] result = new String[resultlen];
		for (int i = 0; i < resultlen; i++)
			result[i] = dis.readUTF();
		dos.close();
		dis.close();
		return result;
	}
	/**
	 * query discount by file.
	 * @param locfilepath
	 * @param destfilepath
	 * @return
	 * @throws Exception
	 */
	public String getDiscountByFile(String locfilepath, String destfilepath)
			throws Exception {
		if (client == null)
			throw new Exception("client == null");
		DataOutputStream dos = new DataOutputStream(client.getOutputStream());
		DataInputStream dis = new DataInputStream(client.getInputStream());
		dos.writeInt(Properties.queryperfile);
		dos.writeUTF(locfilepath);
		dos.writeUTF(destfilepath);
		String result = dis.readUTF();
		dis.close();
		dos.close();
		return result;
	}
}
