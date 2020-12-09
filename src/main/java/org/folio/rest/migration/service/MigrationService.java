package org.folio.rest.migration.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.folio.rest.migration.Migration;
import org.folio.rest.migration.config.model.Database;
import org.folio.rest.migration.exception.MigrationException;
import org.folio.rest.model.repo.ReferenceLinkRepo;
import org.folio.rest.model.repo.ReferenceLinkTypeRepo;
import org.folio.spring.tenant.service.SchemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MigrationService {

  private static final Logger log = LoggerFactory.getLogger(MigrationService.class);

  private static final String QUEUED_RESPONSE_TEMPLATE = "Migration has been queued in position %s";

  @Autowired
  public ObjectMapper objectMapper;

  @Autowired
  public OkapiService okapiService;

  @Autowired
  public SchemaService schemaService;

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

  private LinkedBlockingQueue<Migration> queue = new LinkedBlockingQueue<>();

  private boolean inProgress = false;

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
  public synchronized CompletableFuture<String> migrate(Migration migration) throws MigrationException {
    if (inProgress) {
      queue.add(migration);
      log.info("queued {}, position {}", migration.getClass().getSimpleName(), queue.size());
      return CompletableFuture.completedFuture(String.format(QUEUED_RESPONSE_TEMPLATE, queue.size()));
    }
    inProgress = true;
    return migration.run(this);
  }

  public synchronized void complete() throws MigrationException {
    inProgress = false;
    if (!queue.isEmpty()) {
      Migration migration = queue.poll();
      log.info("dequeued {}, {} remaining", migration.getClass().getSimpleName(), queue.size());
      migrate(migration);
    }
  }

}
