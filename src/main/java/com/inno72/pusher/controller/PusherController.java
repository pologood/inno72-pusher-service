package com.inno72.pusher.controller;

import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inno72.common.Result;
import com.inno72.pusher.dto.PushMultiToMultiBean;
import com.inno72.pusher.dto.PushMultiToOneBean;
import com.inno72.pusher.dto.PushOneBean;
import com.inno72.pusher.model.PusherTaskDaoBean;
import com.inno72.pusher.remoting.common.ClientManager;
import com.inno72.pusher.service.IdWorker;
import com.inno72.pusher.service.PusherTaskService;

@RestController
@RequestMapping("/pusher")
public class PusherController {
	

	@Resource
	private ClientManager  clientManager;
	
	@Resource
	private PusherTaskService pusherTaskService;
	
	
	@Resource
	private IdWorker idWorker;
	
	@RequestMapping(value="/push/one")
	public  Result<Void> pushOne(@RequestBody PushOneBean reqBean){
		
		if(StringUtils.isBlank(reqBean.getMachineCode()) || StringUtils.isBlank(reqBean.getData())) {
			
			Result<Void> rspBean = new Result<Void>();
			rspBean.setCode(-1);
			rspBean.setMsg("param error");
			
			return rspBean;
			
		}
		
		long currentTime = System.currentTimeMillis();
		
		PusherTaskDaoBean bean = new PusherTaskDaoBean();
		
		bean.setCreateTime(currentTime);
		bean.setUpdateTime(currentTime);
		bean.setId(idWorker.nextId());
		bean.setTargetCode(reqBean.getMachineCode());
		bean.setMessage(reqBean.getData());
		
		if(reqBean.getIsQueue() == 1) {	
			clientManager.sendMsg(bean, pusherTaskService);
		}else {
			clientManager.sendMsg(bean, null);
		}
		
		
		Result<Void> rspBean = new Result<Void>();
		rspBean.setCode(0);
		rspBean.setMsg("ok");
		
		return rspBean;
		
	}
	
	@RequestMapping(value="/push/multi/multi")
	public  Result<Void> pushMultiToMulti(@RequestBody PushMultiToMultiBean reqBean){
		
		if(reqBean.getPeers() == null) {
			
			Result<Void> rspBean = new Result<Void>();
			rspBean.setCode(-1);
			rspBean.setMsg("param error");
			
			return rspBean;
			
		}
		
		Set<Map.Entry<String, String>> peers = reqBean.getPeers().entrySet();
		
		long currentTime = System.currentTimeMillis();
		
		for(Map.Entry<String, String> peer : peers) {
		
			PusherTaskDaoBean bean = new PusherTaskDaoBean();
			
			bean.setCreateTime(currentTime);
			bean.setUpdateTime(currentTime);
			bean.setId(idWorker.nextId());
			bean.setTargetCode(peer.getKey());
			bean.setMessage(peer.getValue());
			
			if(reqBean.getIsQueue() == 1) {	
				clientManager.sendMsg(bean, pusherTaskService);
			}else {
				clientManager.sendMsg(bean, null);
			}
		}
		
		Result<Void> rspBean = new Result<Void>();
		rspBean.setCode(0);
		rspBean.setMsg("ok");
		
		return rspBean;
	}
	
	
	@RequestMapping(value="/push/multi/one")
	public  Result<Void> pushMultiToOne(@RequestBody PushMultiToOneBean reqBean){
		
		if(reqBean.getMachineCodes() == null ||  reqBean.getMachineCodes().isEmpty() ||  StringUtils.isBlank(reqBean.getData())) {
			
			Result<Void> rspBean = new Result<Void>();
			rspBean.setCode(-1);
			rspBean.setMsg("param error");
			
			return rspBean;
			
		}
			
		long currentTime = System.currentTimeMillis();
		
		for(String machineCode : reqBean.getMachineCodes()) {
		
			PusherTaskDaoBean bean = new PusherTaskDaoBean();
			
			bean.setCreateTime(currentTime);
			bean.setUpdateTime(currentTime);
			bean.setId(idWorker.nextId());
			bean.setTargetCode(machineCode);
			bean.setMessage(reqBean.getData());
			
			if(reqBean.getIsQueue() == 1) {	
				clientManager.sendMsg(bean, pusherTaskService);
			}else {
				clientManager.sendMsg(bean, null);
			}
		}
		
		Result<Void> rspBean = new Result<Void>();
		rspBean.setCode(0);
		rspBean.setMsg("ok");
		
		return rspBean;
	}
	
	
	@RequestMapping(value="/push/map")
	public  Result<Map<String, String>> pushMap(){
				
		Map<String, String> map = clientManager.getKeyChannelMap();
		
		Result<Map<String, String>> rspBean = new Result<Map<String, String>>();
		rspBean.setCode(0);
		rspBean.setData(map);
		rspBean.setMsg("ok");
		
		return rspBean;
	}
	
	@RequestMapping(value="/reload/service")
	public  Result<Void> reloadService(){
				
		pusherTaskService.loadServiceMap();
		
		Result<Void> rspBean = new Result<Void>();
		rspBean.setCode(0);
		rspBean.setMsg("ok");
		
		return rspBean;
	}
	

}
