package com.demo.filmdb.graphql.config;

import graphql.scalars.ExtendedScalars;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
@PropertySource("classpath:graphql/graphql.properties")
public class GraphQLConfig {
    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .scalar(ExtendedScalars.Date)
                .scalar(ExtendedScalars.GraphQLLong)
                .scalar(NonBlankString.INSTANCE);
    }
}
