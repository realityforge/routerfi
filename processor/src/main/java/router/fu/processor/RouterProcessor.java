package router.fu.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import router.fu.annotations.Route;
import router.fu.annotations.Router;
import static javax.tools.Diagnostic.Kind.*;

/**
 * Annotation processor that analyzes Router annotated source and generates a router
 * implementation from the annotations.
 */
@SuppressWarnings( "Duplicates" )
@AutoService( Processor.class )
@SupportedAnnotationTypes( { "router.fu.annotations.*" } )
@SupportedSourceVersion( SourceVersion.RELEASE_8 )
public final class RouterProcessor
  extends AbstractProcessor
{
  private final Pattern _urlParameterPattern = Pattern.compile( "^:([a-zA-Z0-9-_]*[a-zA-Z0-9])(<(.+?)>)?" );
  private final Pattern _separatorPattern = Pattern.compile( "^([!&\\-/_.;])" );
  private final Pattern _fragmentPattern = Pattern.compile( "^([0-9a-zA-Z]+)" );

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean process( final Set<? extends TypeElement> annotations, final RoundEnvironment env )
  {
    final Set<? extends Element> elements = env.getElementsAnnotatedWith( Router.class );
    processElements( elements );
    return false;
  }

  private void processElements( @Nonnull final Set<? extends Element> elements )
  {
    for ( final Element element : elements )
    {
      try
      {
        process( (TypeElement) element );
      }
      catch ( final IOException ioe )
      {
        processingEnv.getMessager().printMessage( ERROR, ioe.getMessage(), element );
      }
      catch ( final RouterProcessorException e )
      {
        processingEnv.getMessager().printMessage( ERROR, e.getMessage(), e.getElement() );
      }
      catch ( final Throwable e )
      {
        final StringWriter sw = new StringWriter();
        e.printStackTrace( new PrintWriter( sw ) );
        sw.flush();

        final String message =
          "Unexpected error will running the " + getClass().getName() + " processor. This has " +
          "resulted in a failure to process the code and has left the compiler in an invalid " +
          "state. Please report the failure to the developers so that it can be fixed.\n" +
          " Report the error at: https://github.com/realityforge/router-fu/issues\n" +
          "\n\n" +
          sw.toString();
        processingEnv.getMessager().printMessage( ERROR, message, element );
      }
    }
  }

  private void process( @Nonnull final TypeElement element )
    throws IOException, RouterProcessorException
  {
    final RouterDescriptor descriptor = parse( element );
    emitTypeSpec( descriptor.getPackageName(), Generator.buildService( descriptor ) );
    emitTypeSpec( descriptor.getPackageName(), Generator.buildRouterImpl( descriptor ) );
  }

  private void emitTypeSpec( @Nonnull final String packageName, @Nonnull final TypeSpec typeSpec )
    throws IOException
  {
    JavaFile.builder( packageName, typeSpec ).
      skipJavaLangImports( true ).
      build().
      writeTo( processingEnv.getFiler() );
  }

  @Nonnull
  private RouterDescriptor parse( @Nonnull final TypeElement typeElement )
  {
    final Router component = typeElement.getAnnotation( Router.class );
    assert null != component;
    final PackageElement packageElement = processingEnv.getElementUtils().getPackageOf( typeElement );
    final RouterDescriptor descriptor = new RouterDescriptor( packageElement, typeElement );
    descriptor.setArezComponent( component.arez() );

    parseRouteAnnotations( typeElement, descriptor );

    return descriptor;
  }

  private void parseRouteAnnotations( @Nonnull final TypeElement typeElement,
                                      @Nonnull final RouterDescriptor descriptor )
  {
    for ( final Route routeAnnotation : typeElement.getAnnotationsByType( Route.class ) )
    {
      parseRouteAnnotation( typeElement, descriptor, routeAnnotation );
    }
  }

  private void parseRouteAnnotation( @Nonnull final TypeElement typeElement,
                                     @Nonnull final RouterDescriptor router,
                                     @Nonnull final Route annotation )
  {
    final String name = annotation.name();
    if ( !ProcessorUtil.isJavaIdentifier( name ) )
    {
      throw new RouterProcessorException( "@Router target has a route with an invalid name '" + name + "'",
                                          typeElement );

    }
    final String path = annotation.path();
    final boolean navigationTarget = annotation.navigationTarget();
    final boolean partialMatch = annotation.partialMatch();
    final RouteDescriptor route = new RouteDescriptor( name, path, navigationTarget, partialMatch );

    if ( router.hasRouteNamed( name ) )
    {
      throw new RouterProcessorException( "@Router target has multiple routes with the name '" + name + "'",
                                          typeElement );
    }

    parseRoutePath( route, path );

    router.addRoute( route );
  }

  private void parseRoutePath( @Nonnull final RouteDescriptor route, @Nonnull final String path )
  {
    final int length = path.length();
    int start = 0;

    while ( start < length )
    {
      // Match path parameters
      {
        final Matcher matcher = _urlParameterPattern.matcher( path.substring( start ) );
        if ( matcher.find() )
        {
          final String matched = matcher.group();
          start += matched.length();
          final String name = matcher.group( 1 );
          final String constraint = matcher.groupCount() > 1 ? matcher.group( 3 ) : null;
          route.addParameter( new ParameterDescriptor( name, constraint ) );
        }
      }
      // Match separators
      {
        final Matcher matcher = _separatorPattern.matcher( path.substring( start ) );
        if ( matcher.find() )
        {
          final String matched = matcher.group();
          start += matched.length();
          route.addText( matched );
        }
      }

      // Match fragments
      {
        final Matcher matcher = _fragmentPattern.matcher( path.substring( start ) );
        if ( matcher.find() )
        {
          final String matched = matcher.group();
          start += matched.length();
          route.addText( matched );
        }
      }
    }
  }
}
