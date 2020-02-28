package org.folio.rest.migration.config.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "connections")
public class Connections {

  public List<Settings> settings;

  public Connections() {
    settings = new ArrayList<Settings>();
  }

  public List<Settings> getSettings() {
    return settings;
  }

  public void setSettings(List<Settings> settings) {
    this.settings = settings;
  }

  public Optional<Settings> get(String poolName) {
    return settings.stream().filter(properties -> properties.getName().equals(poolName)).findFirst();
  }

}
