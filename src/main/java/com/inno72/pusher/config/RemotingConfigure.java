package com.inno72.pusher.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.inno72.pusher.remoting.common.ClientManager;
import com.inno72.pusher.remoting.netty.NettyRemotingService;
import com.inno72.pusher.remoting.netty.NettyServerConfig;
import com.inno72.pusher.service.IdWorker;
import com.inno72.pusher.service.PusherTaskService;


@Configuration
public class RemotingConfigure {
	
	
	@Value("${remoting.port}")
    private int remotingPort; 
	
	@Bean(initMethod= "start", destroyMethod= "shutdown")
	public NettyRemotingService getNettyRemotingService(NettyServerConfig config, ClientManager clientManager, PusherTaskService pusherTaskService) {
		
		return new NettyRemotingService(config, clientManager, clientManager, pusherTaskService);
		
	}
	
	@Bean
	public NettyServerConfig getNettyServerConfig() {
		
		NettyServerConfig nettyServerConfig = new NettyServerConfig();
		
		nettyServerConfig.setListenPort(remotingPort);
		
		return nettyServerConfig;
		
	}
	
	@Bean
	IdWorker idWorkerBean() {
		return new IdWorker(1, 1);
	}

	
}
