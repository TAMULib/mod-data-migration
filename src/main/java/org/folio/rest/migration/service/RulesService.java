package org.folio.rest.migration.service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class RulesService {

  private final static Logger logger = LoggerFactory.getLogger(RulesService.class);

  @Autowired
  private OkapiService okapiService;

  @Autowired
  private ResourceLoader resourceLoader;

  @Autowired
  private ObjectMapper objectMapper;

  @Async("asyncTaskExecutor")
  public CompletableFuture<Void> updateRulesAsync(String file, String path, String tenant) {
    return CompletableFuture.supplyAsync(() -> {
      return updateRules(file, path, tenant);
    }).thenAccept(c -> {
      logger.info("finished updating rules");
    });
  }

  public boolean updateRules(String file, String path, String tenant) {
    String token = okapiService.getToken(tenant);
    try {
      JsonNode rules = objectMapper.readValue(loadResource(file).getInputStream(), JsonNode.class);
      okapiService.updateRules(rules, path, tenant, token);
      logger.info("updated mapping rules {}", rules);
    } catch (IOException e) {
      logger.error("failed reading resource {}: {}", file, e.getMessage());
      return false;
    } catch (Exception e) {
      logger.debug("failed updating mapping rules: {}", e.getMessage());
      return false;
    }
    return true;
  }

  private Resource loadResource(String path) throws IOException {
    return resourceLoader.getResource(path);
  }
}
