package org.folio.rest.config;

import org.folio.rest.handler.ReferenceLinkEventHandler;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

@Configuration
public class RepositoryMvcRestConfig extends RepositoryRestMvcConfiguration {

  @Autowired
  private AsyncTaskExecutor asyncTaskExecutor;

  @Value("${spring.mvc.async.request-timeout:172800000}")
  private long asyncRequestTimeout;

  public RepositoryMvcRestConfig(ApplicationContext context, ObjectFactory<ConversionService> conversionService) {
    super(context, conversionService);
  }

  @Bean
  public ReferenceLinkEventHandler referenceLinkEventHandler() {
    return new ReferenceLinkEventHandler();
  }

  @Override
  public RequestMappingHandlerAdapter repositoryExporterHandlerAdapter() {
    RequestMappingHandlerAdapter requestMappingHandlerAdapter = super.repositoryExporterHandlerAdapter();
    requestMappingHandlerAdapter.setAsyncRequestTimeout(asyncRequestTimeout);
    requestMappingHandlerAdapter.setTaskExecutor(asyncTaskExecutor);
    return requestMappingHandlerAdapter;
  }

  @Override
  @ConfigurationProperties(prefix = "spring.data.rest")
  public RepositoryRestConfiguration repositoryRestConfiguration() {
    return super.repositoryRestConfiguration();
  }

}
