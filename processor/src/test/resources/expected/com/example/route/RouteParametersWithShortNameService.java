package com.example.route;

import javax.annotation.Generated;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.realityforge.arez.annotations.Action;
import router.fu.Location;
import router.fu.Route;
import router.fu.RouteState;

@Generated("router.fu.processor.RouterProcessor")
public interface RouteParametersWithShortNameService {
  @Nonnull
  Location getLocation();

  @Nonnull
  Route getARoute();

  @Nullable
  RouteState getARouteState();

  @Nonnull
  @Action(
      mutation = false
  )
  String buildALocation(@Nonnull String regionCode);

  @Nonnull
  void gotoA(@Nonnull String regionCode);
}