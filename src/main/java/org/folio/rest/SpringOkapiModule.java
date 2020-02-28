package org.folio.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = { "org.folio.rest", "org.folio.spring" })
public class SpringOkapiModule extends SpringBootServletInitializer {

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(SpringOkapiModule.class);
  }

  public static void main(String[] args) {
    SpringApplication.run(SpringOkapiModule.class, args);
  }

}
