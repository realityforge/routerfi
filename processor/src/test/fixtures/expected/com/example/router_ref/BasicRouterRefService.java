package com.example.router_ref;

import javax.annotation.Generated;
import javax.annotation.Nonnull;
import router.fu.Location;

@Generated("router.fu.processor.RouterProcessor")
public interface BasicRouterRefService {
  @Nonnull
  Location getLocation();

  void reRoute();
}
