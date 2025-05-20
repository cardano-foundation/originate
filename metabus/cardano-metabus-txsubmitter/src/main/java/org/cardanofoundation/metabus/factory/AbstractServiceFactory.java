package org.cardanofoundation.metabus.factory;

import jakarta.annotation.PostConstruct;
import org.cardanofoundation.metabus.service.TxSubmitterServiceInstance;

import java.util.List;
import java.util.Map;

public abstract class AbstractServiceFactory<T extends TxSubmitterServiceInstance, I> {
  protected final List<T> services;
  protected Map<Class<?>, I> serviceMap;

  protected AbstractServiceFactory(List<T> services) {
    this.services = services;
  }

  @PostConstruct
  abstract void init();
}
