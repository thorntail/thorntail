# Contributing to WildFly Swarm

We welcome contributions of any kind to WildFly Swarm from everyone.

We expect all contributors and users to follow our [Code of Conduct]code_of_conduct.md when communicating through project channels.
These include, but are not limited to: irc, issues, code, Google Group discussions.

Building WildFly Swarm requires Java 8 or newer and Maven 3.2.5 or newer.

## One time setup

### Create a GitHub account

If you don't have one already, head to http://github.com/

### Fork a repository

Fork a WildFly Swarm repository, such as https://github.com/wildfly-swarm/wildfly-swarm,
or whichever repository contains the code/docs that you wish to change, into your GitHub account.

### Clone your newly forked repository onto your local machine

```bash
git clone git@github.com:[your username]/wildfly-swarm.git
cd wildfly-swarm
```

### Add a remote reference to upstream

This makes it easy to pull down changes in the project over time

```bash
git git remote add upstream git://github.com/wildfly-swarm/wildfly-swarm.git
```

### Setup your IDE for the code style format of the project

Currently we have code styles defined for Intellij. Please follow the steps in [the README]ide-configs/idea/README.md on how
to set them up.

### Setup commit message template

We have defined a template for commit messages to help with clarity of any change that is made, thus making it easier for
anyone not familiar with existing code to understand the impacts of a change.

Run the following:
```bash
git config commit.template ./support/gitcommit-template.txt
```

## Development Process

This is the typical process you would follow to submit any changes to our GitHub repositories.

### Pulling updates from upstream

```bash
git pull --rebase upstream master
```

> Note that --rebase will automatically move your local commits, if you have any, on top of the latest branch you pull from.
> If you don't have any commits it is safe to leave off, but for safety it doesn't hurt to use it each time just in case you
> have a commit you've forgotten about!

### Discuss your planned changes (if you want feedback)

 * Google Group - https://groups.google.com/forum/#!forum/wildfly-swarm
 * IRC - irc://irc.freenode.org/wildfly-swarm

### Make sure there is a JIRA for the work

Either assign an existing JIRA to yourself, or create a new one at https://issues.jboss.org/browse/SWARM.

### Create a simple topic branch to isolate your work (recommended)

```bash
git checkout -b my_cool_feature
```

If working on several changes it once, it may be beneficial to use the JIRA issue as the branch name.

### Make the changes

Make whatever code changes, including new tests to verify your change, are necessary and ensure that the build and tests pass:

```bash
mvn clean install
```

> If you're making non code changes, the above step is not required.

### Commit changes

Add whichever files were changed into 'staging' before performing a commit:

```bash
git commit
```

During the commit process you will need to replace the various parts of the template that we added as part of the one time setup.

Here is some information to help:
 * Subject - Short and concise, preferably less than 50 characters
 * Motivation - Describe the purpose of the commit, along with any associated JIRA issue
 * Modifications - What code has changed, and why?
 * Result - How does this commit affect the behaviour or usage

### Rebase changes against master

Once all your commits for the issue have been made against your local topic branch, we need to rebase it against master in
upstream to ensure that your commits are added on top of the current state of master.  This will make it easier to incorporate
your changes into the master branch, especially if there has been any significant time passed since you rebased at the beginning.

```bash
git pull --rebase upstream master
```

### Push to your repo

Now that you've sync'd your topic branch with upstream, it's time to push it to your GitHub repo.

```bash
git push origin my_cool_feature
```

### Getting your changes merged into upstream, a pull request

Now your updates are in your GitHub repo, you will need to notify the project that you have code/docs for inclusion.

 * Send a pull request, by clicking the pull request link while in your repository fork
 * Copy the URL of the pull request
 * In the associated JIRA, click on the "Workflow" menu item and choose "Link Pull Request".  Paste the previously copied link
  and supply any additional comments.
 * As part of the review you may see an automated test run comment on your pull request.
 * After review a maintainer will merge your pull request, update/resolve associated JIRAs, and reply when complete
 * Lastly, switch back to master from your topic branch and pull the updates

```bash
git checkout master
git pull upstream master
```

 * You may also choose to update your origin on GitHub as well

```bash
git push origin
```

### Some tips

Here are some tips on increasing the chance that your pull request is accepted:
 * Include tests that fail without your code, and pass with it
 * Update any associated documentation, or add new documentation. This will require a separate pull request as the documentation
 is in a separate repository
 * Follow the existing code style of the project
