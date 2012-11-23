package pl.polidea.robospock.test.integration

import spock.lang.Specification
import android.app.Activity

class StubSpecifcation extends Specification {

    def "should throw Stub! exception"() {
        when:
        new Activity();

        then:
        def e = thrown(RuntimeException)
        e.message == "Stub!"
    }
}
