package org.folio.rest.migration.model;

import org.folio.rest.migration.model.request.user.UserDefaults;
import org.folio.rest.migration.model.request.user.UserMaps;

public abstract class AbstractUserRecord {

  protected UserMaps maps;
  protected UserDefaults defaults;

  public UserMaps getMaps() {
    return maps;
  }

  public void setMaps(UserMaps userMaps) {
    this.maps = userMaps;
  }

  public UserDefaults getDefaults() {
    return defaults;
  }

  public void setDefaults(UserDefaults userDefaults) {
    this.defaults = userDefaults;
  }

}