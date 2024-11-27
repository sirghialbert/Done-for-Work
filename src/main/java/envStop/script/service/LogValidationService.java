package envStop.script.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LogValidationService {

    public boolean logsModifiedInLast24Hours(Path envPath, String logsDir) {
        Path logsDirectory = envPath.resolve(logsDir);
        if (!Files.exists(logsDirectory) || !Files.isDirectory(logsDirectory)) {
            return false;
        }

        try {
            return Files.walk(logsDirectory)
                    .filter(Files::isRegularFile)
                    .anyMatch(file -> {
                        try {
                            return Files.getLastModifiedTime(file).toInstant().isAfter(Instant.now().minusSeconds(86400));
                        } catch (IOException e) {
                            return false;
                        }
                    });
        } catch (IOException e) {
            return false;
        }
    }
}
