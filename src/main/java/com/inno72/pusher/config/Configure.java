package com.inno72.pusher.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Configure {

	@Bean(name="asyncPersistenceExecutor", destroyMethod="shutdown")
	ExecutorService getAsyncPersistenceExecutor() {
		ExecutorService executor = Executors.newFixedThreadPool(8, new ThreadFactory() {
			private AtomicInteger threadIndex = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "AsyncPersistenceExecutor_" + this.threadIndex.incrementAndGet());
			}
		});
		
		return executor;
	}
	
	
	@Bean(name="asyncPublicExecutor", destroyMethod="shutdown")
	ExecutorService getAsyncPublicExecutor() {
		ExecutorService executor = Executors.newFixedThreadPool(32, new ThreadFactory() {
			private AtomicInteger threadIndex = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "AsyncPublicExecutor" + this.threadIndex.incrementAndGet());
			}
		});
		
		return executor;
	}
	
}
