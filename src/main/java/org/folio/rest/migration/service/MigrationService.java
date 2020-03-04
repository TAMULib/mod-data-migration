package org.folio.rest.migration.service;

import java.util.concurrent.CompletableFuture;

import javax.annotation.PostConstruct;

import org.folio.rest.migration.Migration;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.model.repo.ReferenceLinkRepo;
import org.folio.rest.model.repo.ReferenceLinkTypeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MigrationService {

  @Autowired
  public ObjectMapper objectMapper;

  @Autowired
  public OkapiService okapiService;

  @Autowired
  public ReferenceLinkRepo referenceLinkRepo;

  @Autowired
  public ReferenceLinkTypeRepo referenceLinkTypeRepo;

  @Value("${spring.datasource.url}")
  public String url;

  @Value("${spring.datasource.username}")
  public String username;

  @Value("${spring.datasource.password}")
  public String password;

  @Value("${spring.datasource.driverClassName}")
  public String driverClassName;

  public Database referenceLinkSettings;

  @PostConstruct
  public void init() {
    objectMapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    referenceLinkSettings = new Database();
    referenceLinkSettings.setUrl(url);
    referenceLinkSettings.setUsername(username);
    referenceLinkSettings.setPassword(password);
    referenceLinkSettings.setDriverClassName(driverClassName);
  }

  @Async("asyncTaskExecutor")
  public CompletableFuture<Boolean> migrate(Migration migration) {
    return migration.run(this);
  }

}
