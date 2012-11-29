package pl.polidea.robospock.test.integration

import com.google.inject.CreationException
import pl.polidea.robospock.RoboSpecification

import java.util.concurrent.Executor

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
            bind Executor String
        }

        then:
        thrown(CreationException)

    }
}


