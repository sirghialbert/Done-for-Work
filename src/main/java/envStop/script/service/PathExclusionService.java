package envStop.script.service;

import envStop.script.properties.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PathExclusionService {

    private final ApplicationProperties properties;

    public Set<String> loadExcludedPaths() throws IOException {
        Path excludedFile = Path.of(properties.getExcludedFilePath());
        Set<String> excludedPaths = new HashSet<>();

        if (Files.exists(excludedFile)) {
            try (BufferedReader reader = Files.newBufferedReader(excludedFile)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    excludedPaths.add(line.trim());
                }
            }
        }
        return excludedPaths;
    }

}
