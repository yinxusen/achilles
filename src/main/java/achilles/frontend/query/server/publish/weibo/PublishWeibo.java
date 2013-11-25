package achilles.frontend.query.server.publish.weibo;

import java.util.List;

import weibo4j.Comments;
import weibo4j.Timeline;
import weibo4j.model.Status;
import weibo4j.model.StatusWapper;
import weibo4j.model.WeiboException;

public class PublishWeibo {
	public static final String author_access_token = "2.00CnssoD9c9W7D1048e4221b00tK9p";
	String test_statuscontent = "test weibo api to publish status";
	public static boolean publish(String access_token,String statuscontent){
		Timeline tm = new Timeline();
		tm.client.setToken(access_token);
		try {
			Status status = tm.UpdateStatus(statuscontent);
			if(status.getText().equals(statuscontent))
				return true;
			else
				return false;
		} catch (WeiboException e) {
			return false;
		}
	}
	public static boolean comment(String access_token,String commentcontent,String statusid){
		Comments cms = new Comments();
		cms.setToken(access_token);
		try {
			cms.createComment(commentcontent, statusid);
			return true;
		} catch (WeiboException e) {
			// TODO Auto-generated catch block
			return false;
		}
	}
	public static void main(String[] args) {
		Timeline tm = new Timeline();
		tm.client.setToken(author_access_token);
		try {
			StatusWapper statuswapper = tm.getUserTimeline();
			List<Status> statuses = statuswapper.getStatuses();
			for(Status s:statuses){
				Comments cm  = new Comments();
				cm.setToken(author_access_token);
				cm.createComment("test1", s.getId());
			}
		} catch (WeiboException e) {
			e.printStackTrace();
		}
	}
}
