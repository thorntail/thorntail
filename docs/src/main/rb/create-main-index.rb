
require 'open-uri'

versions = []

open('https://raw.githubusercontent.com/wildfly-swarm/docs.wildfly-swarm.io/master/versions.txt') do |io|
  versions = io.readlines
end

versions.unshift ARGV[0]
versions.uniq!
versions.reject! {|e| ! e }
versions.reject! {|e| e.strip == ''}

open('target/generated-docs/versions.txt', 'w') do |out|
  versions.each do |v|
    out.puts v.strip
  end
end

open('target/index.adoc', 'w') do |out|
  out.puts "= WildFly Swarm Documentation"
  out.puts
  out.puts ".Available Versions"
  out.puts
  versions.each do |v|
    out.puts "* link:./#{v.strip}/[#{v.strip}]"
  end

end
