package com.inno72.pusher.service;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.inno72.pusher.mapper.PusherInfoDao;
import com.inno72.pusher.model.PusherTaskDaoBean;

@Component
public class PushQueues {
	
	
	private final int FIRST_LEVEL_QUEUE_TIMES = 5;
	
	
	private final int SECOND_LEVEL_QUEUE_TIMES = 5;
	
	
	private final int THIRD_LEVEL_QUEUE_TIMES = 5;
	
	
	private final long FIRST_LEVEL_QUEUE_DURATION = 1000*5;
	
	
	private final long SECOND_LEVEL_QUEUE_DURATION = 1000*60*5;
	
	
	private final long THIRD_LEVEL_QUEUE_DURATION = 1000*60*60*5;
	
	
	private BlockingQueue<PusherTaskDaoBean> firstLevelQueue = new LinkedBlockingQueue<PusherTaskDaoBean>();
	
	
	private BlockingQueue<PusherTaskDaoBean> secondLevelQueue = new LinkedBlockingQueue<PusherTaskDaoBean>();
	
	
	private BlockingQueue<PusherTaskDaoBean> thirdLevelQueue = new LinkedBlockingQueue<PusherTaskDaoBean>();
	
	
	
	public List<PusherTaskDaoBean> getNeedResendTask(int level, long time) {
		
		
		BlockingQueue<PusherTaskDaoBean> queue = null;
		long duration = 0;
		
		switch(level) {
			case 1: queue = firstLevelQueue; duration=FIRST_LEVEL_QUEUE_DURATION; break;
			case 2: queue = secondLevelQueue; duration=SECOND_LEVEL_QUEUE_DURATION; break;
			case 3: queue = thirdLevelQueue; duration=THIRD_LEVEL_QUEUE_DURATION; break;
			default:
				return null;
			
		}
		
		List<PusherTaskDaoBean> tasks = new LinkedList<PusherTaskDaoBean>();
		
		queue.drainTo(tasks);
		
		Iterator<PusherTaskDaoBean> it = tasks.iterator();
		
		while(it.hasNext()){
			PusherTaskDaoBean task = it.next();
		    if(task.getUpdateTime() + duration > time) {
		    	queue.add(task);
		    	it.remove();
		    }
		}	
		return tasks;
	}

	
	@Resource
	private PusherInfoDao pusherInfoDao;
	
	public void loadTaskInfos() {
		
		List<PusherTaskDaoBean> pusherTaskDaoList = pusherInfoDao.queryAvailable();
		
		if(pusherTaskDaoList != null && pusherTaskDaoList.size() > 0) {
			for(PusherTaskDaoBean item : pusherTaskDaoList) {
				
				if(item.getQueueLevel() == 1) {
					firstLevelQueue.add(item);
				}else if(item.getQueueLevel() == 2) {
					secondLevelQueue.add(item);
				}else if(item.getQueueLevel() == 3) {
					thirdLevelQueue.add(item);
				}
			}
		}
	}
	
	
	public PusherTaskDaoBean handleFailTask(PusherTaskDaoBean task) {
		
		long currentTime = System.currentTimeMillis();
		
		if(task.getQueueLevel() == 0) {
			task.setQueueLevel(1);
			task.setTimes(0);
			task.setUpdateTime(currentTime);
			firstLevelQueue.add(task);
			return task;
		}else if(task.getQueueLevel() == 1){
			if(task.getTimes() >= FIRST_LEVEL_QUEUE_TIMES) {
				task.setQueueLevel(2);
				task.setTimes(0);
				task.setUpdateTime(currentTime);
				secondLevelQueue.add(task);
			}else {
				task.setTimes(task.getTimes()+1);
				task.setUpdateTime(currentTime);
				firstLevelQueue.add(task);
			}
			return task;
			
		}else if(task.getQueueLevel() == 2){
			if(task.getTimes() >= SECOND_LEVEL_QUEUE_TIMES) {
				task.setQueueLevel(3);
				task.setTimes(0);
				task.setUpdateTime(currentTime);
				thirdLevelQueue.add(task);
			}else {
				task.setTimes(task.getTimes()+1);
				task.setUpdateTime(currentTime);
				secondLevelQueue.add(task);
			}
			return task;
			
		}else if(task.getQueueLevel() == 3){
			if(task.getTimes() >= THIRD_LEVEL_QUEUE_TIMES) {
				task.setQueueLevel(4);
				task.setTimes(0);
				task.setUpdateTime(currentTime);
			}else {
				task.setTimes(task.getTimes()+1);
				task.setUpdateTime(currentTime);
				thirdLevelQueue.add(task);
			}
			return task;
		}
		
		
		return null;
	}
	

}
