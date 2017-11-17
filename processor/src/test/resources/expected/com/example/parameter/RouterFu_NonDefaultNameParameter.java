package com.example.parameter;

import elemental2.core.RegExp;
import elemental2.dom.Window;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Generated;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import router.fu.Elemental2HashBackend;
import router.fu.Location;
import router.fu.MatchResult;
import router.fu.Parameter;
import router.fu.Route;
import router.fu.RouteState;
import router.fu.Router;
import router.fu.Segment;

@Generated("router.fu.processor.RouterProcessor")
public class RouterFu_NonDefaultNameParameter extends NonDefaultNameParameter implements NonDefaultNameParameterService {
  private final Parameter $fu$_regionCode_821487049 = new Parameter( "regionCode", new RegExp( "[a-zA-Z]+" ) );

  private final Route $fu$_route_region = new Route( "region", new Segment[]{new Segment( "regions/" ), new Segment( $fu$_regionCode_821487049 ), }, new Parameter[]{$fu$_regionCode_821487049, }, new RegExp( "^/regions/([a-zA-Z0-9\\-_]+)$" ), ( location, route, parameters ) -> MatchResult.TERMINAL );

  private final Router $fu$_router;

  private Location $fu$_location;

  private RouteState $fu$_state_region;

  private String $fu$_param_foo;

  RouterFu_NonDefaultNameParameter(@Nonnull final Window window) {
    $fu$_router = new Router( this::onLocationChanged, new Elemental2HashBackend( window ), Collections.unmodifiableList( Arrays.asList( $fu$_route_region ) ) );
  }

  @Nonnull
  @Override
  public Route getRegionRoute() {
    return $fu$_route_region;
  }

  @Nullable
  @Override
  public RouteState getRegionRouteState() {
    return $fu$_state_region;
  }

  void setRegionRouteState(@Nullable final RouteState state) {
    $fu$_state_region = state;
  }

  @Nullable
  @Override
  public String getFoo() {
    return $fu$_param_foo;
  }

  void setFoo(@Nullable final String value) {
    $fu$_param_foo = value;
  }

  @Override
  public void updateFoo(@Nonnull final String foo) {
    final Location location = getLocation();
    final RouteState terminalState = location.getTerminalState();
    if ( null != terminalState ) {
      final Route route = terminalState.getRoute();
      if ( route == $fu$_route_region ) {
        gotoRegion(foo);
      }
    }
  }

  @Nonnull
  @Override
  public String buildRegionLocation(@Nonnull final String regionCode) {
    final Map<Parameter, String> $fu$_route_params = new HashMap<>();
    $fu$_route_params.put( $fu$_regionCode_821487049, regionCode );
    return $fu$_route_region.buildLocation( $fu$_route_params );
  }

  @Nonnull
  @Override
  public void gotoRegion(@Nonnull final String regionCode) {
    $fu$_router.changeLocation( buildRegionLocation( regionCode ) );
  }

  @Nonnull
  @Override
  public Location getLocation() {
    assert null != $fu$_location;
    return $fu$_location;
  }

  void setLocation(@Nonnull final Location location) {
    $fu$_location = location;
  }

  void onLocationChanged(@Nonnull final Location location) {
    setLocation( Objects.requireNonNull( location ) );
    final List<RouteState> states = location.getStates();
    int routeStartIndex = 0;
    for ( int i = 0; i < 1; i++ ) {
      final RouteState state = states.size() > routeStartIndex ? states.get( routeStartIndex ) : null;
      routeStartIndex++;
      switch ( i ) {
        case 0:;
        setRegionRouteState( state );
        if ( null != state ) {
          setFoo( state.getParameterValue( $fu$_regionCode_821487049 ) );
        }
        break;
      }
    }
  }
}