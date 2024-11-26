require 'nokogiri'

def set_xml_attribute(file_path, xpath, attribute_name, attribute_value)
  # Check if the file exists
  raise "File not found: #{file_path}" unless File.exist?(file_path)

  # Parse the XML file
  xml_content = File.read(file_path)
  doc = Nokogiri::XML(xml_content) { |config| config.default_xml.noblanks }

  # Locate the node using XPath
  target_node = doc.at_xpath(xpath)
  if target_node.nil?
    puts "No node found for XPath: #{xpath}"
    return
  end

  # Set or update the attribute
  target_node[attribute_name] = attribute_value
  puts "Updated #{attribute_name} to #{attribute_value} for node at #{xpath}"

  # Write the updated XML back to the file
  File.write(file_path, doc.to_xml)
end

