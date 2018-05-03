package com.aurea.autotask

import com.atlassian.jira.rest.client.api.domain.Issue
import com.aurea.autotask.config.ProjectConfiguration
import com.aurea.autotask.jira.CreateJiraTask
import groovy.util.logging.Log4j2
import net.jodah.failsafe.Failsafe
import net.jodah.failsafe.RetryPolicy
import net.jodah.failsafe.event.ContextualResultListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

@Log4j2
@Component
class InternalReviewRunner {

    private ProjectConfiguration projectConfiguration

    @Autowired
    InternalReviewRunner(ProjectConfiguration projectConfiguration) {
        this.projectConfiguration = projectConfiguration
    }

    void run() {
        def issue = createJiraIssue()

        def issueKey = "$issue.key"
        def issueLink = "${projectConfiguration.jira}/browse/$issueKey"

        log.info "JIRA: $issueLink"
    }

    private Issue createJiraIssue() {

        log.info 'Trying to create JIRA task ...'

        Callable<Issue> createJiraTask = new CreateJiraTask(projectConfiguration)

        RetryPolicy socketTimeoutRetryPolicy = new RetryPolicy()
                .retryOn(SocketTimeoutException, ConnectException)
                .withMaxRetries(10)
                .withDelay(10, TimeUnit.SECONDS)
                .withJitter(500, TimeUnit.MILLISECONDS)

        Failsafe.with(socketTimeoutRetryPolicy)
                .onRetry({ c, f, ctx -> log.info "Retrying to create JIRA task [${ctx.executions}] ... "} as ContextualResultListener)
                .get(createJiraTask)
    }
}
