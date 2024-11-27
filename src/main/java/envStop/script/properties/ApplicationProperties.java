package envStop.script.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "stop.services")
public class ApplicationProperties {
    private String excludedFilePath;
    private String logsDir;
    private List<String> usersToCheck;
    private String scriptName;
    @Override
    public String toString() {
        return "ApplicationProperties{" +
                "excludedFilePath='" + excludedFilePath + '\'' +
                ", logsDir='" + logsDir + '\'' +
                ", usersToCheck=" + usersToCheck +
                '}';
    }
}
