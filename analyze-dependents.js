#! /usr/bin/env node
var fs = require('fs')

var target = process.argv[2];

var module_paths = [
  '../wildfly-core/core-feature-pack/src/main/resources/modules/system/layers/base/',
  '../wildfly/web-feature-pack/src/main/resources/modules/system/layers/base/',
  '../wildfly/feature-pack/src/main/resources/modules/system/layers/base/'
];

var modules = [];

function walkAll() {
  module_paths.forEach( function(path) {
    walk(path);
  } );
}

function walk(path) {
  var stat = fs.statSync(path);
  if ( stat.isDirectory() ) {
    var children = fs.readdirSync(path);
    children.forEach( function(child) {
      walk( path + '/' + child );
    } );
  } else if ( path.indexOf( "module.xml" ) == ( path.length - 10 ) ) {
    analyzeModule( path ); 
  }
}

function analyzeModule(moduleXml) {
  var content = fs.readFileSync( moduleXml );
  var lines = content.toString().split('\n');
  var thisModuleName;
  for ( var i = 0 ; i < lines.length ; ++i ) {
    var result = lines[i].match( /module.+name=\"([^"]+)\"/ );
    if ( result ) {
      if ( ! thisModuleName ) {
        thisModuleName = result[1];
        continue;
      }
      if ( result[1] == target ) {
        var optionalResult = lines[i].match( /optional/ );
        if ( ! optionalResult ) {
          modules.push( thisModuleName );
        }
      }
    }
  }
}

function unique(list) {
  var u = [];

  list.forEach(function(item) {
    if ( u.indexOf( item ) <= 0  ) {
      u.push( item );
    }
  })

  return u;
}

console.log( "search", module_paths );
console.log( "target", target );

walkAll();

console.log( unique( modules ) );
