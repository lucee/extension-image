#!/usr/bin/env bash

ant || exit $?

baseDir=~/projects/lucee-dev

export testLabels=image
export testAdditional=${baseDir}/extensions/extension-image/tests


ant -buildfile "${baseDir}/script-runner/" \
  -Dexecute="bootstrap-tests.cfm" \
  -DexecuteScriptByInclude="false" \
  -DextensionDir="${baseDir}/extensions/extension-image/dist" \
  -DluceeVersion="6.0.0.300-SNAPSHOT" \
  -Dwebroot="${baseDir}/lucee/test"

