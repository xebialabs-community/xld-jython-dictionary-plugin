# XL Deploy Jython Dictionary Plugin

[![Build Status][xld-jython-dictionary-plugin-travis-image]][xld-jython-dictionary-plugin-travis-url]
[![License: MIT][xld-jython-dictionary-plugin-license-image]][xld-jython-dictionary-plugin-license-url]
![Github All Releases][xld-jython-dictionary-plugin-downloads-image]

[xld-jython-dictionary-plugin-travis-image]: https://travis-ci.org/xebialabs-community/xld-jython-dictionary-plugin.svg?branch=master
[xld-jython-dictionary-plugin-travis-url]: https://travis-ci.org/xebialabs-community/xld-jython-dictionary-plugin
[xld-jython-dictionary-plugin-license-image]: https://img.shields.io/badge/License-MIT-yellow.svg
[xld-jython-dictionary-plugin-license-url]: https://opensource.org/licenses/MIT
[xld-jython-dictionary-plugin-downloads-image]: https://img.shields.io/github/downloads/xebialabs-com]

## Preface

This document describes the functionality provided by the XL Deploy Jython Dictionary plugin 

## Overview

This plugin allow to define customs dictionaries using a simple Jython script.
A sample is provided with the `udm.DeploymentContextDictionary` CI: it provides entries with the current deployment context.
The following entries are generated:
* application: deployed application's name
* version: deployed version
* container: targeted container
* environment: environment's name
* now: the current date.

This entries can be used either as file placeholder or as a property placeholder. 
In the sample configuration below, the target path of the copied file is computed usng the name of the targeted container and the version of deployed package.
````
targetPath: /tmp/foo/{{version}}/{{container}}
````


## Installation

* requirement xl-deploy-server 9.0.0+
* Copy the latest JAR file from the [releases page](https://github.com/xebialabs-community/xld-jython-dictionary-plugin/releases) into the `XL_DEPLOY_SERVER/plugins` directory.
* Restart the XL Deploy server.

## Sample Configuration
A sample configuration is available in the project.

```
 ./xlw --config ./.xebialabs/config.yaml apply -f xebialabs.yaml
[1/4] Applying infrastructure.yaml (imported by xebialabs.yaml)
    Updated CI Infrastructure/config/machine1
    Updated CI Infrastructure/config/machine2
    Updated CI Infrastructure/config

[2/4] Applying environment.yaml (imported by xebialabs.yaml)
    Updated CI Environments/config/deployment_context
    Updated CI Environments/config/env
    Updated CI Environments/config

[3/4] Applying application.yaml (imported by xebialabs.yaml)
    Updated CI Applications/config/demo/1.1.12/demo.txt
    Updated CI Applications/config/demo/1.1.12
    Updated CI Applications/config/demo/1.1.15/demo.txt
    Updated CI Applications/config/demo/1.1.15
    Updated CI Applications/config/demo
    Updated CI Applications/config

[4/4] Applying xebialabs.yaml
Done

```
## Create your own dictionary

1. Declare a new type

In the `synthetic.xml` file, define a new type that extends the `jython.AbstractJythonDictionary` CI:

```

  <type type="github.Dictionary" extends="jython.AbstractJythonDictionary">
    <property name="pythonScript" hidden="true" default="github.py"/>
    <property name="entries" kind="map_string_string" hidden="true" required="false"/>
    <property name="encryptedEntries" kind="map_string_string" hidden="true" password="true" required="false"/>
    <property name="username" category="Github"/>
    <property name="password" category="Github" password="true"/>
    <property name="repository" category="Github"/>
    <property name="branch" category="Github" default="master"/>
    <property name="path" category="Github"/>
  </type>
```

2. Create the jython file

In the ext/ folder, create the github.py file.
This is default content that load an empty map in the dictionary.s

```
values = {}

if logger.isDebugEnabled():
    logger.debug("dict values {0}: {1}".format(dictionary_id, values))

entries.setValues(values)
```

3. Install additional libraries

For github, go to https://github.com/PyGithub/PyGithub/releases and dowload the latest release.

```
wget https://github.com/PyGithub/PyGithub/archive/v1.28.zip
unzip v1.28.zip
cp -r PyGithub-1.28/github $XLD_HOME/ext
```
4. Implement the lookup action

Note: _this sample use an old GitHub Api and should be used only as a sample code_

````
from github import Github

username = dictionary.getProperty('username')
password = dictionary.getProperty('password')
repository = dictionary.getProperty('repository')
branch = dictionary.getProperty('branch')
path = dictionary.getProperty('path')


# First create a Github instance:
print "open a connection to github using the '%s' username" % username
g = Github(username, password)
print "get '%s' repository" % repository
repo = g.get_user().get_repo(repository)
print "get content of '%s' in '%s' branch " % (path, branch)
contents = repo.get_contents(path, branch)
# print "decoded_content", contents.decoded_content

values = {}
for line in contents.decoded_content.split('\n'):
    line = line.rstrip()  # removes trailing whitespace and '\n' chars

    if "=" not in line: continue  # skips blanks and comments w/o =
    if line.startswith("#"): continue  # skips comments which contain =

    k, v = line.split("=", 1)
    values[k] = v


if logger.isDebugEnabled():
    logger.debug("dict values {0}: {1}".format(dictionary_id, values))

entries.setValues(values)
```

## References

