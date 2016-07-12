require 'fileutils'
require 'set'

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

def collect_src(input_dir, output_dir)
  packages = Set.new
  Dir.glob("#{input_dir}/**/*.java")
    .reject {|path| IGNORE =~ path}
    .each do |path|
      _, rel_path = path.split(input_dir)
      dest = File.join(output_dir, rel_path)
      FileUtils.mkdir_p(File.dirname(dest))
      FileUtils.cp(path, dest)
      packages << File.split(rel_path).first.slice(1..-1).gsub("/", ".")
    end

  packages
end

def process(input_dir, output_dir)
  packages = {}
  versions = {}
  Dir.glob("#{input_dir}/*").each do |dir|
    version_file = File.join(dir, "_version")
    if File.exist?(version_file)
      dirname = File.basename(dir)
      packages[dirname] = collect_src(dir, output_dir)
      versions[dirname] = File.read(version_file).strip
    end
  end

  [packages, versions]
end

def marshal(x, f)
  File.open(f, "w+") do |f|
    Marshal.dump(x, f)
  end
end

target_dir, output_dir, dep_src_dir = $ARGV

puts "Copying dependency src to the javadoc tree"

clean(output_dir)
packages, versions = process(dep_src_dir, output_dir)

f = File.join(target_dir, "packages.dat")
puts "Marshaling package list to #{f}"
marshal(packages, f)

f = File.join(target_dir, "versions.dat")
puts "Marshaling version list to #{f}"
marshal(versions, f)
