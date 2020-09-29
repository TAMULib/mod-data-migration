package org.folio.rest.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  @Autowired
  private AsyncTaskExecutor asyncTaskExecutor;

  @Override
  public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
    configurer.setTaskExecutor(asyncTaskExecutor);
    WebMvcConfigurer.super.configureAsyncSupport(configurer);
  }

}
