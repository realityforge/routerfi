package com.example.route;

import javax.annotation.Generated;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.realityforge.arez.annotations.Action;
import router.fu.Location;
import router.fu.Route;
import router.fu.RouteState;

@Generated("router.fu.processor.RouterProcessor")
public interface RouteWithParametersWithConstraintsService {
  @Nonnull
  Location getLocation();

  @Nonnull
  Route getRegionEventRoute();

  @Nullable
  RouteState getRegionEventRouteState();

  @Nonnull
  Route getRegionRoute();

  @Nullable
  RouteState getRegionRouteState();

  @Nonnull
  Route getRegionEventsRoute();

  @Nullable
  RouteState getRegionEventsRouteState();

  @Nonnull
  @Action(
      mutation = false
  )
  String buildRegionEventLocation(@Nonnull String regionCode, @Nonnull String eventCode);

  @Nonnull
  void gotoRegionEvent(@Nonnull String regionCode, @Nonnull String eventCode);

  @Nonnull
  @Action(
      mutation = false
  )
  String buildRegionLocation(@Nonnull String regionCode);

  @Nonnull
  void gotoRegion(@Nonnull String regionCode);

  @Nonnull
  @Action(
      mutation = false
  )
  String buildRegionEventsLocation(@Nonnull String regionCode);

  @Nonnull
  void gotoRegionEvents(@Nonnull String regionCode);
}
