package envStop.script.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScriptExecutorService {

    private static final String SCRIPT_NAME = "Services.sh";
    private static final String LOGS_DIR_NAME = "logs";
    private static final String[] APPS_PATHS = {"hp595srv/apps", "hp595srv1/apps"};
    private static final String EXCLUDED_FILE_NAME = "excluded.txt";
    private Set<String> excludedPaths = new HashSet<>();

    public void executeScriptIfLogsUnchanged() {
        try {
            loadExcludedPaths();
            List<Path> envPaths = findEnvPaths();
            for (Path envPath : envPaths) {
                String envPathStr = envPath.toString();
                System.out.println("Verifying environment: " + envPath);


                if (excludedPaths.contains(envPathStr)) {
                    System.out.println("Environment " + envPath + " is excluded from execution.");
                    continue;
                }

                if (!logsModifiedInLast24Hours(envPath)) {
                    System.out.println("Logs in " + envPath + " have not been modified in the last 24 hours.");
                    executeScript(envPath);
                } else {
                    System.out.println("Logs have been modified in the last 24 hours in: " + envPath + ". Skipping execution.");
                }
            }
        } catch (IOException e) {
            System.err.println("Error accessing paths: " + e.getMessage());
        }
    }

    private void loadExcludedPaths() throws IOException {
        Path excludedFile = Paths.get(EXCLUDED_FILE_NAME);

        if (Files.exists(excludedFile)) {
            try (BufferedReader reader = new BufferedReader(new FileReader(excludedFile.toFile()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    excludedPaths.add(line.trim());
                }
                System.out.println("Excluded paths loaded from " + EXCLUDED_FILE_NAME);
            }
        } else {
            System.out.println("No excluded paths file found.");
        }
    }

    private List<Path> findEnvPaths() throws IOException {
        List<Path> envPaths = new ArrayList<>();

        for (String appsPath : APPS_PATHS) {
            Path appsDir = Paths.get(appsPath);

            if (!Files.exists(appsDir) || !Files.isDirectory(appsDir)) {
                throw new IOException("Directory " + appsPath + " does not exist or is not accessible.");
            }

            envPaths.addAll(Files.walk(appsDir, 1)
                    .filter(Files::isDirectory)
                    .filter(path -> !path.getFileName().toString().equals(LOGS_DIR_NAME))
                    .collect(Collectors.toList()));
        }

        return envPaths;
    }

    private boolean logsModifiedInLast24Hours(Path envPath) {
        Path logsDir = envPath.resolve(LOGS_DIR_NAME);
        if (!Files.exists(logsDir) || !Files.isDirectory(logsDir)) {
            System.out.println("Logs directory not found in: " + envPath);
            return false;
        }

        try {
            Instant twentyFourHoursAgo = Instant.now().minusSeconds(24*60*60);
            return Files.walk(logsDir)
                    .filter(Files::isRegularFile)
                    .anyMatch(file -> {
                        try {
                            BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
                            boolean modifiedRecently = attrs.lastModifiedTime().toInstant().isAfter(twentyFourHoursAgo);
                            if (modifiedRecently) {
                                System.out.println("File " + file + " in logs has been modified recently.");
                            }
                            return modifiedRecently;
                        } catch (IOException e) {
                            throw new RuntimeException("Error reading file attributes for: " + file, e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("Error checking logs directory: " + logsDir, e);
        }
    }

    public void executeScript(Path envPath) {
        Path scriptPath = envPath.resolve("Services.sh");

        if (Files.exists(scriptPath)) {
            try {
                String absoluteScriptPath =  scriptPath.toAbsolutePath().toString() ;

                String command = "bash -i -c 'source " + absoluteScriptPath + "'";
System.out.println(absoluteScriptPath);
                ProcessBuilder processBuilder = new ProcessBuilder(absoluteScriptPath + "-k");
                processBuilder.inheritIO();

                Process process = processBuilder.start();
                int exitValue = process.waitFor();

                if (exitValue == 0) {
                    System.out.println("Script executed successfully!");
                } else {
                    System.err.println("Script execution failed with exit code: " + exitValue);
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Failed to execute script: " + e.getMessage());
            }
        } else {
            System.out.println("Script not found at: " + scriptPath);
        }
    }


}
