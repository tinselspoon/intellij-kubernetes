# Kubernetes support for IntelliJ
Provides assistance for editing YAML files containing Kubernetes resources for IntelliJ IDEA.

YAML files are auto-detected - add in a `kind` or `apiVersion` element at the top level and the completion will be activated.

**OpenShift completion is optional and disabled by default.** To add OpenShift completion or change the version of Kubernetes in use, go to _Settings > Languages > Kubernetes and OpenShift_.

## Features
- Auto-completion of properties within resources.
- Popup documentation of properties.
- Inspections to detect and fix invalid, duplicated, and missing required properties.
- Supports Kubernetes top-level resources with definitions from the Kubernetes swagger specs.

## Current Limitations
- Only YAML files are supported; no JSON support.
