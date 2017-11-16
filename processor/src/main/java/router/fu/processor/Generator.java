package router.fu.processor;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Generated;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import router.fu.Elemental2HashBackend;
import router.fu.Location;
import router.fu.Parameter;

final class Generator
{
  private static final ClassName REGEXP_TYPE = ClassName.get( "elemental2.core", "RegExp" );
  private static final ClassName WINDOW_TYPE = ClassName.get( "elemental2.dom", "Window" );
  private static final ClassName ROUTER_TYPE = ClassName.get( "router.fu", "Router" );
  private static final ClassName ROUTE_TYPE = ClassName.get( "router.fu", "Route" );
  private static final ClassName ROUTE_STATE_TYPE = ClassName.get( "router.fu", "RouteState" );
  private static final ClassName SEGMENT_TYPE = ClassName.get( "router.fu", "Segment" );
  private static final ClassName PARAMETER_TYPE = ClassName.get( "router.fu", "Parameter" );
  private static final ClassName MATCH_RESULT_TYPE = ClassName.get( "router.fu", "MatchResult" );
  private static final String FIELD_PREFIX = "$fu$_";
  private static final String ROUTE_FIELD_PREFIX = FIELD_PREFIX + "route_";
  private static final String ROUTE_STATE_FIELD_PREFIX = FIELD_PREFIX + "state_";

  private Generator()
  {
  }

  @Nonnull
  static TypeSpec buildService( @Nonnull final RouterDescriptor descriptor )
  {
    final TypeElement element = descriptor.getElement();

    final TypeSpec.Builder builder = TypeSpec.interfaceBuilder( descriptor.getServiceClassName() );

    ProcessorUtil.copyAccessModifiers( element, builder );

    // Mark it as generated
    builder.addAnnotation( AnnotationSpec.builder( Generated.class ).
      addMember( "value", "$S", RouterProcessor.class.getName() ).
      build() );

    buildGetLocationMethod( builder );
    descriptor.getRoutes().forEach( route -> {
      buildRouteMethod( builder, route );
      buildGetRouteStateMethod( builder, route );
    } );
    descriptor.getRoutes().stream().
      filter( RouteDescriptor::isNavigationTarget ).
      forEach( route -> {
        buildBuildLocationMethod( builder, route );
        buildGotoLocationMethod( builder, route );
      } );

    return builder.build();
  }

  private static void buildGetLocationMethod( @Nonnull final TypeSpec.Builder builder )
  {
    final MethodSpec.Builder method = MethodSpec.methodBuilder( "getLocation" );
    method.addAnnotation( Nonnull.class );
    method.addModifiers( Modifier.PUBLIC, Modifier.ABSTRACT );
    method.returns( Location.class );
    builder.addMethod( method.build() );
  }

  private static void buildRouteMethod( @Nonnull final TypeSpec.Builder builder, @Nonnull final RouteDescriptor route )
  {
    final MethodSpec.Builder method =
      MethodSpec.methodBuilder( "get" + route.getPascalCaseName() + "Route" );
    method.addModifiers( Modifier.PUBLIC, Modifier.ABSTRACT );
    method.addAnnotation( Nonnull.class );
    method.returns( ROUTE_TYPE );
    builder.addMethod( method.build() );
  }

  private static void buildGetRouteStateMethod( @Nonnull final TypeSpec.Builder builder,
                                                @Nonnull final RouteDescriptor route )
  {
    final MethodSpec.Builder method =
      MethodSpec.methodBuilder( "get" + route.getPascalCaseName() + "RouteState" );
    method.addModifiers( Modifier.PUBLIC, Modifier.ABSTRACT );
    method.addAnnotation( Nullable.class );
    method.returns( ROUTE_STATE_TYPE );
    builder.addMethod( method.build() );
  }

