import Foundation

public struct ConfigLoader {
    
    ///
    /// Determine absolute path of the given argument relative to the current
    /// directory
    ///
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
    
    ///
    /// Load .env file and put all the variables into the dictionary
    ///
    public static func loadDotEnvFile(filename: String) -> [String: String] {
        let path = getAbsolutePath(relativePath: "/\(filename)")
        var result: [String: String] = [:]
        
        if let path = path, let contents = try? NSString(contentsOfFile: path, encoding: String.Encoding.utf8.rawValue) {
            let lines = String(describing: contents).split { $0 == "\n" || $0 == "\r\n" }
                .map(String.init)
                .map({s in s.trimmingCharacters(in: .whitespaces)})
            
            for line in lines {
                
                // ignore empty lines
                if line.isEmpty {
                    continue
                }
                
                // ignore comments
                if line[line.startIndex] == "#" {
                    continue
                }
                
                // extract key and value which are separated by an equals sign
                let parts = line.split(separator: "=", maxSplits: 1).map(String.init)
                
                let key = parts[0].trimmingCharacters(in: .whitespaces)
                var value = parts[1].trimmingCharacters(in: .whitespaces)
                
                // remove surrounding quotes from value & convert remove escape character before any embedded quotes
                if value[value.startIndex] == "\"" && value[value.index(before: value.endIndex)] == "\"" {
                    value.remove(at: value.startIndex)
                    value.remove(at: value.index(before: value.endIndex))
                    value = value.replacingOccurrences(of:"\\\"", with: "\"")
                }
                result[key] = value
            }
        }
        
        return result
    }
}
