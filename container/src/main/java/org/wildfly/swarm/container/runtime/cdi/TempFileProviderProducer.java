package org.wildfly.swarm.container.runtime.cdi;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.jboss.vfs.TempFileProvider;
import org.wildfly.swarm.bootstrap.util.TempFileManager;

/**
 * @author Bob McWhirter
 */
@Singleton
public class TempFileProviderProducer implements Runnable {

    private TempFileProvider tempFileProvider;

    @PostConstruct
    void init() throws IOException {
        File serverTmp = TempFileManager.INSTANCE.newTempDirectory("wildfly-swarm", ".d");
        System.setProperty("jboss.server.temp.dir", serverTmp.getAbsolutePath());

        ScheduledExecutorService tempFileExecutor = Executors.newSingleThreadScheduledExecutor();
        this.tempFileProvider = TempFileProvider.create("wildfly-swarm", tempFileExecutor, true);

        Runtime.getRuntime().addShutdownHook(new Thread(this) );
    }

    @Override
    public void run() {
        try {
            this.tempFileProvider.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Produces
    TempFileProvider tempFileProvider() {
        return this.tempFileProvider;
    }

}
