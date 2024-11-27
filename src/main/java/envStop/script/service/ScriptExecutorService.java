package envStop.script.service;

import envStop.script.properties.ApplicationProperties;
import envStop.script.util.PathUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class ScriptExecutorService {

    private ApplicationProperties properties;

    public ScriptExecutorService(ApplicationProperties properties) {
        this.properties = properties;
    }

    public void executeScriptIfLogsUnchanged() {
        try {
            Set<String> excludedPaths = loadExcludedPaths();
            List<Path> envPaths = findEnvPaths();

            for (Path envPath : envPaths) {
                System.out.println("Processing environment: " + envPath);

                if (PathUtils.isExcluded(envPath, excludedPaths)) {
                    continue;
                }

                if (!PathUtils.isLogModifiedRecently(envPath.resolve(properties.getLogsDir()))) {
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

    private Set<String> loadExcludedPaths() throws IOException {
        Set<String> excludedPaths = new TreeSet<>();
        Path excludedFile = Paths.get(properties.getExcludedFilePath());

        if (Files.exists(excludedFile)) {
            try (BufferedReader reader = Files.newBufferedReader(excludedFile)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    excludedPaths.add(line.trim());
                    System.out.println("Excluded path loaded: " + line.trim());
                }
            }
        } else {
            System.out.println("No excluded paths file found.");
        }

        return excludedPaths;
    }

    private List<Path> findEnvPaths() throws IOException {
        List<Path> envPaths = new ArrayList<>();

        for (String basePath : properties.getUsersToCheck()) {
            Path appsDir = Paths.get(basePath, "apps");

            if (!Files.exists(appsDir) || !Files.isDirectory(appsDir)) {
                throw new IOException("Directory " + appsDir + " does not exist or is not accessible.");
            }

            System.out.println("Found apps directory: " + appsDir);
            envPaths.addAll(Files.walk(appsDir, 1)
                    .filter(Files::isDirectory)
                    .filter(path -> !path.getFileName().toString().equals(properties.getLogsDir()))
                    .collect(Collectors.toList()));
        }

        return envPaths;
    }

    private void executeScript(Path envPath) {
        Path scriptPath = envPath.resolve(properties.getScriptName());

        if (Files.exists(scriptPath)) {
            System.out.println("Executing script at: " + scriptPath);
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", scriptPath.toAbsolutePath().toString() + " -k");
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
