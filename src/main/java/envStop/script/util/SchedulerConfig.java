package envStop.script.util;

import envStop.script.service.ScriptExecutorService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SchedulerConfig implements CommandLineRunner {

    private final ScriptExecutorService scriptExecutorService;

    public SchedulerConfig(ScriptExecutorService scriptExecutorService) {
        this.scriptExecutorService = scriptExecutorService;
    }


    @Override
    public void run(String... args) {
        System.out.println("Running script immediately after startup...");
        scriptExecutorService.executeScriptIfLogsUnchanged();
    }


    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Bucharest")
    public void executeScriptEveryMinute() {
        System.out.println("Running script every minute...");
        scriptExecutorService.executeScriptIfLogsUnchanged();
    }
}

