package com.example.shorty.config;

import com.example.shorty.id.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.security.MessageDigest;

@Configuration
public class SnowflakeConfig {

    @Value("${app.shard-id:#{null}}")
    private Long configuredShardId;

    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator() throws Exception {
        long shardId;
        if (configuredShardId != null) {
            shardId = configuredShardId;
        } else {
            String hostname = InetAddress.getLocalHost().getHostName();
            byte[] hash = MessageDigest.getInstance("MD5").digest(hostname.getBytes());
            shardId = Math.abs(((hash[0] & 0xFF) << 8) | (hash[1] & 0xFF)) % 1024;
        }
        return new SnowflakeIdGenerator(shardId);
    }
}
