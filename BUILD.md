
# How to build and deploy a release of MSDK

## Prerequisities

* Install GPG and generate your key pair (gpg --gen-key)
* Deploy your public GPG key to a public keyserver (e.g., pgp.mit.edu)
* Get an account on Sonatype JIRA (https://issues.sonatype.org)
* Setup your ~/.m2/settings.xml file using this template:

```
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>SONATYPE_JIRA_USERNAME</username>
      <password>SONATYPE_JIRA_PASSWORD</password>
    </server>
  </servers>
  <profiles>
    <profile>
      <id>ossrh</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <gpg.executable>/usr/local/bin/gpg2</gpg.executable>
        <gpg.passphrase></gpg.passphrase>
      </properties>
    </profile>
  </profiles>
</settings>
```

# Build

Use the following sequence of commands to build and deploy a new MSDK release.

```

# Cleanup 
mvn clean

# Update version number of all modules (change X.Y.Z to new version number)
mvn versions:set -DnewVersion=X.Y.Z versions:commit
git commit -a

# Build the whole project
mvn -Pmsdk-release package

# Deploy after successful build
mvn -Pmsdk-release -DskipTests deploy

# Create a tag in the git repo
git tag vX.Y.Z
git push origin --tags

# To generate complete JavaDoc documentation and upload it to http://msdk.github.io/api/
mvn -Pmsdk-release -DskipTests package javadoc:aggregate scm-publish:publish-scm

# After a succesful release, set the versions to the next development version
mvn versions:set -DnewVersion=X.Y.Z-SNAPSHOT versions:commit
git commit -a

```
