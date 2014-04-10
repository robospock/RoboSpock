package pl.polidea.robospock.test.integration

import android.app.Activity
import pl.polidea.robospock.RoboSpecification

class ActivityCreationTest extends RoboSpecification {

    def 'Create and activity'() {
        when:
        new Activity()

        then:
        noExceptionThrown()
    }
}
