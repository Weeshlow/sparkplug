#
# Sparkplug Test Container
# Apache Spark 1.5.1
#
# Runs the Sparkplug test suite in a container
#
# One-time usage (such as on travis):
# $ docker build -t uncharted/Sparkplug-test .
# $ docker run --rm uncharted/Sparkplug-test
#
# Dev environment usage:
# $ docker run -v $(pwd):/opt/sparkplug -it --entrypoint=/bin/bash uncharted/sparkplug-test
# container$ ./gradlew
#
# If you need to install the jars to your local m2 repository, be sure to clean
# the build directory from inside the docker container, since the container
# happens to assign root permissions to all the files in the /build directory

FROM uncharted/sparklet:1.5.1
MAINTAINER Michael Laccetti <mlaccetti@uncharted.software>

ADD . /opt/sparkplug
WORKDIR /opt/sparkplug

RUN mkdir /opt/libs

# silence log4j garbage from spark
ADD src/test/resources/log4j.properties /usr/local/spark/conf/log4j.properties

# for dev environment
ENV GRADLE_OPTS -Dorg.gradle.daemon=true

# download scalatest
RUN curl http://central.maven.org/maven2/org/scalatest/scalatest_2.11/2.2.5/scalatest_2.11-2.2.5.jar > /opt/libs/scalatest_2.11-2.2.5.jar

# download scoverage
RUN curl https://repo1.maven.org/maven2/org/scoverage/scalac-scoverage-runtime_2.11/1.1.1/scalac-scoverage-runtime_2.11-1.1.1.jar > \
  /opt/libs/scalac-scoverage-runtime_2.10-1.1.1.jar

ENTRYPOINT ["./gradlew"]

CMD ["coverage"]
