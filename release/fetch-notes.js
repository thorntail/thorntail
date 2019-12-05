var https = require('https')

var version = process.argv[2];

https.get( 'https://issues.redhat.com/rest/api/latest/search?maxResults=100&jql=project=THORN+AND+fixVersion=' + version + '+order+by+key+asc', function(result, err) {

  var buf = "";

  var partitions = {};
  var breaking = [];

  result.on( 'data', function(d) {
    buf = buf + d.toString();
  } );

  result.on( 'end', function() {
    var json = JSON.parse( buf );

    var jiraVersion;
 
    json.issues.forEach( function(e) {
      var members = partitions[e.fields.issuetype.name];
      jiraVersion = e.fields.fixVersions[0].id;
      if ( ! members ) {
        members = [];
        partitions[e.fields.issuetype.name] = members;
      }
      members.push( e );
      if (e.fields.labels && e.fields.labels.indexOf('breaking_change') >= 0) {
        breaking.push(e);
      }
    } );

    console.log( "== Changelog" );
    console.log( "Release notes for " + version + " are available https://issues.redhat.com/secure/ReleaseNote.jspa?projectId=12317020&version=" + jiraVersion + "[here]." );
    console.log();

    Object.keys( partitions ).forEach( function(type) {
      console.log( "=== " + type );
      partitions[type].forEach( function(e) {
        if ( e.fields.resolution ) {
          console.log( '* [https://issues.redhat.com/browse/' + e.key + '[' + e.key + ']] ' + e.fields.summary + ' (' + e.fields.resolution.name + ')' );
        }
      } );
      console.log();
    } );
    if (breaking.length > 0) {
      console.log("== Breaking changes");
      breaking.forEach(function(e) {
        console.log('* [https://issues.redhat.com/browse/' + e.key + '[' + e.key + ']] ' + e.fields.summary);
      });
    }
  } );

  result.resume();
});
