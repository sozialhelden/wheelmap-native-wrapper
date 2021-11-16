import Foundation

public struct ConfigLoader {
    
    /// Determine absolute path of the given argument relative to the current
    /// directory
    private static func getAbsolutePath(relativePath: String) -> String? {
        let fileManager = FileManager.default
        let currentPath = fileManager.currentDirectoryPath
        let filePath = currentPath + relativePath
        if fileManager.fileExists(atPath: filePath) {
            return filePath
        } else {
            return nil
        }
    }
    
    /// Load plist configuration file and put all the variables into the dictionary
    public static func load(plistPath: String) -> [String: String] {
//        let plistPath = getAbsolutePath(relativePath: "/\(filename)")
        var result: [String: String] = [:]
        
        var propertyListFormat =  PropertyListSerialization.PropertyListFormat.xml //Format of the Property List.
        let plistXML = FileManager.default.contents(atPath: plistPath)!
        
        do {
            //convert the data to a dictionary and handle errors.
            result = try PropertyListSerialization.propertyList(
                from: plistXML,
                options: .mutableContainersAndLeaves,
                format: &propertyListFormat) as![String:String]
        } catch {
            print("Error reading plist: \(error), format: \(propertyListFormat)")
        }
        return result
    }
}
