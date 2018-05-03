package com.aurea.autotask.jira

import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.api.NamedEntity
import com.atlassian.jira.rest.client.api.domain.Field
import com.atlassian.jira.rest.client.api.domain.Issue
import com.atlassian.jira.rest.client.api.domain.IssueType
import com.atlassian.jira.rest.client.api.domain.Transition
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue
import com.atlassian.jira.rest.client.api.domain.input.FieldInput
import com.atlassian.jira.rest.client.api.domain.input.IssueInput
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput
import groovy.util.logging.Log4j2
import one.util.streamex.StreamEx

@Log4j2
class JiraRequestHandler {

    private JiraCache jiraCache
    private JiraRestClient client

    JiraRequestHandler(JiraCache jiraCache, JiraRestClient client) {
        this.jiraCache = jiraCache
        this.client = client
    }

    void transition(Map<String, String> transitionPath) {
        String currentStatus
        while ((currentStatus = issue.status.name.toUpperCase()) != jiraCache.desiredStatus) {
            log.info "Current status: '$currentStatus', next transition: '${transitionPath[currentStatus]}'"

            /* ToDo: Fix this dirty hack */
            transition(getByNameOrFail(transitions, transitionPath[currentStatus]))
        }
    }

    void transition(Transition transition) {
        log.info "Transitioning to ${transition}..."

        client.issueClient.transition(issue, new TransitionInput(transition.id)).claim()
        refreshIssue()
    }

    Iterable<Transition> getTransitions() {
        log.info 'Getting transitions'

        client.issueClient.getTransitions(issue).claim()
    }

    private refreshIssue() {
        assert jiraCache.issue

        log.info 'Refreshing issue'

        jiraCache.issue = client.issueClient.getIssue(issue.key).claim()
    }

    Issue getIssue() {
        if (jiraCache.issue) {
            return jiraCache.issue
        }

        log.info 'Getting issue'

        jiraCache.issue = client.issueClient.getIssue(issueKey).claim()
    }

    String getIssueKey() {
        if (jiraCache.issueKey) {
            return jiraCache.issueKey
        }

        def issueType = jiraCache.parentIssue ? jiraCache.issueSubTaskType : jiraCache.issueType
        def issueTypeId = getByNameOrFail(issueTypes, issueType).id

        def builder = new IssueInputBuilder(jiraCache.projectKey, issueTypeId, jiraCache.summary)

        if (jiraCache.reviewer) {
            log.info "Setting reviewer to '$jiraCache.reviewer'"
            def reviewerField = new FieldInput(reviewerField.id, ComplexIssueInputFieldValue.with('name', jiraCache.reviewer))
            builder.setFieldInput(reviewerField)
        }

        if (jiraCache.parentIssue) {
            builder.setFieldValue('parent', ComplexIssueInputFieldValue.with('key', jiraCache.parentIssue))
        }

        log.info 'Creating issue input'

        IssueInput issueInput = builder
                .setAssigneeName(jiraCache.assignee)
                .build()

        log.info 'Creating issue'

        def basicIssue = client.issueClient.createIssue(issueInput).claim()

        log.info "Issue created: $basicIssue.key"

        jiraCache.issueKey = basicIssue.key
    }

    Field getReviewerField() {
        if (jiraCache.reviewerField) {
            return jiraCache.reviewerField
        }

        log.info 'Getting reviewer field'

        jiraCache.reviewerField = getByNameOrFail(fields, 'Reviewer')
    }

    Iterable<Field> getFields() {
        if (jiraCache.fields) {
            return jiraCache.fields
        }

        log.info 'Requesting fields meta information ...'

        jiraCache.fields = client.metadataClient.fields.claim()
    }

    Iterable<IssueType> getIssueTypes() {
        if (jiraCache.issueTypes) {
            return jiraCache.issueTypes
        }

        log.info 'Requesting issue types ...'
        jiraCache.issueTypes = client.metadataClient.issueTypes.claim()
    }

    static <T extends NamedEntity> T getByNameOrFail(Iterable<T> entities, String name) {
        StreamEx.of(entities.iterator()).findFirst { it.name == name }.orElseThrow {
            String names = StreamEx.of(entities.iterator()).map { it.getName() }.joining(', ')
            new IllegalStateException("Failed to find named entity $name in [$names]")
        }
    }
}
