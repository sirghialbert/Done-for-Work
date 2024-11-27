package envStop.script;

import envStop.script.properties.ApplicationProperties;
import envStop.script.service.ScriptExecutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.CountDownLatch;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@RequiredArgsConstructor
public class ScriptApplication implements InitializingBean {

    private final ScriptExecutorService scriptExecutorService;
    private final ApplicationProperties applicationProperties;

    @Value("${runInitDevMode:false}")
    private boolean runInit;

    public static void main(String[] args) throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(countDownLatch::countDown));

        SpringApplication.run(ScriptApplication.class, args);

        countDownLatch.await();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!runInit) return;
        System.out.println("Configuration loaded:");
        System.out.println(applicationProperties.toString());

        System.out.println("Running script after startup...");
        scriptExecutorService.executeScriptIfLogsUnchanged();
    }

}
