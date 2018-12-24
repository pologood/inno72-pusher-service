package com.inno72.pusher.remoting.common;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.inno72.pusher.model.PusherTaskDaoBean;
import com.inno72.pusher.remoting.ChannelEventListener;
import com.inno72.pusher.remoting.ChannelIdleClear;
import com.inno72.pusher.remoting.ClientSender;
import com.inno72.pusher.remoting.SenderResultHandler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

@Component
public class ClientManager implements ChannelEventListener, ChannelIdleClear, ClientSender {

	private ConcurrentHashMap<Channel, String> channelToKeyMap = new ConcurrentHashMap<Channel, String>();

	private ConcurrentHashMap<Channel, Long> waitToRegisterMap = new ConcurrentHashMap<Channel, Long>();

	private ConcurrentHashMap<String, Channel> keyToChannelMap = new ConcurrentHashMap<String, Channel>();

	private ReentrantLock registerLocker = new ReentrantLock();

	private ReentrantLock waitLocker = new ReentrantLock();

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	

	public boolean registerClient(String key, Channel channel) {

		if (StringUtils.isBlank(key) || channel == null) {
			return false;
		}

		try {
			registerLocker.lock();

			Channel perChannel = keyToChannelMap.get(key);
			if (perChannel != null) {
				try {
					channelToKeyMap.remove(perChannel);
					keyToChannelMap.remove(key);
					RemotingUtil.closeChannel(channel);
				} catch (Exception e) {
				}
			}

			keyToChannelMap.put(key, channel);
			channelToKeyMap.put(channel, key);
		} finally {
			registerLocker.unlock();
		}

		return true;
	}

	public void removeClient(String key) {

		try {
			registerLocker.lock();
			Channel channel = keyToChannelMap.get(key);
			if (channel == null) {
				return;
			}

			try {
				channelToKeyMap.remove(channel);
				keyToChannelMap.remove(key);
				RemotingUtil.closeChannel(channel);
			} catch (Exception e) {
			}

		} finally {
			registerLocker.unlock();
		}

	}

	public void removeClientWithoutClose(Channel channel) {

		if (channel == null) {
			return;
		}

		if (removeWaitChannel(channel)) return;

		try {
			registerLocker.lock();
			String key = channelToKeyMap.get(channel);
			if (key == null) {
				return;
			}

			try {
				channelToKeyMap.remove(channel);
				keyToChannelMap.remove(key);
			} catch (Exception e) {
			}

		} finally {
			registerLocker.unlock();
		}
	}


	public void pushWaitItem(Channel channel) {
		Long time = waitToRegisterMap.get(channel);
		if (time == null) {
			waitToRegisterMap.put(channel, System.currentTimeMillis());
		}
	}

	public boolean pickupWaitToRegister(String key, Channel channel) {

		if (!removeWaitChannel(channel)) {
			return false;
		}

		return registerClient(key, channel);
	}


	public boolean removeWaitChannel(Channel channel) {
		try {
			waitLocker.lock();

			if (waitToRegisterMap.containsKey(channel)) {
				waitToRegisterMap.remove(channel);
				return true;
			} else {
				return false;
			}

		} finally {
			waitLocker.unlock();
		}
	}


	@Override
	public void clearTimeoutChannel(long timeout) {

		long currentTime = System.currentTimeMillis();
		try {
			waitLocker.lock();

			Iterator<Entry<Channel, Long>> it = waitToRegisterMap.entrySet().iterator();

			while (it.hasNext()) {
				Entry<Channel, Long> item = it.next();
				if (currentTime - item.getValue() > timeout) {
					try {
						Channel channel = item.getKey();
						if (channel != null) {
							RemotingUtil.closeChannel(channel);
						}
					} catch (Exception e) {
					}
				}
			}

		} finally {
			waitLocker.unlock();
		}


	}

	@Override
	public void onChannelConnect(String remoteAddr, Channel channel) {
		pushWaitItem(channel);
	}

	@Override
	public void onChannelClose(String remoteAddr, Channel channel) {
		removeClientWithoutClose(channel);
	}

	@Override
	public void onChannelException(String remoteAddr, Channel channel) {
	}

	@Override
	public void onChannelIdle(String remoteAddr, Channel channel) {

	}

	@Override
	public void sendMsg(PusherTaskDaoBean task, SenderResultHandler handler) {

		Channel channel = keyToChannelMap.get(task.getTargetCode());

		if (channel != null) {
			TextWebSocketFrame rspFrame = new TextWebSocketFrame(task.getMessage());
			channel.writeAndFlush(rspFrame).addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					
					logger.info("push isSuccess:{} target:{} msg:{} ", future.isSuccess(), task.getTargetCode(), task.getMessage());
					
					if (handler != null) {
						handler.handleResultHandler(future.isSuccess(), task);
					}
				}
			});
		} else {
			logger.info("push isSuccess:false not conn target:{} msg:{} ", task.getTargetCode(), task.getMessage());
			if (handler != null) {
				handler.handleResultHandler(false, task);
			}
		}

	}

	
	@Override
	public void sendMsgs(List<PusherTaskDaoBean> tasks, SenderResultHandler handler) {
		
		for(PusherTaskDaoBean task : tasks) {
			sendMsg(task, handler);
		}
		
	}

}
