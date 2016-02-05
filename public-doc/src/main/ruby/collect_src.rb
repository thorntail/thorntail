require 'fileutils'

IGNORE = Regexp.compile(
         %w{archetype
            arquillian/adapter
            arquillian/daemon
            arquillian/resources
            bootstrap
            internal
            org/jboss/modules
            plugin
            runtime
            swarmtool
            target
            tools}
         .map {|s| "/#{s}/"}
         .join("|")
)

OUTPUT_DIR = File.join(Dir.pwd, "target", "combined-src")

def clean
  FileUtils.rm_rf(OUTPUT_DIR)
end

def collect_src(glob, ignore, split_on)
  Dir.glob(glob)
    .reject {|path| ignore && ignore =~ path}
    .each do |path|
    _, rel_path = path.split(split_on)
    dest = File.join(OUTPUT_DIR, rel_path)
    FileUtils.mkdir_p(File.dirname(dest))
    FileUtils.cp(path, dest)
  end
end

puts "Copying dependency src to the javadoc tree"

clean
collect_src("#{Dir.pwd}/../**/src/main/java/**/*.java", IGNORE, "src/main/java")
collect_src("#{Dir.pwd}/target/depSources/**/*.java", nil, "depSources")
