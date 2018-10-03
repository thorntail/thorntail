
require 'open-uri'

versions = []

open('https://raw.githubusercontent.com/thorntail/docs.thorntail.io/master/versions.txt') do |io|
  versions = io.readlines
end

versions.unshift ARGV[0]
versions.uniq!
versions.reject! {|e| ! e }
versions.reject! {|e| e.strip == ''}

seenSnapshot = false
versions.reject! {|e|
  puts "test #{e}"
  result = false;
  if ( !e.include?('4.0.0') && e.include?('-SNAPSHOT') )
    if ( seenSnapshot )
      puts "reject-1 #{e}"
      result = true;
    else
      puts "seen snap #{e}"
      seenSnapshot = true;
    end
  end
  result
}

open('target/generated-docs/versions.txt', 'w') do |out|
  versions.each do |v|
    puts "add version #{v}"
    out.puts v.strip
  end
end

open('target/index.adoc', 'w') do |out|
  out.puts "= Thorntail Documentation"
  out.puts
  out.puts ".Available Versions"
  out.puts
  versions.each do |v|
    out.puts "* link:./#{v.strip}/[#{v.strip}]"
  end

end
