package io.github.javamrcp.spring;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.javamrcp.server.MrcpServer;
import io.github.javamrcp.server.netty.NettyMrcpServer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class MrcpServerAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(org.springframework.boot.autoconfigure.AutoConfigurations.of(
                    MrcpServerAutoConfiguration.class));

    @Test
    void createsNettyServerWhenEnabled() {
        contextRunner
                .withPropertyValues(
                        "mrcp.server.sip-host=127.0.0.1",
                        "mrcp.server.sip-port=0",
                        "mrcp.server.mrcp-host=127.0.0.1",
                        "mrcp.server.mrcp-port=0",
                        "mrcp.server.rtp-port=0",
                        "mrcp.server.shutdown-quiet-period=0ms",
                        "mrcp.server.shutdown-timeout=2s")
                .run(context -> {
                    MrcpServer server = context.getBean(MrcpServer.class);

                    assertInstanceOf(NettyMrcpServer.class, server);
                    assertTrue(server.isRunning());
                    assertTrue(context.getBean(MrcpServerLifecycle.class).isRunning());
                    assertTrue(context.containsBean("mrcpServerHealthIndicator"));
                });
    }

    @Test
    void doesNotCreateServerWhenDisabled() {
        contextRunner
                .withPropertyValues("mrcp.server.enabled=false")
                .run(context -> assertFalse(context.containsBean("mrcpServer")));
    }

    @Test
    void backsOffWhenUserProvidesServerBean() {
        contextRunner
                .withUserConfiguration(UserServerConfiguration.class)
                .run(context -> {
                    MrcpServer server = context.getBean(MrcpServer.class);

                    assertInstanceOf(TestMrcpServer.class, server);
                    assertFalse(server.isRunning());
                });
    }

    @Test
    void contributesHealthAndMetricsWhenDependenciesArePresent() {
        contextRunner
                .withUserConfiguration(MetricsConfiguration.class)
                .withPropertyValues(
                        "mrcp.server.sip-host=127.0.0.1",
                        "mrcp.server.sip-port=0",
                        "mrcp.server.mrcp-host=127.0.0.1",
                        "mrcp.server.mrcp-port=0",
                        "mrcp.server.rtp-port=0",
                        "mrcp.server.shutdown-quiet-period=0ms",
                        "mrcp.server.shutdown-timeout=2s")
                .run(context -> {
                    assertEquals(Status.UP, context.getBean(MrcpServerHealthIndicator.class).health().getStatus());
                    assertTrue(context.getBean(SimpleMeterRegistry.class)
                            .find("mrcp.server.running")
                            .gauge()
                            .value() > 0.0d);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class UserServerConfiguration {
        @Bean
        MrcpServer testMrcpServer() {
            return new TestMrcpServer();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class MetricsConfiguration {
        @Bean
        SimpleMeterRegistry simpleMeterRegistry() {
            return new SimpleMeterRegistry();
        }
    }

    static class TestMrcpServer implements MrcpServer {
        @Override
        public CompletionStage<Void> start() {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletionStage<Void> stop() {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public boolean isRunning() {
            return false;
        }
    }
}
