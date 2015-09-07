package pl.polidea.robospock.test.integration

import android.app.Activity
import org.robolectric.Robolectric
import pl.polidea.robospock.RoboSpecification
import spock.lang.Unroll

class ActivityCreationTest extends RoboSpecification {

    def 'Create and activity'() {
        when:
        def act = new Activity()

        assert 1 == 1

        then:
        noExceptionThrown()

        act instanceof Activity
    }

    @Unroll
    def "where with unroll"() {
        expect:
        i == i

        where:
        i << [0, 1, 5, 10]
    }

    def "should use robolectric"() {
        when:
        def controller = Robolectric.buildActivity(Activity.class).create()

        then:
        controller.get().mCalled == true
    }
}
