var exec = require('child_process').exec;

var prevVersion = process.argv[2]
var thisVersion = process.argv[3]

var cmd = 'git log ' + prevVersion + '..' + thisVersion + ' --format="%aN" | sort -u -k2,2';

exec( cmd, function(err, stdout, stderr) {
  console.log( stdout );
} );
