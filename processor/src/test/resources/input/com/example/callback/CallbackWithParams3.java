package com.example.callback;

import java.util.Map;
import router.fu.MatchResult;
import router.fu.Parameter;
import router.fu.annotations.Route;
import router.fu.annotations.RouteCallback;
import router.fu.annotations.Router;

@Router
@Route( name = "region", path = "regions/:regionCode" )
public class CallbackWithParams3
{
  @RouteCallback
  MatchResult regionCallback( Map<Parameter, String> parameters )
  {
    return MatchResult.TERMINAL;
  }
}
