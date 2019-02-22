package com.inno72.pusher.config;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Configure {

	@Bean(name="asyncPersistenceExecutor")
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
	
	
	@Bean(name="asyncPublicExecutor")
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

	@Bean(name="asyncPublicPriorityExecutor")
	ExecutorService getAsyncPublicPriorityExecutor() {

		ThreadPoolExecutor executor = new ThreadPoolExecutor(
				2,
				32,
				Long.MAX_VALUE, /* timeout */
				TimeUnit.NANOSECONDS,
				new PriorityBlockingQueue<Runnable>(),
				new ThreadFactory() {
					private AtomicInteger threadIndex = new AtomicInteger(0);

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, "asyncPublicPriorityExecutor" + this.threadIndex.incrementAndGet());
					}},
				new ThreadPoolExecutor.AbortPolicy());
		return executor;
	}

}
