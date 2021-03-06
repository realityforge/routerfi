package router.fu;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.realityforge.braincheck.BrainCheckConfig;
import static org.realityforge.braincheck.Guards.*;

/**
 * This class represents the path segments used when reconstructing paths for route.
 */
public final class Segment
{
  @Nullable
  private final Parameter _parameter;
  @Nullable
  private final String _path;

  /**
   * Create a path element for a static path component.
   *
   * @param path the static path component.
   */
  public Segment( @Nonnull final String path )
  {
    _parameter = null;
    _path = Objects.requireNonNull( path );
  }

  /**
   * Create a path element for a parameter.
   *
   * @param parameter the parameter.
   */
  public Segment( @Nonnull final Parameter parameter )
  {
    _parameter = Objects.requireNonNull( parameter );
    _path = null;
  }

  /**
   * Return the static path segment. This should not be called if {@link #isParameter()} returns true.
   *
   * @return the static path component.
   */
  @Nonnull
  public String getPath()
  {
    if ( BrainCheckConfig.checkApiInvariants() )
    {
      apiInvariant( () -> null != _path,
                    () -> "Segment.getPath() invoked on parameter path element with parameter named '" +
                          _parameter + "'" );
    }
    assert null != _path;
    return _path;
  }

  /**
   * Return the parameter used to construct segment. This should only be called if {@link #isParameter()} returns true.
   *
   * @return the parameter component.
   */
  @Nonnull
  public Parameter getParameter()
  {
    if ( BrainCheckConfig.checkApiInvariants() )
    {
      apiInvariant( () -> null != _parameter,
                    () -> "Segment.getParameter() invoked on non-parameter path element with value '" + _path + "'" );
    }
    assert null != _parameter;
    return _parameter;
  }

  /**
   * Return true if this path element is a parameter.
   *
   * @return true if this path element is a parameter.
   */
  public boolean isParameter()
  {
    return null != _parameter;
  }
}
