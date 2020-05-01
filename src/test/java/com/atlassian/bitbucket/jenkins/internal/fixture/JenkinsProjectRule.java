package com.atlassian.bitbucket.jenkins.internal.fixture;

import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.scm.SCM;
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.test.JenkinsRule;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

/**
 * A test rule designed to be used as part of a test chain to provide your tests with the ability to run against multiple
 * kinds of supported Jenkins projects, and ensure your features have compatibility without having to write a separate
 * test case for each one. The rule is designed for black-box testing (treating any kind of Jenkins project as a generic
 * {@link hudson.model.Job}), and can be customized to run with any chosen set of projects if your feature is not
 * intended to have full compatibility. This rule can also be used to handle project instantiation and cleanup for individual jobs.
 */
public class JenkinsProjectRule implements TestRule {

    // The set of all project types that can be tested by this rule, specified in the builder
    private final Set<ProjectType> testedProjectTypes;
    // The chained JenkinsRule, used to instantiate projects
    private final JenkinsRule jenkinsRule;

    // The job available to the integration test
    private Job activeJob;
    private ProjectType activeJobType;
    // The list of all jobs that have been created as part of testing this job
    private List<Job> instantiatedJobs = new ArrayList<>();

    private JenkinsProjectRule(Builder builder) {
        this.jenkinsRule = builder.jenkinsRule;
        this.testedProjectTypes = builder.projectTypes;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                // If the method was not annotated with a project initialization, run as normal
                if (testedProjectTypes.isEmpty()) {
                    base.evaluate();
                } else {
                    try {
                        initializeProjects();
                        for (ProjectType projectType : testedProjectTypes) {
                            setActiveJob(projectType);
                            base.evaluate();
                        }
                    } finally {
                        cleanupProjects();
                    }
                }
            }
        };
    }

    /**
     * Returns the job that the project rule is currently testing.
     *
     * @return the currently active job
     */
    public Job getJob() {
        return activeJob;
    }

    /**
     * Sets the SCM of the currently active job
     *
     * @param scm the SCM to add to the currently active job
     */
    public void setSCM(SCM scm) throws IOException {
        switch(activeJobType) {
            case FREESTYLE:
                ((FreeStyleProject) activeJob).setScm(scm);
                break;
            case PIPELINE:
                ((WorkflowJob) activeJob).setDefinition(new CpsScmFlowDefinition(scm, "Jenkinsfile"));
                break;
            case MULTIBRANCH:
                // TODO: Add Multibranch Support
                break;
        }
    }

    /**
     * Gets the SCM of the currently active job
     *
     * @return the SCM of the currently active job
     */
    @Nullable
    public SCM getSCM() {
        switch(activeJobType) {
            case FREESTYLE:
                return ((FreeStyleProject) activeJob).getScm();
            case PIPELINE:
                return ((CpsScmFlowDefinition) ((WorkflowJob) activeJob).getDefinition()).getScm();
            case MULTIBRANCH:
                // TODO: Add Multibranch Support
                return null;
            default:
                return null;
        }
    }

    private void setActiveJob(ProjectType type) {
        activeJob = instantiatedJobs.stream()
                .filter(job -> job.getClass() == type.getJobClass())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Project of type " + type.toString() + " not found."));
        activeJobType = type;
    }

    private void cleanupProjects() throws IOException, InterruptedException {
        for (Job job : instantiatedJobs) {
            job.delete();
        }
    }

    private void initializeProjects() throws IOException {
        for (ProjectType projectType : testedProjectTypes) {
            switch(projectType) {
                case MULTIBRANCH:
                    // TODO: Add Multibranch Support
                    break;
                default:
                    instantiatedJobs.add((Job) jenkinsRule.createProject(projectType.getTopLevelItemClass()));
                    break;
            }
        }
    }

    public static class Builder {

        public Set<ProjectType> projectTypes = new HashSet<>();
        public JenkinsRule jenkinsRule;

        public Builder(JenkinsRule jenkinsRule) {
            this.jenkinsRule = jenkinsRule;
        }

        public JenkinsProjectRule build() {
            return new JenkinsProjectRule(this);
        }

        public Builder withAllSupportedJobs() {
            projectTypes = EnumSet.allOf(ProjectType.class);
            return this;
        }

        public Builder withFreestyleJob() {
            projectTypes.add(ProjectType.FREESTYLE);
            return this;
        }

        public Builder withMultibranchPipelineJob() {
            // TODO: Add Mutlibranch Support
            throw new UnsupportedOperationException("Multibranch Jobs are not yet supported");
        }

        public Builder withPipelineJob() {
            projectTypes.add(ProjectType.PIPELINE);
            return this;
        }
    }
}