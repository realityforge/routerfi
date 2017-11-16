package com.example.route;

import javax.annotation.Generated;
import javax.annotation.Nonnull;
import router.fu.Route;

@Generated("router.fu.processor.RouterProcessor")
public interface RouteWithParametersService {
  @Nonnull
  Route getRegionEventRoute();

  @Nonnull
  Route getRegionRoute();

  @Nonnull
  Route getRegionEventsRoute();

  @Nonnull
  String buildRegionEventLocation(@Nonnull String regionCode, @Nonnull String eventCode);

  @Nonnull
  String buildRegionLocation(@Nonnull String regionCode);

  @Nonnull
  String buildRegionEventsLocation(@Nonnull String regionCode);
}