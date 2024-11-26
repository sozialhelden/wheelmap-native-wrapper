require 'nokogiri'

def update_xml_text(file_path, xpath_expr, new_value)
  # Ensure the file exists
  raise "File not found: #{file_path}" unless File.exist?(file_path)

  # Parse the XML file
  xml_content = File.read(file_path)
  doc = Nokogiri::XML(xml_content) { |config| config.default_xml.noblanks }

  # Find the node(s) using the XPath expression
  node = doc.at_xpath(xpath_expr)
  raise "No node found for XPath: #{xpath_expr}" if node.nil?

  # Update the node's text content
  node.content = new_value
  puts "Updated node at XPath '#{xpath_expr}' with value: #{new_value}"

  # Write the updated XML back to the file
  File.write(file_path, doc.to_xml)

  puts "Updated XML file: #{file_path}"
end
