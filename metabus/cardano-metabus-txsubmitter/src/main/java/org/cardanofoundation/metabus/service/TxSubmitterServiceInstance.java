package org.cardanofoundation.metabus.service;

import java.lang.reflect.ParameterizedType;

public interface TxSubmitterServiceInstance<T> {

  @SuppressWarnings("unchecked")
  default Class<?> supports() {
    ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
    return (Class<T>) parameterizedType.getActualTypeArguments()[0];
  }
}
