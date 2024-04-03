package com.cetiti.config;


import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoDriverInformation;
import com.mongodb.client.MongoClient;
import com.mongodb.client.internal.MongoClientImpl;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Arrays;

@Configuration
@EnableMongoRepositories(basePackages = "com.cetiti.dao", mongoTemplateRef = MongoConfig.MONGO_TEMPLATE)
public class MongoConfig {

    public final static String MONGO_TEMPLATE = "MongoTemplate";

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.mongodb")
    public MongoProperties mongoProperties() {
        return new MongoProperties();
    }

    @Primary
    @Bean(name = MONGO_TEMPLATE)
    public MongoTemplate shMongoTemplate(MongoProperties mongoProperties, ApplicationContext appContext) throws Exception {
        MongoDatabaseFactory factory = shFactory(mongoProperties);
        MongoMappingContext mongoMappingContext = new MongoMappingContext();
        mongoMappingContext.setApplicationContext(appContext);
        mongoMappingContext.afterPropertiesSet(); // 确保调用初始化方法

        MappingMongoConverter converter = new MappingMongoConverter(new DefaultDbRefResolver(factory), mongoMappingContext);
        converter.setCustomConversions(mongoCustomConversions()); // 设置自定义转换器
        converter.afterPropertiesSet(); // 确保调用初始化方法

        return new MongoTemplate(factory, converter);
    }

    @Bean
    @Primary
    public MongoDatabaseFactory shFactory(MongoProperties mongoProperties) {
        convertMongoProperties(mongoProperties);
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoProperties.getUri()))
                .build();
        MongoDriverInformation driverInformation = MongoDriverInformation.builder().build();
        MongoClient mongoClient = new MongoClientImpl(mongoClientSettings, driverInformation);
        return new SimpleMongoClientDatabaseFactory(mongoClient, mongoProperties.getDatabase());

    }

    public static void convertMongoProperties(MongoProperties mongoProperties) {
        String url = mongoProperties.getUri();
        String[] params = url.split(":");
        mongoProperties.setUsername(params[1].replace("//", ""));
        mongoProperties.setPassword(params[2].split("@")[0].toCharArray());
        mongoProperties.setHost(params[2].split("@")[1]);
        mongoProperties.setPort(Integer.parseInt(params[3].split("/")[0]));
        mongoProperties.setDatabase(params[3].split("/")[1]);
    }

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(Arrays.asList(
                // 添加您的自定义转换器
                new LocalDateTimeToDateConverter(),
                new DateToLocalDateTimeConverter()
        ));
    }
}