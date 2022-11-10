package com.atlassian.bitbucket.jenkins.internal.trigger.revision;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.mixin.ChangeRequestSCMRevision;

public class PullRequestSCMRevision extends ChangeRequestSCMRevision<PullRequestSCMHead> {

    private final SCMRevision pullRequestTarget;
    
    public PullRequestSCMRevision(@NonNull PullRequestSCMHead head, @NonNull SCMRevision targetRevision, @NonNull SCMRevision pullRequestTarget) {
        super(head, pullRequestTarget);
        this.pullRequestTarget = pullRequestTarget;
    }

    @Override
    public boolean equivalent(ChangeRequestSCMRevision<?> o) {
        if (!(o instanceof PullRequestSCMRevision)) {
            return false;
        }
        PullRequestSCMRevision otherRevision = (PullRequestSCMRevision) o;
        return getHead().equals(otherRevision.getHead()) && pullRequestTarget.equals(otherRevision.pullRequestTarget);
    }

    @Override
    protected int _hashCode() {
        return pullRequestTarget.hashCode();
    }

    public SCMRevision getPullRequestTarget() {
        return pullRequestTarget;
    }
}
