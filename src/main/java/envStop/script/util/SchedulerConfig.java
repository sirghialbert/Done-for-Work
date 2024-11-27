package envStop.script.util;

import envStop.script.properties.ApplicationProperties;
import envStop.script.service.ScriptExecutorService;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@AllArgsConstructor
@Component
public class SchedulerConfig  {

    private final ScriptExecutorService scriptExecutorService;

    @Scheduled(cron = "0 * * * * *", zone = "Europe/Bucharest")
    public void executeScriptEveryMinute() {
        System.out.println("Running script every minute...");
        scriptExecutorService.executeScriptIfLogsUnchanged();
    }

}

