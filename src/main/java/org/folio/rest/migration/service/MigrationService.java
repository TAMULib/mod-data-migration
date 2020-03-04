package org.folio.rest.migration.service;

import java.util.concurrent.CompletableFuture;

import javax.annotation.PostConstruct;

import org.folio.rest.migration.Migration;
import org.folio.rest.model.repo.ReferenceLinkRepo;
import org.springframework.beans.factory.annotation.Autowired;
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

  @PostConstruct
  public void init() {
    objectMapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

  @Async("asyncTaskExecutor")
  public CompletableFuture<Boolean> migrate(Migration migration) {
    return migration.run(this);
  }

}
