package com.inno72.pusher.config;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inno72.spring.JsonView;

@Configuration
@EnableWebMvc
public class MvcConfiguration extends WebMvcConfigurerAdapter {
	
	private Logger logger = LoggerFactory.getLogger(MvcConfiguration.class);
	
	public View jsonView() {
		logger.info("***********************init JsonView**************************");
		JsonView view = new JsonView();
		view.setExtractValueFromSingleKeyModel(true);
		Jackson2ObjectMapperFactoryBean objectMapperFactoryBean = new Jackson2ObjectMapperFactoryBean();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapperFactoryBean.setObjectMapper(objectMapper);
		objectMapperFactoryBean.setSerializationInclusion(Include.NON_NULL);
		objectMapperFactoryBean.afterPropertiesSet();
		view.setObjectMapper(objectMapperFactoryBean.getObject());
		return view;
	}
	
	
	@Bean
	public ViewResolver viewResolver() {
		logger.info("***********************init ViewResolver**************************");
		ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
		List<View> views = new ArrayList<View>();
		views.add(jsonView());
		resolver.setDefaultViews(views);
		return resolver;
	}
	
	
}
