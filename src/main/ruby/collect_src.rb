require 'fileutils'

IGNORE = Regexp.compile(
         %w{archetype
            arquillian/adapter
            arquillian/daemon
            arquillian/resources
            bootstrap
            fractionlist
            internal
            org/jboss/modules
            plugin
            runtime
            swarmtool
            tools}
         .map {|s| "/#{s}/"}
         .join("|")
)

def clean(d)
  FileUtils.rm_rf(d)
end

def collect_src(output_dir, glob, ignore, split_on)
  Dir.glob(glob)
    .reject {|path| ignore && ignore =~ path}
    .each do |path|
    _, rel_path = path.split(split_on)
    dest = File.join(output_dir, rel_path)
    FileUtils.mkdir_p(File.dirname(dest))
    FileUtils.cp(path, dest)
  end
end

output_dir, dep_src_dir = $ARGV

puts "Copying dependency src to the javadoc tree"

clean(output_dir)
collect_src(output_dir, "#{Dir.pwd}/../**/src/main/java/**/*.java", IGNORE, "src/main/java")
collect_src(output_dir, "#{dep_src_dir}/**/*.java", IGNORE, "depSources")
