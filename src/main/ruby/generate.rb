require('fileutils')

puts "Generating fraction list at #{Dir.pwd} "

def generate()
  roots = []

  deps = collect_first_order_dependencies( File.open( "#{Dir.pwd}/target/dependencies.info" ), 'provided' )

  for each in deps do
    root = {}
    root['name'] = simplify(each)
    root['deps'] = collect_first_order_dependencies( load_dependencies(each) )
    roots << root
  end

  outputDir = File.join( '.', 'target', 'classes' );

  FileUtils.mkdir_p( outputDir )
  
  File.open( File.join( outputDir, 'fraction-list.txt' ), 'w' ) do |f|
    for root in roots do 
      name = root['name']
      deps = root['deps']
      f.puts "#{name} = #{filter(roots,deps).join(', ')}"
    end
  end
end

def filter(roots,deps) 
  return deps.select{|e| roots.collect{|r|r['name']}.include?(e) }
end

def simplify(gav) 
  parts = gav.split(':')
  return "#{parts[0]}:#{parts[1]}"
end

def collect_first_order_dependencies(input, scope = 'compile' ) 
  deps = []
  input.each_line do |line|
    if ( line[/\A */].size == 3 )
      parts = line.strip.split(':')
      if ( parts[0] != 'org.wildfly.swarm' )
        next
      end
      if ( parts[4] != scope )
        next
      end
      deps << "#{parts[0]}:#{parts[1]}" 
    end
  end
  return deps
end

def load_dependencies(gav)
  parts = gav.strip.split(':')
  core = parts[1]

  path = File.join( Dir.pwd, '..', core, 'api', 'target', 'dependencies.info' )
  if ( File.exists?( path ) ) 
    return File.open( path );
  end

  path = File.join( Dir.pwd, '..', core, 'target', 'dependencies.info' )
  if ( File.exists?( path ) ) 
    return File.open( path );
  end

  puts "NOT FOUND #{core}"

end


generate()
