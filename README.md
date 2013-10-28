RoboSpock
=========

RoboSpock is a testing framework which was founded as combination of <a href="http://pivotal.github.com/robolectric/">Robolectric</a> (Android unit test framework) and <a href="http://code.google.com/p/spock/">Spockframework</a> (modern groovy test framework). Both Robolectric and Spock are well know testing tools in their environment. We came with idea of making two things into one. The result is modern testing tool for Android which lets you run unit tests on PC using very handy language like Groovy.

Spock relies on base class Specification and its runner Sputnik. Writing tests just needs extending Specification class. Same thing goes with Robolectric, it needs to annotate test class with @RunWith and provide RobolectricTestRunner, which more than often is overridden because configuration of other third party libs.

RoboSpock makes life a bit easier. Developer just needs to extend RoboSpecification. That's it! You can test now your Android apps with Spock.

--
Here we will show some simple examples how to program in RoboSpock.
The test below is basic test in Robolectric.

    @Test
    public void testHelloText() {
        // given
        final TextView textView = new TextView(Robolectric.application);

        // when
        textView.setText("Hello")

        // then
        Assert.assertEquals("Hello", textView.getText());
    }

We use given/when/then as comments to mark which parts of tests are responsible for preparation, action and assertion. Assertion is done via Assert.assertEquals.

Converting the same to RoboSpock will look like this:

    def "should display hello text"() {
        given:
        def textView = new TextView(Robolectric.application)

        when:
        textView.setText("Hello")

        then:
        textView.text == "Hello"
    }
    
First of all we can use human readable sentences for method naming. Second of all using dynamic typing. Third of all no ';'. These things we get from Groovy. Spock introduced keywords for 'given', 'when', 'then' (and few more) so from this time we point which part of code is really preparation or assertion. For example 'then' part has to have asserion, otherwise Spock will close with warning text. There is Android integration done by Robolectric. 'textView' exists so we can check its content by calling 'field' text not method getText() - Groovy automagically transform .text to getText(). Assertion is done by simple logic expression not by unit static method.

Does it look great?!

Installation
========

We advice to install RoboSpock using gradle by adding the line to dependencies

    compile 'pl.polidea:robospock:0.1'
    
and remember to use groovy plugin

    apply plugin: 'groovy'
    
If you prefer maven for building test application please add this text to your pom.xml

    <dependency>
        <groupId>pl.polidea</groupId>
        <artifactId>robospock</artifactId>
        <version>0.1</version>
    </dependency>


The installation can be also done just by downloading <a href="http://search.maven.org/remotecontent?filepath=pl/polidea/robospock/0.1/robospock-0.1.jar">jar</a>. Remember to get the dependencies:
<ul>
<li><a href="http://search.maven.org/remotecontent?filepath=org/spockframework/spock-core/0.6-groovy-1.8/spock-core-0.6-groovy-1.8.jar">Spock</a></li>
<li><a href="http://search.maven.org/remotecontent?filepath=com/pivotallabs/robolectric/1.1/robolectric-1.1-jar-with-dependencies.jar">Robolectric with denedencies</a></li>
<li><a href="http://search.maven.org/remotecontent?filepath=org/roboguice/roboguice/2.0/roboguice-2.0.jar">RoboGuice</a></li>
<li><a href="http://code.google.com/p/google-guice/downloads/detail?name=guice-2.0-no_aop.jar&can=2&q=">Guice 2.0 no aop</a><br>For RoboGuice installation please check <a hrefhttp://code.google.com/p/roboguice/wiki/Installation"> this link</a></li>
</ul>

For maven and jar projects please configure your test project in your IDE as Groovy based project (not only Java).
You may find sources and Javadocs in <a href="http://search.maven.org/#browse%7C285983449">Central Maven Repository</a> as well.
