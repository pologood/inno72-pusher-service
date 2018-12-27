package com.inno72.pusher.remoting.netty;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.inno72.pusher.remoting.ChannelEventListener;
import com.inno72.pusher.remoting.ChannelIdleClear;
import com.inno72.pusher.remoting.RemotingService;
import com.inno72.pusher.remoting.common.RemotingHelper;
import com.inno72.pusher.remoting.common.RemotingPostConstruct;
import com.inno72.pusher.remoting.common.RemotingUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;


public class NettyRemotingService implements RemotingService, ApplicationContextAware {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ServerBootstrap serverBootstrap;

	private final EventLoopGroup eventLoopGroupSelector;

	private final EventLoopGroup eventLoopGroupBoss;


	private DefaultEventExecutorGroup defaultEventExecutorGroup;

	private final NettyServerConfig nettyServerConfig;

	private ApplicationContext applicationContext;

	private ChannelEventListener channelEventListener;
	
	protected ChannelIdleClear channelIdleClear;

	protected final NettyEventExecutor nettyEventExecutor;
	
	protected RemotingPostConstruct remotingPostConstruct;
	
	private final Timer timer = new Timer("ServerHouseKeepingService", true);

	private int port = 0;

	public NettyRemotingService(NettyServerConfig nettyServerConfig, 
			ChannelEventListener channelEventListener, 
			ChannelIdleClear channelIdleClear,
			RemotingPostConstruct remotingPostConstruct) {

		this.nettyServerConfig = nettyServerConfig;
		this.channelEventListener = channelEventListener;
		this.channelIdleClear = channelIdleClear;
		this.serverBootstrap = new ServerBootstrap();
		this.nettyEventExecutor = new NettyEventExecutor(this.channelEventListener);
		this.remotingPostConstruct = remotingPostConstruct; 

		int publicThreadNums = nettyServerConfig.getServerCallbackExecutorThreads();
		if (publicThreadNums <= 0) {
			publicThreadNums = 4;
		}

		this.eventLoopGroupBoss = new NioEventLoopGroup(1, new ThreadFactory() {
			private AtomicInteger threadIndex = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, String.format("NettyBoss_%d", this.threadIndex.incrementAndGet()));
			}
		});

		if (useEpoll()) {
			this.eventLoopGroupSelector = new EpollEventLoopGroup(nettyServerConfig.getServerSelectorThreads(),
					new ThreadFactory() {
						private AtomicInteger threadIndex = new AtomicInteger(0);
						private int threadTotal = nettyServerConfig.getServerSelectorThreads();

						@Override
						public Thread newThread(Runnable r) {
							return new Thread(r, String.format("NettyServerEPOLLSelector_%d_%d", threadTotal,
									this.threadIndex.incrementAndGet()));
						}
					});
		} else {
			this.eventLoopGroupSelector = new NioEventLoopGroup(nettyServerConfig.getServerSelectorThreads(),
					new ThreadFactory() {
						private AtomicInteger threadIndex = new AtomicInteger(0);
						private int threadTotal = nettyServerConfig.getServerSelectorThreads();

						@Override
						public Thread newThread(Runnable r) {
							return new Thread(r, String.format("NettyServerNIOSelector_%d_%d", threadTotal,
									this.threadIndex.incrementAndGet()));
						}
					});
		}

	}

	private boolean useEpoll() {
		return RemotingUtil.isLinuxPlatform() && nettyServerConfig.isUseEpollNativeSelector() && Epoll.isAvailable();
	}


	@Override
	public void start() {
		
		if(this.remotingPostConstruct != null) {
			this.remotingPostConstruct.postBeforeStart();
		}
		
		this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(nettyServerConfig.getServerWorkerThreads(),
				new ThreadFactory() {

					private AtomicInteger threadIndex = new AtomicInteger(0);

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, "NettyServerCodecThread_" + this.threadIndex.incrementAndGet());
					}
				});

		ServerBootstrap childHandler = this.serverBootstrap.group(this.eventLoopGroupBoss, this.eventLoopGroupSelector)
				.channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 1024).option(ChannelOption.SO_REUSEADDR, true)
				.option(ChannelOption.SO_KEEPALIVE, false).childOption(ChannelOption.TCP_NODELAY, true)
				.childOption(ChannelOption.SO_SNDBUF, nettyServerConfig.getServerSocketSndBufSize())
				.childOption(ChannelOption.SO_RCVBUF, nettyServerConfig.getServerSocketRcvBufSize())
				.localAddress(new InetSocketAddress(this.nettyServerConfig.getListenPort()))
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(defaultEventExecutorGroup, "decoder", new HttpRequestDecoder());
						ch.pipeline().addLast(defaultEventExecutorGroup, "aggregator",
								new HttpObjectAggregator(1024 * 1024 * 10));
						ch.pipeline().addLast(defaultEventExecutorGroup, "encoder", new HttpResponseEncoder());
						ch.pipeline().addLast(defaultEventExecutorGroup, "handshake",
								new WebSocketServerProtocolHandler("/"));
						ch.pipeline().addLast(defaultEventExecutorGroup, new NettyConnectManageHandler());
						ch.pipeline().addLast(defaultEventExecutorGroup, "transaction", applicationContext.getBean(TransactionHandle.class));
					}
				});

		if (nettyServerConfig.isServerPooledByteBufAllocatorEnable()) {
			childHandler.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		}
		
		if (this.channelEventListener != null) {
            this.nettyEventExecutor.start();
        }

		try {
			ChannelFuture sync = this.serverBootstrap.bind().sync();
			InetSocketAddress addr = (InetSocketAddress) sync.channel().localAddress();
			this.port = addr.getPort();
		} catch (InterruptedException e1) {
			throw new RuntimeException("this.serverBootstrap.bind().sync() InterruptedException", e1);
		}
		

        this.timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                try {
//                	if(channelIdleClear != null)
//                		channelIdleClear.clearTimeoutChannel(5000);
                } catch (Throwable e) {
                	logger.error("scanResponseTable exception", e);
                }
            }
        }, 1000 * 3, 1000);
        
        if(this.remotingPostConstruct != null) {
			this.remotingPostConstruct.postAfterStart();
		}
        
        logger.info("remoting server start ok :" + this.port);

	}

	@Override
	public void shutdown() {
		try {
			if (this.timer != null) {
                this.timer.cancel();
            }

			this.eventLoopGroupBoss.shutdownGracefully();
			this.eventLoopGroupSelector.shutdownGracefully();

			if (this.defaultEventExecutorGroup != null) {
				this.defaultEventExecutorGroup.shutdownGracefully();
			}
		} catch (Exception e) {
			logger.error("NettyRemotingServer shutdown exception, ", e);
		}

	}

	public int getLocalPort() {
		return this.port;
	}

	protected void putNettyEvent(final NettyEvent event) {
		this.nettyEventExecutor.putNettyEvent(event);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}


	class NettyConnectManageHandler extends ChannelDuplexHandler {
		@Override
		public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
			final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
			logger.info("NETTY SERVER PIPELINE: channelRegistered {}", remoteAddress);
			super.channelRegistered(ctx);
		}

		@Override
		public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
			final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
			logger.info("NETTY SERVER PIPELINE: channelUnregistered, the channel[{}]", remoteAddress);
			super.channelUnregistered(ctx);
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
			logger.info("NETTY SERVER PIPELINE: channelActive, the channel[{}]", remoteAddress);
			super.channelActive(ctx);

			if (NettyRemotingService.this.channelEventListener != null) {
				NettyRemotingService.this
						.putNettyEvent(new NettyEvent(NettyEventType.CONNECT, remoteAddress, ctx.channel()));
			}
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
			logger.info("NETTY SERVER PIPELINE: channelInactive, the channel[{}]", remoteAddress);
			super.channelInactive(ctx);

			if (NettyRemotingService.this.channelEventListener != null) {
				NettyRemotingService.this
						.putNettyEvent(new NettyEvent(NettyEventType.CLOSE, remoteAddress, ctx.channel()));
			}
		}

		@Override
		public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
			ctx.fireUserEventTriggered(evt);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
			logger.warn("NETTY SERVER PIPELINE: exceptionCaught {}", remoteAddress);
			logger.warn("NETTY SERVER PIPELINE: exceptionCaught exception.", cause);

			if (NettyRemotingService.this.channelEventListener != null) {
				NettyRemotingService.this
						.putNettyEvent(new NettyEvent(NettyEventType.EXCEPTION, remoteAddress, ctx.channel()));
			}

			RemotingUtil.closeChannel(ctx.channel());
		}
	}

}
