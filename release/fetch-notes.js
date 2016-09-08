
var https = require('https')

console.log( "release notes for: ", process.argv[2] );

var version = process.argv[2];

https.get( 'https://issues.jboss.org/rest/api/latest/search?jql=project=SWARM%20AND%20fixVersion=' + version, function(result, err) {

  var buf = "";

  var partitions = {};

  result.on( 'data', function(d) {
    buf = buf + d.toString();
  } );

  result.on( 'end', function() {
    var json = JSON.parse( buf );
    json.issues.forEach( function(e) {
      var members = partitions[e.fields.issuetype.name];
      if ( ! members ) {
        members = [];
        partitions[e.fields.issuetype.name] = members;
      }
      members.push( e );
    } );
    Object.keys( partitions ).forEach( function(type) {
      console.log( "=== " + type );
      partitions[type].forEach( function(e) {
        console.log( '* [https://issues.jboss.org/browse/' + e.key + '[' + e.key + ']] ' + e.fields.summary );
      } );
      console.log( " " );
    } );
  } );

  result.resume();
});
