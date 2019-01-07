package com.inno72.pusher.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.inno72.pusher.dto.TargetInfoBean;
import com.inno72.pusher.mapper.PusherInfoDao;
import com.inno72.pusher.model.PusherServiceDaoBean;
import com.inno72.pusher.model.PusherTaskDaoBean;
import com.inno72.pusher.remoting.SenderResultHandler;
import com.inno72.pusher.remoting.common.ClientManager;
import com.inno72.pusher.remoting.common.HttpFormConnector;
import com.inno72.pusher.remoting.common.RemotingHelper;
import com.inno72.pusher.remoting.common.RemotingPostConstruct;

import io.netty.channel.Channel;


@Component
public class PusherTaskService implements RemotingPostConstruct, SenderResultHandler{
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Map<String, String> serviceMap = new HashMap<String, String>();
	
	private ReentrantReadWriteLock serviceMapLocker = new ReentrantReadWriteLock();
	
	@Resource
	private PusherInfoDao pusherInfoDao;
	
	@Resource
	private PushQueues pushQueues;
	
	@Resource(name = "asyncPersistenceExecutor")
	private ExecutorService asyncPersistenceExecutor;
	
	
	@Resource(name = "asyncPublicExecutor")
	private ExecutorService asyncPublicExecutor;
	
	@Resource
	private ClientManager clientManager;
	
	private final Timer timer = new Timer("ResendTaskService", true);
	
	@Override
	public void postBeforeStart() {	
		
		loadServiceMap();
		pushQueues.loadTaskInfos();
		
		this.timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				long currentTime = System.currentTimeMillis();
				
				List<PusherTaskDaoBean> tasks = new LinkedList<PusherTaskDaoBean>();
				
				for(int i=1; i<=3; i++) {
					List<PusherTaskDaoBean> resendTasks = getNeedResendTask(i, currentTime);
					if(resendTasks != null) {
						tasks.addAll(resendTasks);
					}
				}
				
				if(tasks != null && tasks.size() > 0) {
					try {
						clientManager.sendMsgs(tasks, PusherTaskService.this);
					} catch (UnsupportedEncodingException e) {
						logger.warn(e.getMessage(), e);
					}
				}			
			}
		}, 1000 * 10, 5000);
	}

	@Override
	public void postAfterStart() {
		
		
	}
	
	@PreDestroy
	public void stop() {
		timer.cancel();
	}
	
	
	public void loadServiceMap() {
		
		List<PusherServiceDaoBean> pusherServiceDaoBeans = pusherInfoDao.queryServiceMaps();
		
		try {
			serviceMapLocker.writeLock().lock();
			serviceMap.clear();
			if(pusherServiceDaoBeans != null) {
				for(PusherServiceDaoBean item : pusherServiceDaoBeans) {
					serviceMap.put(item.getRequest(), item.getService());
				}
			}
			
		}finally {
			serviceMapLocker.writeLock().unlock();
		}
	}
	
	public String findService(String request) {
		try {
			serviceMapLocker.readLock().lock();
			
			return serviceMap.get(request);
		}finally {
			serviceMapLocker.readLock().unlock();	
		}
	}
	
	
	public List<PusherTaskDaoBean> getNeedResendTask(int level, long time) {
		return pushQueues.getNeedResendTask(level, time);
	}
	
	
	

	@Override
	public void handleResultHandler(boolean isSuccess, PusherTaskDaoBean task) {
		
		final long currentTime = System.currentTimeMillis();
		
		if(isSuccess) {
			
			task.setStatus(1);
			task.setCreateTime(currentTime);
			task.setUpdateTime(currentTime);
			task.setTimes(0);
		
			final PusherTaskDaoBean persistTask = task.clone();
			
			asyncPersistenceExecutor.submit( new Runnable() {
				@Override
				public void run() {
					if(pusherInfoDao.insertPusherTask(persistTask) == 0) {
						pusherInfoDao.updatePusherTaskStatus(persistTask.getId(), 1, currentTime);
					}
				}
			});
			
			
			
		}else {
			final PusherTaskDaoBean failTask = pushQueues.handleFailTask(task);
			
			if(failTask != null) {
				final PusherTaskDaoBean persistTask  = failTask.clone();
				
				asyncPersistenceExecutor.submit( new Runnable() {
					@Override
					public void run() {
						if(pusherInfoDao.insertPusherTask(persistTask) == 0) {
							pusherInfoDao.updatePusherTaskQueueLevel(persistTask.getId(), persistTask.getQueueLevel(), 
									persistTask.getTimes(), persistTask.getUpdateTime());
						}
					}
				});
			}
			
		}
		
		
	}
	
	
	public void handleWithRequest(final String method, final String msgType, final JSONObject param, Channel channel) {
		
		final String url = serviceMap.get(method);
		
		if(StringUtils.isBlank(url)) {
			
			if("machine.register".equalsIgnoreCase(method)) {
				String targetCode = param.getString("targetCode");
				String targetType = param.getString("type");
				if(StringUtils.isNotBlank(targetCode) && StringUtils.isNotBlank(targetType)) {
					clientManager.pickupWaitToRegister(new TargetInfoBean(targetCode, targetType), channel);
					logger.info("register ok targetCode:{} type:{}", targetCode, targetType);
					return;
				}else {
					logger.warn("register fail not found targetCode or type");
				}
			}
			
			logger.warn("not found can support method:" + method);
			return;
		}
		
		
		asyncPublicExecutor.submit( new Runnable() {
			
			@Override
			public void run() {
				if(url != null && param != null) {
					try {
						
						param.put("clientIp", RemotingHelper.parseChannelRemoteAddr(channel));
						
						TargetInfoBean targetInfo = clientManager.getTargetInfo(channel);
						Map<String, String> header = null;
						if(targetInfo != null) {
							header = new HashMap<String, String>();
							header.put("TargetCode", targetInfo.getTargetCode());
							header.put("TargetType", targetInfo.getTargetType());
						}
						
						if(StringUtils.isNotBlank(msgType)) {
							if(header != null) {
								header.put("MsgType", msgType);
							}else {
								header = new HashMap<String, String>();
								header.put("MsgType", msgType);
							}
						}
						
						byte[] res = HttpFormConnector.doPostJson(url, param, header, 1000);
						
						String resStr = new String(res);
						
						logger.info("method:{} param:{} ret:{}", method, param.toJSONString(), resStr);
						
						if("machine.register".equalsIgnoreCase(method)) {
							
							JSONObject retJson = JSON.parseObject(resStr);
							
							String targetCode = retJson.getJSONObject("data").getString("targetCode");
							String targetType = retJson.getJSONObject("data").getString("type");
							
							if(retJson.getIntValue("code") == 0 && StringUtils.isNotBlank(targetCode) && StringUtils.isNotBlank(targetType)) {
								clientManager.pickupWaitToRegister(new TargetInfoBean(targetCode, targetType), channel);
								logger.info("register ok targetCode:{} type:{}", targetCode, targetType);
							}else {
								logger.warn("register fail not found targetCode or type");
							}
						}
						
					} catch (IOException e) {
						logger.error(e.getMessage(), e);
					}
				}
				
			}
		});
	}
	
}
