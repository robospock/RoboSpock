package pl.polidea.robospock.test.integration

import pl.polidea.robospock.RoboSpecification
import com.google.inject.ConfigurationException
import javax.security.auth.login.CredentialException
import com.google.inject.CreationException
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

    def "should throw exception while binding class to wrong interface"(){
        when:
        modules{
            bind Executor String
        }

        then:
        thrown(CreationException)

    }
}


