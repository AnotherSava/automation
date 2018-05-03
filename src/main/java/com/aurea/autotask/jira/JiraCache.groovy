package com.aurea.autotask.jira

import com.atlassian.jira.rest.client.api.domain.Field
import com.atlassian.jira.rest.client.api.domain.Issue
import com.atlassian.jira.rest.client.api.domain.IssueType

class JiraCache {
    final String projectKey
    final String summary
    final String reviewer
    final String desiredStatus
    final String assignee
    final String timeSpent

    String issueKey

    Issue issue
    Iterable<IssueType> issueTypes
    Iterable<Field> fields
    Field reviewerField

    JiraCache(String projectKey, String summary, String reviewer, String desiredStatus, String assignee, String issueKey, String timeSpent) {
        this.projectKey = projectKey
        this.summary = summary
        this.reviewer = reviewer
        this.desiredStatus = desiredStatus
        this.assignee = assignee
        this.issueKey = issueKey
        this.timeSpent = timeSpent
    }
}