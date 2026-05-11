package io.github.javamrcp.spring;

import io.github.javamrcp.server.MrcpServer;
import io.github.javamrcp.server.netty.NettyMrcpServer;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configures an embedded Netty MRCP server for Spring Boot applications.
 */
@AutoConfiguration
@ConditionalOnClass(NettyMrcpServer.class)
@EnableConfigurationProperties(MrcpServerProperties.class)
public class MrcpServerAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(MrcpServer.class)
    @ConditionalOnProperty(prefix = "mrcp.server", name = "enabled", havingValue = "true", matchIfMissing = true)
    MrcpServer mrcpServer(MrcpServerProperties properties) {
        return new NettyMrcpServer(properties.toConfig());
    }

    @Bean
    @ConditionalOnBean(MrcpServer.class)
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "mrcp.server", name = "enabled", havingValue = "true", matchIfMissing = true)
    MrcpServerLifecycle mrcpServerLifecycle(MrcpServer server) {
        return new MrcpServerLifecycle(server);
    }

    @Bean
    @ConditionalOnClass(HealthIndicator.class)
    @ConditionalOnBean(MrcpServer.class)
    @ConditionalOnMissingBean(name = "mrcpServerHealthIndicator")
    MrcpServerHealthIndicator mrcpServerHealthIndicator(MrcpServer server) {
        return new MrcpServerHealthIndicator(server);
    }

    @Bean
    @ConditionalOnClass(MeterRegistry.class)
    @ConditionalOnBean(MrcpServer.class)
    @ConditionalOnMissingBean
    MrcpServerMetrics mrcpServerMetrics(MeterRegistry meterRegistry, MrcpServer server) {
        return new MrcpServerMetrics(meterRegistry, server);
    }
}
