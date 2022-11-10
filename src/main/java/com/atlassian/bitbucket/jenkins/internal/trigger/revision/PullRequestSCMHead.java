package com.atlassian.bitbucket.jenkins.internal.trigger.revision;

import com.atlassian.bitbucket.jenkins.internal.model.BitbucketPullRequest;
import jenkins.plugins.git.GitBranchSCMHead;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import jenkins.scm.api.mixin.ChangeRequestSCMHead2;

import java.util.Objects;

public class PullRequestSCMHead extends SCMHead implements ChangeRequestSCMHead2 {

    private final BitbucketPullRequest pullRequest;
    private final SCMHead target;

    public PullRequestSCMHead(BitbucketPullRequest pullRequest) {
        
        super(pullRequest.getFromRef().getDisplayId());
        this.pullRequest = pullRequest;
        target = new GitBranchSCMHead(pullRequest.getToRef().getDisplayId());
    }

    @Override
    public ChangeRequestCheckoutStrategy getCheckoutStrategy() {
        return ChangeRequestCheckoutStrategy.HEAD;
    }

    @Override
    public String getId() {
        return Objects.toString(pullRequest.getId());
    }

    @Override
    public SCMHead getTarget() {
        return target;
    }

    @Override
    public String getOriginName() {
        return "PR-" + pullRequest.getId() + ' ' + pullRequest.getFromRef().getDisplayId();
    }
}