  private static void buildBuildLocationMethod( @Nonnull final TypeSpec.Builder builder,
                                                @Nonnull final RouteDescriptor route )
  {
    final MethodSpec.Builder method =
      MethodSpec.methodBuilder( "build" + route.getPascalCaseName() + "Location" );
    method.addModifiers( Modifier.PUBLIC, Modifier.ABSTRACT );
    method.addAnnotation( Nonnull.class );
    method.returns( String.class );
    for ( final ParameterDescriptor parameter : route.getParameters() )
    {
      method.addParameter( ParameterSpec.builder( String.class, parameter.getName() )
                             .addAnnotation( Nonnull.class )
                             .build() );
    }
    builder.addMethod( method.build() );
  }

  private static void buildGotoLocationMethod( @Nonnull final TypeSpec.Builder builder,
                                               @Nonnull final RouteDescriptor route )
  {
    final MethodSpec.Builder method =
      MethodSpec.methodBuilder( "goto" + route.getPascalCaseName() );
    method.addModifiers( Modifier.PUBLIC, Modifier.ABSTRACT );
    method.addAnnotation( Nonnull.class );
    for ( final ParameterDescriptor parameter : route.getParameters() )
    {
      method.addParameter( ParameterSpec.builder( String.class, parameter.getName() )
                             .addAnnotation( Nonnull.class )
                             .build() );
    }
    builder.addMethod( method.build() );
  }

  @Nonnull
  static TypeSpec buildRouterImpl( @Nonnull final RouterDescriptor descriptor )
  {
    final TypeElement element = descriptor.getElement();

    final TypeSpec.Builder builder = TypeSpec.classBuilder( descriptor.getRouterClassName() );

    builder.superclass( descriptor.getClassName() );

    if ( descriptor.isArezComponent() )
    {
      final AnnotationSpec.Builder annotation =
        AnnotationSpec.builder( ClassName.get( "org.realityforge.arez.annotations", "ArezComponent" ) ).
          addMember( "type", "$S", descriptor.getClassName().simpleName() ).
          addMember( "nameIncludesId", "false" ).
          addMember( "allowEmpty", "true" );
      builder.addAnnotation( annotation.build() );
    }

    ProcessorUtil.copyAccessModifiers( element, builder );

    builder.addSuperinterface( descriptor.getServiceClassName() );

    // Mark it as generated
    builder.addAnnotation( AnnotationSpec.builder( Generated.class ).
      addMember( "value", "$S", RouterProcessor.class.getName() ).
      build() );

    buildParameterFields( builder, descriptor );
    descriptor.getRoutes().forEach( route -> buildRouteField( builder, route ) );
    //Add router field
    builder.addField( ROUTER_TYPE, FIELD_PREFIX + "router", Modifier.FINAL, Modifier.PRIVATE );
    builder.addField( Location.class, FIELD_PREFIX + "location", Modifier.PRIVATE );
    descriptor.getRoutes().forEach( route -> buildRouteStateField( builder, route ) );

    buildConstructor( builder, descriptor );

    descriptor.getRoutes().forEach( route -> {
      buildRouteMethodImpl( builder, route );
      buildGetRouteStateMethodImpl( builder, route );
      buildSetRouteStateMethodImpl( builder, route );
    } );
    descriptor.getRoutes().stream().
      filter( RouteDescriptor::isNavigationTarget ).
      forEach( route -> {
        buildBuildLocationMethodImpl( builder, route );
        buildGotoLocationMethodImpl( builder, route );
      } );

    buildGetLocationImplMethod( builder );
    buildSetLocationMethod( builder );
    buildOnLocationChangedMethod( builder, descriptor );

    return builder.build();
  }

