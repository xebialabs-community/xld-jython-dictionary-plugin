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
## Features

## References

