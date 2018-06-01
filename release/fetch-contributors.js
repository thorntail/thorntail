var exec = require('child_process').exec;

var prevVersion = process.argv[2]
var thisVersion = process.argv[3]
var websiteRepo = process.argv[4] || 'wildfly-swarm-swarm.io'
var examplesRepo = process.argv[6] || 'thorntail-examples'

if ( ! prevVersion) {
  console.log( "Must specify previous version" );
  console.log( "usaged: node fetch-contributors.js <prevVersion> <thisVersion>" );
  process.exit(1);
}

if ( ! thisVersion ) {
  console.log( "Must specify this version" );
  console.log( "usaged: node fetch-contributors.js <prevVersion> <thisVersion>" );
  process.exit(1);
}

var cmd = 'git log ' + prevVersion + '..' + thisVersion + ' --format="%aN" | sort -u -k2,2';

var repos = {
  Core: '.',
  Examples: '../../' + examplesRepo,
  Website: '../../' + websiteRepo,
}

Object.keys(repos).forEach( function(e) {
  var path = repos[e];
  exec( cmd, { cwd: path }, function(err, stdout, stderr) {
    var lines = stdout.split("\n").filter( function(e){ return e!="" && e!="WildFly Swarm CI"} );
    if ( lines.length > 0 ) {
      console.log( "=== " + e );
      lines.forEach( function(line) {
        console.log( "* " + line );
      } )
      console.log( "" );
    }
  } );
} );
