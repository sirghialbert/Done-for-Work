package envStop.script.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Set;

public class PathUtils {

    public static boolean isValidScriptPath(Path path) {
        boolean isValid = Files.exists(path) && Files.isRegularFile(path);
        System.out.println("Verifying if script path is valid: " + path + " -> " + (isValid ? "Valid" : "Invalid"));
        return isValid;
    }

    public static boolean isDirectory(Path path) {
        boolean isDir = Files.isDirectory(path);
        System.out.println("Checking if path is a directory: " + path + " -> " + (isDir ? "Yes" : "No"));
        return isDir;
    }

    public static boolean isExcluded(Path path, Set<String> excludedPaths) {
        boolean excluded = excludedPaths.contains(path.toString());
        System.out.println("Checking if path is excluded: " + path + " -> " + (excluded ? "Excluded" : "Not excluded"));
        return excluded;
    }

    public static boolean exists(Path path) {
        boolean exists = Files.exists(path);
        System.out.println("Checking existence of path: " + path + " -> " + (exists ? "Exists" : "Does not exist"));
        return exists;
    }

    public static boolean isLogModifiedRecently(Path logsDir) {
        try {
            Instant twentyFourHoursAgo = Instant.now().minusSeconds(1);
            boolean modifiedRecently = Files.walk(logsDir)
                    .filter(Files::isRegularFile)
                    .anyMatch(file -> {
                        try {
                            BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
                            boolean modified = attrs.lastModifiedTime().toInstant().isAfter(twentyFourHoursAgo);
                            System.out.println("File " + file + " modified recently: " + modified);
                            return modified;
                        } catch (IOException e) {
                            System.err.println("Error checking modification time for file: " + file + " -> " + e.getMessage());
                            return false;
                        }
                    });
            return modifiedRecently;
        } catch (IOException e) {
            System.err.println("Error checking logs directory: " + logsDir + " -> " + e.getMessage());
            return false;
        }
    }
}
