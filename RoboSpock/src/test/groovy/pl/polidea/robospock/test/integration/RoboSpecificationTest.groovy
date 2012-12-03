package pl.polidea.robospock.test.integration

import com.google.inject.AbstractModule
import com.google.inject.CreationException

import java.util.concurrent.Executor
import javax.inject.Inject
import com.google.inject.Provider
import pl.polidea.robospock.RoboSpecification

class RoboSpecificationTest extends RoboSpecification {

    @Inject String someString

    @Inject Object someObject
    @Inject Object anotherObject

    def "should throw exception while installing non AbstractModule class"() {
        when:
        inject {
            install String
        }

        then:
        thrown(CreationException)
    }

    def "should throw exception while binding class to wrong interface"() {
        when:
        inject {
            bind Executor, String
        }

        then:
        thrown(CreationException)

    }

    def "should be able to install new guice module"() {
        given:
        def module = Mock(AbstractModule)

        when:
        inject {
            install module
        }

        then:
        notThrown(CreationException)
    }

    def "should be able to bind a class to interface"() {
        when:
        inject {
            bind CharSequence, String
        }

        then:
        notThrown(CreationException)
    }

    def "calling modules should inject stuff here"() {
        given:
        def config = {
            bind(String).toInstance('any string')
        }

        when:
        def before = someString
        inject config
        def after = someString

        then: 'calling modules injected someString'
        !before
        after
    }

    def "should bindInstance create same object when injecting"() {
        when:
        inject config

        then:
        same == (someObject.is(anotherObject))
        equal == (someObject.equals(anotherObject))

        where:
        same | equal | config
        true | true  | {bindInstance Object, 'asdf'}
        false| true  | {bind(Object).toProvider(new Provider<Object>() {@Override Object get() {[]}})}
    }
}


