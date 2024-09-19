package com.demo.filmdb.graphql;

import com.demo.filmdb.graphql.config.NonBlankString;
import graphql.scalars.ExtendedScalars;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
class TestConfigurer {

    @Bean("testGraphQLConfigurer")
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .scalar(NonBlankString.INSTANCE)
                .scalar(ExtendedScalars.GraphQLLong)
                .scalar(ExtendedScalars.Date);
    }
}
