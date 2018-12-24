package com.inno72.pusher.remoting;

import com.inno72.pusher.model.PusherTaskDaoBean;

public interface SenderResultHandler {
	
	void handleResultHandler(boolean isSuccess, PusherTaskDaoBean task);

}
