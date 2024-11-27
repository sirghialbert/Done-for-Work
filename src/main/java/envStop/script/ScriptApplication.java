package envStop.script;

import envStop.script.properties.ApplicationProperties;
import envStop.script.service.ScriptExecutorService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@RequiredArgsConstructor
@EnableScheduling
public class ScriptApplication {

    private final ScriptExecutorService scriptExecutorService;
    private final ApplicationProperties applicationProperties;

    public static void main(String[] args) {
        SpringApplication.run(ScriptApplication.class, args);
    }

    @PostConstruct
    public void executeScripts() {
        System.out.println("Configuration loaded:");
        System.out.println(applicationProperties.toString());

        System.out.println("Running script after startup...");
        scriptExecutorService.executeScriptIfLogsUnchanged();
    }
}
