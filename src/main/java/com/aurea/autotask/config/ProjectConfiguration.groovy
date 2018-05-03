package com.aurea.autotask.config

import groovy.transform.Canonical
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated

@Configuration
@ConfigurationProperties(prefix = 'project')
@EnableConfigurationProperties
@Canonical
@Validated
class ProjectConfiguration {
    String jira
    String reviewerJira

    String projectKey
    String desiredStatus
    String jiraIssue
    String summary
    String timeSpent

    User me

    static class User {
        String adUser
        String adPassword
    }
}
