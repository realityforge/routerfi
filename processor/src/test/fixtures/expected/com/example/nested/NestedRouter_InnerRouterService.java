package com.example.nested;

import javax.annotation.Generated;
import javax.annotation.Nonnull;
import router.fu.Location;

@Generated("router.fu.processor.RouterProcessor")
public interface NestedRouter_InnerRouterService {
  @Nonnull
  Location getLocation();

  void reRoute();
}
