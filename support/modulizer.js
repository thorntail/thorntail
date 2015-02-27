
var fs = require('fs')
var path = require('path');
var parse = require('xml-parser')

var Handlebars = require('handlebars');

var source = fs.readFileSync( 'template.java', 'utf8' );
var template = Handlebars.compile(source);

console.log( "template", template );

var root = '/Users/bob/repos/wildfly-core/build/target/wildfly-core-1.0.0.Beta1-SNAPSHOT/modules';

var walkable = [ root ];
var modules = [];

while ( walkable.length > 0 ) {
  var dir = walkable.pop();
  crawlDir( dir );
}

modules.forEach( function(e) { 
  processModule( e );
} )



function crawlDir(dir) {
  console.log( "crawl", dir );
  var children = fs.readdirSync( dir );

  children.forEach( function(e) {
    var stat = fs.statSync( dir + '/' + e );
    //console.log( stat );
    if ( stat.isDirectory() ) {
      walkable.push( dir + '/' + e );
    } else if ( e == 'module.xml' ) {
      modules.push( dir + '/module.xml' );
    }
  } )
}

function processModule(moduleXml) {
  var path1 = path.relative( root, moduleXml );
  var path2 = path.relative( 'system/layers/base', path1 );

  var parts = path2.split('/');
  parts.pop(); // module.xml

  var package = "modules.system.layers.base." + parts.join('.');
  var slot = parts.pop();
  var name = parts.join('.');
  console.log( '* ' + name + ' :: ' + slot );
 
  var xml = fs.readFileSync( moduleXml, 'utf8'  );
  var obj = parse( xml );

  var context = { };

  context.name = name;
  context.slot = slot;
  context.package = package;

  //console.log( obj );
  //console.log( obj.root.children.dependencies.children )

  obj.root.children.forEach( function(e) {
    if ( e.name == 'resources' ) {
    } else if ( e.name == 'dependencies' ) {
      e.children.forEach( function(e) {
        if ( e.name == 'module' ) {
          console.log( "MODULE", e );
        } else if ( e.name == 'system' ) {
          console.log( "SYSTEM", e );
        }
      } );
    }
    console.log( "DONE CHILD", e );
  } );

  genereateJava( context );

  //console.log( obj );
}


function genereateJava(context) {
  console.log( template( context ) );
}


