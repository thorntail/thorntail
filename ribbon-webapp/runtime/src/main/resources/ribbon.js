
Ribbon = {};

Ribbon.ajax = function(serviceName, url, settings) {
  var allServers = Booker.State.Topology.servers( serviceName );

  console.log( "servers for [" + serviceName + "]", allServers );

  if ( ! settings ) {
    settings = {};
  }

  var headers = settings.headers || {};
  settings.url = '//' + allServers[0] + url;
  settings.headers = headers;

  if ( allServers.length < 1 ) {
    var deferred = $.Deferred();
    return deferred.reject();
  }

  if ( ! keycloak.authenticated ) {
    console.log( "not authenticated" );
    return Ribbon._doRequest( settings, false );
  }

  var deferred = $.Deferred();

  keycloak.updateToken(30).success( function() {
    console.log( "refreshed" );
    Ribbon._doRequest( settings, true ).then( function(result) {
      deferred.resolve(result);
    })
    //deferred.resolve( Ribbon._doRequest( settings, true ) );
  }).error( function() {
    console.log( "Unable to update token" );
    deferred.reject();
  })

  return deferred;
}

Ribbon._doRequest = function(settings, useToken) {
  if ( useToken ) {
    settings.headers.Authorization = 'Bearer ' + keycloak.token;
  }
  return $.ajax( settings );
}

Ribbon.getJSON = function(serviceName, url, data) {
  return Ribbon.ajax( serviceName, url, {
    dataType: 'json',
    data: data,
  })
}

Ribbon.postJSON = function(serviceName, url, data) {
  return Ribbon.ajax( serviceName, url, {
    dataType: 'json',
    method: 'POST',
    data: data,
  })
}
