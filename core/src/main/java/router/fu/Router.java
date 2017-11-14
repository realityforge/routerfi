package router.fu;

import java.util.ArrayList;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.realityforge.anodoc.TestOnly;

/**
 * Primary interface for performing routing.
 */
public final class Router
{
  @Nonnull
  private final LocationListener _listener;
  @Nonnull
  private final RoutingBackend _backend;
  @Nonnull
  private final ArrayList<Route> _routes;
  /**
   * Callback used when registering callback handler after router installed.
   */
  @Nullable
  private Object _callback;

  public Router( @Nonnull final LocationListener listener,
                 @Nonnull final RoutingBackend backend,
                 @Nonnull final ArrayList<Route> routes )
  {
    _listener = Objects.requireNonNull( listener );
    _backend = Objects.requireNonNull( backend );
    _routes = Objects.requireNonNull( routes );
  }

  /**
   * Change the location to specified location.
   *
   * @param location the location to change to.
   */
  public void changeLocation( @Nonnull final String location )
  {
    _backend.setLocation( location );
  }

  /**
   * Activate the router.
   * This involves connecting the router to the backend and starting to respond to routing changes.
   */
  public void activate()
  {
    deactivate();
    _callback = _backend.addListener( this::onLocationChange );
    onLocationChange( _backend.getLocation() );
  }

  /**
   * Reactivate the router.
   * This involves disconnecting from the backend if currently connected.
   */
  public void deactivate()
  {
    if ( null != _callback )
    {
      _backend.removeListener( _callback );
      _callback = null;
    }
  }

  /**
   * Respond to a location update.
   *
   * @param location the new location. Note this may be the same as the old location.
   */
  private void onLocationChange( @Nonnull final String location )
  {
    _listener.onLocationChanged( route( location ) );
  }

  /**
   * Route on specified location.
   *
   * @param location the location to route.
   * @return the result of routing.
   */
  @Nonnull
  RouteLocation route( @Nonnull final String location )
  {
    final ArrayList<RouteState> states = new ArrayList<>();
    for ( final Route route : _routes )
    {
      final RouteState state = route.match( location );
      if ( null != state )
      {
        states.add( state );
        if ( state.isTerminal() )
        {
          break;
        }
      }
    }
    return new RouteLocation( states );
  }

  @TestOnly
  @Nullable
  Object getCallback()
  {
    return _callback;
  }
}