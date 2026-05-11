package io.github.javamrcp.spring;

import io.github.javamrcp.server.MrcpServer;
import io.github.javamrcp.server.netty.NettyMrcpServer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configures an embedded Netty MRCP server for Spring Boot applications.
 */
@AutoConfiguration
@ConditionalOnClass(NettyMrcpServer.class)
@EnableConfigurationProperties(MrcpServerProperties.class)
public class MrcpServerAutoConfiguration {
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean(MrcpServer.class)
    @ConditionalOnProperty(prefix = "mrcp.server", name = "enabled", havingValue = "true", matchIfMissing = true)
    MrcpServer mrcpServer(MrcpServerProperties properties) {
        return new NettyMrcpServer(properties.toConfig());
    }
}
