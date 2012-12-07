RoboSpock
=========

RoboSpock is a testing framework which was founded as combination of Robolectric (Android unit test framework) and Spock (modern groovy test framework). Both Robolectric and Spock are well know testing tools in their environment. We came with idea of making two things into one. The result is modern testing tool for Android which lets you run unit tests on PC using very handy language like Groovy.

Spock relies on base class Specification and its runner Sputnik. Writing tests just needs extending Specification class. Same thing goes with Robolectric, it needs to annotate test class with @RunWith and provide RobolectricTestRunner, which more than often is overridden because configuration of other third party libs.

RoboSpock makes life a bit easier. Developer just needs to extend RoboSpecification. That's it! You can test now your Android apps with Spock.

--
Here we will show some simple examples how to program in RoboSpock.
The test below is basic test in Robolectric.

    @Test
    public void testHelloText() {
        // given
        final MainActivity mainActivity = new MainActivity();

        // when
        mainActivity.onCreate(null);

        // then
        Assert.assertEquals("Hello Szlif!", mainActivity.helloTv.getText());
    }

We use given/when/then as comments to mark which parts of tests are responsible for preparation, action and assertion. Assertion is done via Assert.assertEquals.

Converting the same to RoboSpock will look like this:

    def "should display hello text"() {
        given:
        def mainActivity = new MainActivity()

        when:
        mainActivity.onCreate(null)

        then:
        mainActivity.helloTv.text == "Hello Szlif!"
    }
    
First of all we can use human readable sentences for method naming. Second of all using dynamic typing. Third of all no ';'. These things we get from Groovy. Spock introduced keywords for 'given', 'when', 'then' (and few more) so from this time we point which part of code is really preparation or assertion. For example 'then' part has to have asserion, otherwise Spock will close with warning text. There is Android integration done by Robolectric - onCreate really inflates views and sets fields. 'helloTv' exists so we can check its content by calling 'field' text not method getText() - Groovy automagically transform .text to getText(). Assertion is done by simple logic expression not by unit static method.

Does it look great?!