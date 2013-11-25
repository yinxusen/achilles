package achilles.frontend.query.server;

import achilles.frontend.query.common.Properties;
import achilles.frontend.query.server.publish.weibo.PublishWeibo;
import achilles.frontend.query.server.sqldb.DB;

public class Daemon implements Runnable{
	private static final int default_sleep_time = 5000; //5 min
	public Daemon(){
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			System.out.println("**************************new task************************");
			String[][] newstatuslocs = DB.getLatestStatus();
			//info format: info[0] is user_id ,info[1] is status_id,info[2] is locinfo.
			for(String[] locinfos : newstatuslocs){
				if(locinfos.length!=3)
					;
				else{
//					String userid = locinfos[0]; //for publish
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
			try {
				Thread.sleep(default_sleep_time);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args) {
		new Daemon().run();
//		String[] a ={"a"};
//		String[] b = a;
//		a[0] = "changed a";
//		System.out.println(a[0]+"\t"+b[0]);
	}
}
