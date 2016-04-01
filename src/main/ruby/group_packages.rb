require 'fileutils'

GROUPINGS = {"Core" => ->(x) { x !~ /\.config\./ },
             "Config" => ->(x) { x =~ /\.config\./ }}
ORDER = %w{Core Config}                      
  
def read_overview_frame(path)
  content = {:pre => [], :packages => [], :post => []}
  position = :pre
  
  File.readlines(path).each do |line|
    case line
    when /ul title/
      position = :packages
    when /\/ul/
      position = :post
    else
      content[position] << line
    end
  end

  content
end

def read_overview_summary(path)
  content = {:pre => [], :packages => [], :post => []}
  position = :pre
  
  lines = File.readlines(path)
  idx = 0
  while (idx < lines.count)
    case lines[idx]
    when /table/
      position = :packages
      idx += 7
    when /\/tbody/
      position = :post
      idx += 2
    else
      if position == :packages
        package = ""
        4.times do
          package += lines[idx]
          idx += 1
        end
        content[position] << package
      else 
        content[position] << lines[idx]
        idx += 1
      end
    end
    
  end

  content
end

def group_packages(packages, groupings)
  result = Hash.new { |h, k| h[k] = [] } 
  packages.each do |p|
    groupings.each do |g,matcher|
      result[g] << p if matcher.call(p)
    end
  end

  result
end

def write_content(content, path)
  File.open(path, "w") do |f|
    content[:pre].each {|line| f.puts(line)}
    
    ORDER.each {|title| yield(f, title, content[:packages][title])}
    
    content[:post].each {|line| f.puts(line)}
  end
end

doc_path = "#{$ARGV[0]}/apidocs"

puts "Grouping packages for javadoc"

content = read_overview_frame("#{doc_path}/overview-frame.html")
content[:packages] = group_packages(content[:packages], GROUPINGS)
write_content(content, "#{doc_path}/overview-frame.html") do |f, title, packages|
  f.puts "<h3>#{title}</h3><ul>"
  packages.each {|line| f.puts(line)}
  f.puts "</ul>"
end

content = read_overview_summary("#{doc_path}/overview-summary.html")
content[:packages] = group_packages(content[:packages], GROUPINGS)
write_content(content, "#{doc_path}/overview-summary.html") do |f, title, packages|
  f.puts %Q{<table class="overviewSummary" border="0" cellpadding="3" cellspacing="0" summary="#{title} table, listing packages, and an explanation">
<caption><span>#{title}</span><span class="tabEnd">&nbsp;</span></caption>
<tr>
<th class="colFirst" scope="col">Package</th>
<th class="colLast" scope="col">Description</th>
</tr>
<tbody>}
  packages.each {|line| f.puts(line)}
  f.puts "</tbody></table>"
end