  private static void buildConstructor( @Nonnull final TypeSpec.Builder builder,
                                        @Nonnull final RouterDescriptor descriptor )
  {
    final MethodSpec.Builder ctor = MethodSpec.constructorBuilder();
    ctor.addParameter( ParameterSpec.builder( WINDOW_TYPE, "window", Modifier.FINAL ).
      addAnnotation( Nonnull.class ).build() );

    final StringBuilder sb = new StringBuilder();
    final ArrayList<Object> params = new ArrayList<>();
    sb.append( "$N = new $T( this::onLocationChanged, new $T( window ), $T.unmodifiableList( $T.asList( " );
    params.add( FIELD_PREFIX + "router" );
    params.add( ROUTER_TYPE );
    params.add( Elemental2HashBackend.class );
    params.add( Collections.class );
    params.add( Arrays.class );
    sb.append( descriptor.getRoutes()
                 .stream()
                 .map( route -> ROUTE_FIELD_PREFIX + route.getName() )
                 .peek( params::add )
                 .map( routeName -> "$N" )
                 .collect( Collectors.joining( ", " ) ) );
    sb.append( " ) ) )" );
    ctor.addStatement( sb.toString(), params.toArray() );
    builder.addMethod( ctor.build() );
  }

  private static void buildParameterFields( @Nonnull final TypeSpec.Builder builder,
                                            @Nonnull final RouterDescriptor descriptor )
  {
    final Map<String, ParameterDescriptor> parameters =
      descriptor.getRoutes().stream().
        flatMap( r -> r.getParameters().stream() ).
        collect( Collectors.toMap( ParameterDescriptor::getKey, Function.identity(), ( s, a ) -> s ) );

    parameters.values().forEach( parameter -> buildParameterField( builder, parameter ) );
  }

  private static void buildParameterField( @Nonnull final TypeSpec.Builder builder,
                                           @Nonnull final ParameterDescriptor parameter )
  {
    final FieldSpec.Builder field =
      FieldSpec.builder( Parameter.class,
                         FIELD_PREFIX + parameter.getFieldName(),
                         Modifier.FINAL,
                         Modifier.PRIVATE );
    if ( null != parameter.getConstraint() )
    {
      field.initializer( "new $T( $S, new $T( $S ) )",
                         Parameter.class,
                         parameter.getName(),
                         REGEXP_TYPE,
                         parameter.getConstraint() );
    }
    else
    {
      field.initializer( "new $T( $S )", Parameter.class, parameter.getName() );
    }
    builder.addField( field.build() );
  }

  private static void buildRouteStateField( @Nonnull final TypeSpec.Builder builder,
                                            @Nonnull final RouteDescriptor route )
  {
    final FieldSpec.Builder field =
      FieldSpec.builder( ROUTE_STATE_TYPE, ROUTE_STATE_FIELD_PREFIX + route.getName(), Modifier.PRIVATE );
    builder.addField( field.build() );
  }

  private static void buildRouteField( @Nonnull final TypeSpec.Builder builder, @Nonnull final RouteDescriptor route )
  {
    final FieldSpec.Builder field =
      FieldSpec.builder( ROUTE_TYPE,
                         ROUTE_FIELD_PREFIX + route.getName(),
                         Modifier.FINAL,
                         Modifier.PRIVATE );
    final StringBuilder sb = new StringBuilder();
    final ArrayList<Object> params = new ArrayList<>();
    sb.append( "new $T( $S, " );
    params.add( ROUTE_TYPE );
    params.add( route.getName() );

    sb.append( "new $T[]{" );
    params.add( SEGMENT_TYPE );
    buildSegments( sb, params, route );
    sb.append( "}, " );

    sb.append( "new $T[]{" );
    params.add( PARAMETER_TYPE );
    buildParameters( sb, params, route );
    sb.append( "}, " );

    sb.append( "new $T( $S ), " );
    params.add( REGEXP_TYPE );
    params.add( toJsRegExp( route ) );

    sb.append( "( location, route, parameters ) -> $T.$N )" );
    params.add( MATCH_RESULT_TYPE );
    params.add( route.isNavigationTarget() ? "TERMINAL" : "NON_TERMINAL" );

    field.initializer( sb.toString(), params.toArray() );
    builder.addField( field.build() );
  }

  @Nonnull
  private static String toJsRegExp( @Nonnull final RouteDescriptor route )
  {
    final StringBuilder sb = new StringBuilder();
    sb.append( "^/" );
    for ( final Object part : route.getParts() )
    {
      if ( part instanceof String )
      {
        sb.append( part );
      }
      else
      {
        sb.append( "([a-zA-Z0-9\\-_]+)" );
      }
    }
    sb.append( "$" );
    return sb.toString();
  }

