RoboSpock
=========

RoboSpock is a testing framework which was founded as combination of Robolectric (Android unit test framework) and Spock (modern groovy test framework). Both Robolectric and Spock are well know testing tools in their environment. We came with idea of making two things into one. The result is modern testing tool for Android which lets you run unit tests on PC using very handy language like Groovy.

Spock relies on base class Specification and its runner Sputnik. Writing tests just needs extending Specification class. Same thing goes with Robolectric, it needs to annotate test class with @RunWith and provide RobolectricTestRunner, which more than often is overridden because configuration of other third party libs.

RoboSpock makes life a bit easier. Developer just needs to extend RoboSpecification. That's it! You can test now your Android apps with Spock.
