package com.inno72.pusher.remoting;

import java.util.List;

import com.inno72.pusher.model.PusherTaskDaoBean;

public interface ClientSender {
	
	void sendMsg(PusherTaskDaoBean task, SenderResultHandler handler);
	
	void sendMsgs(List<PusherTaskDaoBean> tasks, SenderResultHandler handler);

}
