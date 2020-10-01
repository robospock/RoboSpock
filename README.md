[![Build Status](https://travis-ci.org/robospock/RoboSpock.svg?branch=master)](https://travis-ci.org/robospock/RoboSpock)



RoboSpock
=========

RoboSpock is an Android testing framework which brings <a href="http://code.google.com/p/spock/">Spockframework</a> (modern groovy test framework) to Android applications.
It lets you running blazing fast unit tests on JVM without any need for starting an emulator or making any deployments.


We came with idea of making two things into one (Robolectric and Spock).

Those are only contribution guidelines! To see the documentation, visit http://robospock.org

Setup
=====
Setup Groovy integration:

```
buildscript {
  repositories {
    jcenter()
  }

  dependencies {
    classpath 'com.android.tools.build:gradle:2.2.0'
    classpath 'org.codehaus.groovy:groovy-android-gradle-plugin:1.1.0'
  }
}

apply plugin: 'com.android.application'
apply plugin: 'groovyx.android'
```
    
Add dependency (available on Maven central):  
```
testCompile 'org.robospock:robospock:1.0.1'
```

Changelog
=========
1.0.1 - Adding support for Android Gradle Plugin 2.2.+

1.0.0 - Bumping Spock version to 1.0 and Groovy according to Spock depdendency

0.8.0 - Adding GradleRoboSpecification. Use it for gradle project and easier integration.

0.7.0 - Robolectric updated to 3.0

0.6.0 - Robolectric updated to 2.4 and Groovy to 2.3.8

0.5.0 - Robolectric updated to 2.3

0.4.4 - Robolectric updated to 2.2 and Spock to 0.7
