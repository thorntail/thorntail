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
  Dir.glob("#{input_dir}/*").each do |dir|
    packages[File.basename(dir)] = collect_src(dir, output_dir)
  end

  packages
end

def store_package_list(packages, f)
  File.open(f, "w+") do |f|
    Marshal.dump(packages, f)
  end
end

target_dir, output_dir, dep_src_dir = $ARGV

puts "Copying dependency src to the javadoc tree"

clean(output_dir)
packages = process(dep_src_dir, output_dir)
f = File.join(target_dir, "packages.dat")

puts "Marshaling package list to #{f}"

store_package_list(packages, f)
