package router.fu;

import elemental2.core.JsRegExp;
import elemental2.core.RegExpResult;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.realityforge.braincheck.BrainCheckConfig;
import static org.realityforge.braincheck.Guards.*;

/**
 * A named pattern that can be matched during routing.
 * Matching the pattern can produce parameters as described by the {@link #_parameters} field. If the
 * toolkit identifies this route as matching and all patterns pass validation, then the toolkit invokes the
 * {@link #_matchCallback} callback to complete the match process.
 *
 * <p>Some routes can also be used as targets of navigation. These routes are constructed with {@link Segment}
 * instances passed to them and will return true from {@link #isNavigationTarget()}. The {@link #buildLocation(Map)}
 * method can be invoked on navigation targets.</p>
 */
public final class Route
{
  /**
   * The name of route. An opaque string primarily useful for users.
   */
  @Nonnull
  private final String _name;
  /**
   * The list of elements used for constructing a url if route can be a navigation target. Otherwise this is null.
   */
  @Nullable
  private final Segment[] _segments;
  /**
   * Descriptors for parameters extracted from the path.
   */
  @Nonnull
  private final Parameter[] _parameters;
  /**
   * The regular expression that matches the path and extracts parameters.
   */
  @Nonnull
  private final JsRegExp _matcher;
  /**
   * The callback that makes the final decision whether a route matches.
   */
  @Nonnull
  private final RouteMatchCallback _matchCallback;

  static String pathToPattern( @Nonnull final String path )
  {
    return "^" + path.replaceAll( "([\\-/\\\\\\^$\\*\\+\\?\\.\\(\\)\\|\\[\\]\\{\\}])", "\\\\$1" ) + "$";
  }

  public Route( @Nonnull final String name,
                @Nullable final Segment[] segments,
                @Nonnull final Parameter[] parameters,
                @Nonnull final JsRegExp matcher,
                @Nonnull final RouteMatchCallback matchCallback )
  {
    _name = Objects.requireNonNull( name );
    _segments = segments;
    _parameters = Objects.requireNonNull( parameters );
    _matcher = Objects.requireNonNull( matcher );
    _matchCallback = Objects.requireNonNull( matchCallback );
  }

  /**
   * Return the name of the route
   *
   * @return the name of the route.
   */
  @Nonnull
  public String getName()
  {
    return _name;
  }

  /**
   * Return true if it is valid to navigate to the location identified by this route.
   * As some routes only act as filters (i.e. for applying security) they are not valid navigation
   * targets and should return false from this method.
   *
   * @return true if it is valid to navigate to this route..
   */
  public boolean isNavigationTarget()
  {
    return null != _segments;
  }

  /**
   * Build a location string from the specified parameters.
   * This method should not be invoked unless {@link #isNavigationTarget()} returns true.
   * This location generated by this method should produce identical parameters if passed into match method.
   *
   * @param parameters the parameters required by route.
   * @return the location string.
   */
  @Nonnull
  public String buildLocation( @Nonnull final Map<Parameter, String> parameters )
  {
    if ( BrainCheckConfig.checkApiInvariants() )
    {
      apiInvariant( this::isNavigationTarget,
                    () -> "Route named '" + _name + "' can not have buildPath() invoked on it as is not a target." );
    }
    assert null != _segments;
    final HashSet<Parameter> usedParameters = BrainCheckConfig.checkApiInvariants() ? new HashSet<>() : null;
    final StringBuilder sb = new StringBuilder();
    for ( final Segment segment : _segments )
    {
      if ( segment.isParameter() )
      {
        final Parameter parameterKey = segment.getParameter();
        if ( BrainCheckConfig.checkApiInvariants() )
        {
          apiInvariant( () -> parameters.containsKey( parameterKey ),
                        () -> "Route named '" + _name + "' expects a parameter named '" + parameterKey + "' to be " +
                              "supplied when building path but no such parameter was supplied. " +
                              "Parameters: " + parameters );
          assert null != usedParameters;
          usedParameters.add( parameterKey );
        }
        final String parameterValue = parameters.get( parameterKey );
        if ( BrainCheckConfig.checkApiInvariants() )
        {
          apiInvariant( () -> null == segment.getParameter().getValidator() ||
                              segment.getParameter().getValidator().test( parameterValue ),
                        () -> "Route named '" + _name + "' has a parameter named '" + parameterKey + "' and " +
                              "a value '" + parameterValue + "' has been passed that does not pass validation check." );
        }
        sb.append( parameterValue );
      }
      else
      {
        sb.append( segment.getPath() );
      }
    }
    final String location = sb.toString();
    if ( BrainCheckConfig.checkApiInvariants() )
    {
      assert null != usedParameters;
      final List<Parameter> unusedParameters =
        parameters.keySet().stream().filter( k -> !usedParameters.contains( k ) ).collect( Collectors.toList() );
      apiInvariant( unusedParameters::isEmpty,
                    () -> "Route named '" + _name + "' expects all parameters to be used when building " +
                          "path but the following parameters are unused. Parameters: " + unusedParameters );
    }
    return location;
  }

  /**
   * Attempt to match the specified location.
   *
   * @param location the location to match.
   * @return the route state if a match is successful, null otherwise.
   */
  @Nullable
  public RouteState match( @Nonnull final String location )
  {
    final Map<Parameter, String> parameters = locationMatch( location );
    if ( null != parameters )
    {
      final MatchResult matchResult = _matchCallback.shouldMatch( location, this, parameters );
      if ( MatchResult.NO_MATCH != matchResult )
      {
        return new RouteState( this, parameters, MatchResult.TERMINAL == matchResult );
      }
    }
    return null;
  }

  /**
   * Attempt to match the specified location and generate parameters.
   *
   * @param location the location to match.
   * @return the parameters if match, else null.
   */
  @Nullable
  private Map<Parameter, String> locationMatch( @Nonnull final String location )
  {
    final RegExpResult groups = _matcher.exec( Objects.requireNonNull( location ) );
    if ( null != groups )
    {
      final Map<Parameter, String> matchData = new HashMap<>();
      //Group 0 is the whole string so we can skip it
      for ( int i = 1; i < groups.length; i++ )
      {
        final String value = groups.getAt( i );
        final int paramIndex = i - 1;
        final Parameter parameter = getParameterByIndex( paramIndex );
        final JsRegExp validator = parameter.getValidator();
        if ( null != validator && !validator.test( value ) )
        {
          return null;
        }
        matchData.put( parameter, value );
      }
      return matchData;
    }
    else
    {
      return null;
    }
  }

  /**
   * Return the parameter descriptor at index.
   *
   * @return the parameter descriptor at index.
   */
  @Nonnull
  Parameter getParameterByIndex( final int index )
  {
    if ( BrainCheckConfig.checkInvariants() )
    {
      invariant( () -> _parameters.length > index,
                 () -> "Route named '" + _name + "' expects a parameter at index " + index + " when matching " +
                       "location but no such parameter has been defined." );
    }
    return _parameters[ index ];
  }
}
