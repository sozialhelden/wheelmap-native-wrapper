require 'nokogiri'

def add_metadata(file_path, keys)
  # Ensure the file exists
  raise "File not found: #{file_path}" unless File.exist?(file_path)

  # Parse the XML file
  xml_content = File.read(file_path)
  doc = Nokogiri::XML(xml_content) { |config| config.default_xml.noblanks }

  # Find the <application> node
  application_node = doc.at_xpath('//application')
  raise "No <application> node found in #{file_path}" if application_node.nil?

  # Iterate over the keys
  keys.each do |key|
    value = ENV[key]
    if value.nil? || value.empty?
      puts "Skipping key #{key} because no value is set in the environment."
      next
    end

    # Check if a <meta-data> element with the same android:name exists
    existing_metadata = application_node.at_xpath("./meta-data[@android:name='#{key}']")
    if existing_metadata
      # Update the value if it exists
      existing_metadata['android:value'] = value
      puts "Updated <meta-data> android:name=\"#{key}\" with value=\"#{value}\""
    else
      # Create and add a new <meta-data> element if it doesn't exist
      new_metadata = Nokogiri::XML::Node.new('meta-data', doc)
      new_metadata['android:name'] = key
      new_metadata['android:value'] = value
      application_node.add_child(new_metadata)
      puts "Added <meta-data> android:name=\"#{key}\" with value=\"#{value}\""
    end
  end

  # Write the updated XML back to the file
  File.write(file_path, doc.to_xml)

  puts "Metadata successfully added/updated in #{file_path}"
end


