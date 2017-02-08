#!/bin/bash
#
#  Auto build shell script for nc-gateway
#  Create by weiyuanhua 2015-07-28
#

if [ $# -lt 1 ]; then
  echo "Usage: $0 [p|t|d]";
  exit;
fi

CONFIG=''

case $1 in
  p | production)
    CONFIG="src/main/portable/product.xml"
    ;;
  t | test)
    CONFIG="src/main/portable/test.xml"
    ;;
  d | devleopment)
    CONFIG="src/main/portable/dev.xml"
    ;;
  *)
    echo "Error! unknown parameter."
    exit 1
    ;;
esac

# 此处需要设置或修改MAVEN_OPTS，否则在执行mvn install命令时可能会出现OutOfMemoryError错误
export MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=128m"

VERSION=`cat VERSION`
#mvn versions:set -DnewVersion=$VERSION-SNAPSHOT
mvn clean install -Dmaven.test.skip=true

