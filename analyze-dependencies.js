#! /usr/bin/env node
var fs = require('fs')

var root = process.argv[2];

var module_paths = [
  '../wildfly-core/core-feature-pack/src/main/resources/modules/system/layers/base/',
  '../wildfly/web-feature-pack/src/main/resources/modules/system/layers/base/',
  '../wildfly/feature-pack/src/main/resources/modules/system/layers/base/'
];

var modules = [];

function findModule(name) {
  modules.push( name );
  name = name.replace( /\./g, '/' );

  for ( var i = 0 ; i < module_paths.length ; ++i ) {
    var test = module_paths[i] + name + '/main/module.xml';
    if ( fs.existsSync( test ) ) {
      return test;
    }
  };
}

function analyzeModule(moduleXml) {
  var content = fs.readFileSync( moduleXml );
  var lines = content.toString().split('\n');
  for ( var i = 0 ; i < lines.length ; ++i ) {
    var result = lines[i].match( /module\s+name=\"([^"]+)\"/ );
    if ( result ) {
      var optionalResult = lines[i].match( /optional/ );
      if ( ! optionalResult ) {
        modules.push( result[1] );
      }
    }
  }
}

console.log( "search", module_paths );
console.log( "root", root );

analyzeModule( findModule(root) );

console.log( modules );
