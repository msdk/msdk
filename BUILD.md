
# How to build and deploy a release of MSDK

## Prerequisities

* Install GPG and generate your key pair
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

Use the following sequence of commands to build and release a new MSDK version.

```
# Cleanup 
mvn clean

# Update version number of all modules (change X.Y.Z to new version number)
mvn versions:set -DnewVersion=X.Y.Z versions:commit

# Optional - fix missing JavaDoc comments
mvn javadoc:fix

# Build the whole project
mvn package

# Deployment
mvn deploy
```