  private static void buildSegments( @Nonnull final StringBuilder sb,
                                     @Nonnull final ArrayList<Object> params,
                                     @Nonnull final RouteDescriptor route )
  {
    final StringBuilder accumulator = new StringBuilder();
    for ( final Object part : route.getParts() )
    {
      if ( part instanceof String )
      {
        accumulator.append( part );
      }
      else
      {
        if ( 0 != accumulator.length() )
        {
          sb.append( "new $T( $S ), " );
          params.add( SEGMENT_TYPE );
          params.add( accumulator.toString() );
          accumulator.setLength( 0 );
        }
        final ParameterDescriptor param = (ParameterDescriptor) part;
        sb.append( "new $T( $N ), " );
        params.add( SEGMENT_TYPE );
        params.add( FIELD_PREFIX + param.getFieldName() );
      }
    }
    if ( 0 != accumulator.length() )
    {
      sb.append( "new $T( $S ) " );
      params.add( SEGMENT_TYPE );
      params.add( accumulator.toString() );
    }
  }

  private static void buildParameters( @Nonnull final StringBuilder sb,
                                       @Nonnull final ArrayList<Object> params,
                                       @Nonnull final RouteDescriptor route )
  {
    for ( final Object part : route.getParts() )
    {
      if ( part instanceof ParameterDescriptor )
      {
        final ParameterDescriptor param = (ParameterDescriptor) part;
        sb.append( "$N, " );
        params.add( FIELD_PREFIX + param.getFieldName() );
      }
    }
  }

  private static void buildRouteMethodImpl( @Nonnull final TypeSpec.Builder builder,
                                            @Nonnull final RouteDescriptor route )
  {
    final MethodSpec.Builder method =
      MethodSpec.methodBuilder( "get" + route.getPascalCaseName() + "Route" );
    method.addModifiers( Modifier.PUBLIC );
    method.addAnnotation( Nonnull.class );
    method.addAnnotation( Override.class );
    method.returns( ROUTE_TYPE );
    method.addStatement( "return $N", ROUTE_FIELD_PREFIX + route.getName() );
    builder.addMethod( method.build() );
  }

  private static void buildGetRouteStateMethodImpl( @Nonnull final TypeSpec.Builder builder,
                                                    @Nonnull final RouteDescriptor route )
  {
    final MethodSpec.Builder method =
      MethodSpec.methodBuilder( "get" + route.getPascalCaseName() + "RouteState" );
    method.addModifiers( Modifier.PUBLIC );
    method.addAnnotation( Nullable.class );
    method.addAnnotation( Override.class );
    method.returns( ROUTE_STATE_TYPE );
    method.addStatement( "return $N", ROUTE_STATE_FIELD_PREFIX + route.getName() );
    builder.addMethod( method.build() );
  }

  private static void buildSetRouteStateMethodImpl( @Nonnull final TypeSpec.Builder builder,
                                                    @Nonnull final RouteDescriptor route )
  {
    final MethodSpec.Builder method =
      MethodSpec.methodBuilder( "set" + route.getPascalCaseName() + "RouteState" );
    final ParameterSpec.Builder parameter =
      ParameterSpec.builder( ROUTE_STATE_TYPE, "state", Modifier.FINAL ).addAnnotation( Nullable.class );
    method.addParameter( parameter.build() );
    method.addStatement( "$N = state", ROUTE_STATE_FIELD_PREFIX + route.getName() );
    builder.addMethod( method.build() );
  }

