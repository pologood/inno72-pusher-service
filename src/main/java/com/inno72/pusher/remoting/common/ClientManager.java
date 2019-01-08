package com.inno72.pusher.remoting.common;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.inno72.pusher.dto.TargetInfoBean;
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

	private ConcurrentHashMap<Channel, TargetInfoBean> channelToKeyMap = new ConcurrentHashMap<Channel, TargetInfoBean>();

	private ConcurrentHashMap<Channel, Long> waitToRegisterMap = new ConcurrentHashMap<Channel, Long>();

	private ConcurrentHashMap<TargetInfoBean, Channel> keyToChannelMap = new ConcurrentHashMap<TargetInfoBean, Channel>();

	private ReentrantReadWriteLock registerLocker = new ReentrantReadWriteLock();

	private ReentrantReadWriteLock waitLocker = new ReentrantReadWriteLock();

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	

	protected boolean registerClient(TargetInfoBean key, Channel channel) {

		if (key == null || StringUtils.isBlank(key.getTargetCode()) || StringUtils.isBlank(key.getTargetType()) || channel == null) {
			return false;
		}

		try {
			registerLocker.writeLock().lock();

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
			registerLocker.writeLock().unlock();
		}

		return true;
	}

	public void removeClient(TargetInfoBean key) {

		try {
			registerLocker.writeLock().lock();
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
			registerLocker.writeLock().unlock();
		}

	}

	public void removeClientWithoutClose(Channel channel) {

		if (channel == null) {
			return;
		}

		if (removeWaitChannel(channel)) return;

		try {
			registerLocker.writeLock().lock();
			TargetInfoBean key = channelToKeyMap.get(channel);
			if (key == null) {
				return;
			}

			try {
				channelToKeyMap.remove(channel);
				keyToChannelMap.remove(key);
			} catch (Exception e) {
			}

		} finally {
			registerLocker.writeLock().unlock();
		}
	}


	public void pushWaitItem(Channel channel) {
		Long time = waitToRegisterMap.get(channel);
		if (time == null) {
			waitToRegisterMap.put(channel, System.currentTimeMillis());
		}
	}

	public boolean pickupWaitToRegister(TargetInfoBean key, Channel channel) {

		if (!removeWaitChannel(channel)) {
			return false;
		}

		return registerClient(key, channel);
	}


	public boolean removeWaitChannel(Channel channel) {
		try {
			waitLocker.writeLock().lock();

			if (waitToRegisterMap.containsKey(channel)) {
				waitToRegisterMap.remove(channel);
				return true;
			} else {
				return false;
			}

		} finally {
			waitLocker.writeLock().unlock();
		}
	}


	@Override
	public void clearTimeoutChannel(long timeout) {

		long currentTime = System.currentTimeMillis();
		try {
			waitLocker.writeLock().lock();

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
			waitLocker.writeLock().unlock();
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
	public void sendMsg(PusherTaskDaoBean task, SenderResultHandler handler) throws UnsupportedEncodingException {

		Channel channel = keyToChannelMap.get(task.getTargetInfo());

		if (channel != null) {
			TextWebSocketFrame rspFrame = new TextWebSocketFrame(new String(task.getMessage(), "utf-8"));
			channel.writeAndFlush(rspFrame).addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					
					logger.info("push isSuccess:{} target:{}", future.isSuccess(), task.getTargetInfo());
					
					if (handler != null) {
						handler.handleResultHandler(future.isSuccess(), task);
					}
				}
			});
		} else {
			logger.info("push isSuccess:false not conn target:{}", task.getTargetInfo());
			if (handler != null) {
				handler.handleResultHandler(false, task);
			}
		}

	}

	@Override
	public void sendMsgs(List<PusherTaskDaoBean> tasks, SenderResultHandler handler) throws UnsupportedEncodingException{
		
		for(PusherTaskDaoBean task : tasks) {
			sendMsg(task, handler);
		}
		
	}
	
	
	public TargetInfoBean getTargetInfo(Channel channel) {
		
		return channelToKeyMap.get(channel);
		
	}
	
	
	public List<Pair<String, TargetInfoBean>> getKeyChannelMap(){
		
		List<Pair<String, TargetInfoBean>> ret = new LinkedList<Pair<String, TargetInfoBean>>();
		
		Enumeration<TargetInfoBean> keys = keyToChannelMap.keys();
		
		while(keys.hasMoreElements()) {
			TargetInfoBean key = keys.nextElement();
			
			ret.add(new Pair<String, TargetInfoBean>(RemotingHelper.parseChannelRemoteAddr(keyToChannelMap.get(key)), key));
		}
		
		return ret;
	}
	
	
	public boolean kickOffChannel(TargetInfoBean targetInfo) {
		
		Channel channel = keyToChannelMap.get(targetInfo);
		
		if(channel != null) {
			RemotingUtil.closeChannel(channel);
			
			return true;
		}
		
		return false;
		
	}
	
}
