package com.example.callback;

import router.fu.MatchResult;
import router.fu.annotations.Route;
import router.fu.annotations.RouteCallback;
import router.fu.annotations.Router;

@Router
@Route( name = "region", path = "regions/:regionCode" )
public class DuplicateLocationParameterCallback
{
  @RouteCallback
  MatchResult regionCallback( String location, String location2 )
  {
    return MatchResult.TERMINAL;
  }
}