  private static void buildBuildLocationMethodImpl( @Nonnull final TypeSpec.Builder builder,
                                                    @Nonnull final RouteDescriptor route )
  {
    final MethodSpec.Builder method =
      MethodSpec.methodBuilder( "build" + route.getPascalCaseName() + "Location" );
    method.addModifiers( Modifier.PUBLIC );
    method.addAnnotation( Nonnull.class );
    method.addAnnotation( Override.class );
    method.returns( String.class );

    method.addStatement( "final $T<$T, $T> $N = new $T<>()",
                         Map.class,
                         PARAMETER_TYPE,
                         String.class,
                         ROUTE_FIELD_PREFIX + "params",
                         HashMap.class );
    for ( final ParameterDescriptor parameter : route.getParameters() )
    {
      method.addParameter( ParameterSpec.builder( String.class, parameter.getName(), Modifier.FINAL )
                             .addAnnotation( Nonnull.class )
                             .build() );
      method.addStatement( "$N.put( $N, $N )",
                           ROUTE_FIELD_PREFIX + "params",
                           FIELD_PREFIX + parameter.getFieldName(),
                           parameter.getName() );
    }
    method.addStatement( "return $N.buildLocation( $N )",
                         ROUTE_FIELD_PREFIX + route.getName(),
                         ROUTE_FIELD_PREFIX + "params" );
    builder.addMethod( method.build() );
  }

  private static void buildGotoLocationMethodImpl( @Nonnull final TypeSpec.Builder builder,
                                                   @Nonnull final RouteDescriptor route )
  {
    final MethodSpec.Builder method =
      MethodSpec.methodBuilder( "goto" + route.getPascalCaseName() );
    method.addModifiers( Modifier.PUBLIC );
    method.addAnnotation( Nonnull.class );
    method.addAnnotation( Override.class );
    for ( final ParameterDescriptor parameter : route.getParameters() )
    {
      method.addParameter( ParameterSpec.builder( String.class, parameter.getName(), Modifier.FINAL )
                             .addAnnotation( Nonnull.class )
                             .build() );
    }

    final StringBuilder sb = new StringBuilder();
    final ArrayList<Object> params = new ArrayList<>();
    sb.append( "$N.changeLocation( $N( " );
    params.add( FIELD_PREFIX + "router" );
    params.add( "build" + route.getPascalCaseName() + "Location" );
    sb.append( route.getParameters()
                 .stream()
                 .map( ParameterDescriptor::getName )
                 .peek( params::add )
                 .map( routeName -> "$N" )
                 .collect( Collectors.joining( ", " ) ) );
    sb.append( " ) )" );
    method.addStatement( sb.toString(), params.toArray() );
    builder.addMethod( method.build() );
  }

  private static void buildGetLocationImplMethod( @Nonnull final TypeSpec.Builder builder )
  {
    final MethodSpec.Builder method = MethodSpec.methodBuilder( "getLocation" );
    method.addAnnotation( Nonnull.class );
    method.addAnnotation( Override.class );
    method.addModifiers( Modifier.PUBLIC );
    method.returns( Location.class );
    method.addStatement( "assert null != $N", FIELD_PREFIX + "location" );
    method.addStatement( "return $N", FIELD_PREFIX + "location" );
    builder.addMethod( method.build() );
  }

  private static void buildSetLocationMethod( @Nonnull final TypeSpec.Builder builder )
  {
    final MethodSpec.Builder method = MethodSpec.methodBuilder( "setLocation" );
    method.addParameter( ParameterSpec.builder( Location.class, "location", Modifier.FINAL )
                           .addAnnotation( Nonnull.class )
                           .build() );
    method.addStatement( "$N = location", FIELD_PREFIX + "location" );
    builder.addMethod( method.build() );
  }

  private static void buildOnLocationChangedMethod( @Nonnull final TypeSpec.Builder builder,
                                                    @Nonnull final RouterDescriptor descriptor )
  {
    final MethodSpec.Builder method = MethodSpec.methodBuilder( "onLocationChanged" );
    method.addParameter( ParameterSpec.builder( Location.class, "location", Modifier.FINAL )
                           .addAnnotation( Nonnull.class )
                           .build() );
    method.addStatement( "setLocation( $T.requireNonNull( location ) )", Objects.class );
    builder.addMethod( method.build() );
  }
}
