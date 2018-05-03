package com.aurea.autotask.jira

import com.atlassian.jira.rest.client.api.domain.Issue
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import com.aurea.autotask.config.ProjectConfiguration

import java.util.concurrent.Callable

class CreateJiraTask implements Callable<Issue> {
    private static final Map<String, String> TRANSITIONS_PATH = [
            'OPEN'           : 'Start work',
            'IN PROGRESS'    : 'Resolve',
            'RESOLVED'       : 'Start verification',
            'IN VERIFICATION': 'Accept'
    ]

    private ProjectConfiguration projectConfiguration
    private JiraCache jiraCache

    CreateJiraTask(ProjectConfiguration projectConfiguration) {
        this.projectConfiguration = projectConfiguration
        projectConfiguration.with {
            jiraCache = new JiraCache(projectKey, summary, reviewerJira, desiredStatus, me.adUser,
                    jiraIssue, timeSpent, issueType, issueSubTaskType, parentIssue)
        }
    }

    @Override
    Issue call() throws Exception {
        AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory()
        factory.createWithBasicHttpAuthentication(new URI(projectConfiguration.jira), projectConfiguration.me.adUser,
                projectConfiguration.me.adPassword).withCloseable {

            def jiraRequestHandler = new JiraRequestHandler(jiraCache, it)

            def issue = jiraRequestHandler.issue

            jiraRequestHandler.transition(TRANSITIONS_PATH)

            issue
        }
    }
}
