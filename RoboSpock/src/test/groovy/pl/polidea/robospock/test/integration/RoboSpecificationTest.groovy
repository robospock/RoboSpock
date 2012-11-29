package pl.polidea.robospock.test.integration

import com.google.inject.CreationException
import pl.polidea.robospock.RoboSpecification

import java.util.concurrent.Executor
import com.google.inject.AbstractModule

class RoboSpecificationTest extends RoboSpecification {

    def "should throw exception while installing non AbstractModule class"() {
        when:
        modules {
            install String
        }

        then:
        thrown(CreationException)
    }

    def "should throw exception while binding class to wrong interface"() {
        when:
        modules {
            bind Executor, String
        }

        then:
        thrown(CreationException)

    }

    def "should be able to install new guice module"(){
        given:
        def module = Mock(AbstractModule)

        when:
        modules{
            install module
        }

        then:
        notThrown(CreationException)
    }

    def "should be able to bind a class to interface"(){
        when:
        modules{
            bind CharSequence, String
        }

        then:
        notThrown(CreationException)
    }

}


