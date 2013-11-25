package achilles.frontend.query.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.UnknownHostException;

public class QueryConsole {
	public static void main(String[] args) throws UnknownHostException, IOException, Exception {
		DataInputStream dis = new DataInputStream(System.in);
		while(true){
			@SuppressWarnings("deprecation")
			String cmd = dis.readLine();
			String[] cmdargs = cmd.split(" ");
			if (cmdargs[0].equals("querydiscount")) {
				if (cmdargs[1].equals("byfile")) {
					String result = new DiscountQueryClient("localhost",1000).getDiscountByFile(cmdargs[2], cmdargs[3]);
					System.out.println("result file: "+result);
				}else {
					try{
						String[] result = new DiscountQueryClient("localhost",1000).queryDiscountByLoc(cmdargs[1],Integer.valueOf(cmdargs[2]));
						for(int i=0;i<result.length;i++)
							System.out.println(result[i]);
					}catch(Exception e){
						System.err.println("usage: querydiscount locinfo needrecordnum");
						System.err.println("or usage: querydiscount byfile locfile destfile");
					}
				}
			}else{
				System.err.println("usage: querydiscount byrecord locinfo needrecordnum");
				System.err.println("or usage: querydiscount byfile locfile destfile");
			}
		}
	}
}
