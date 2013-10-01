package pl.polidea.robospock.test.integration

import android.app.Activity

class SampleTest extends RoboSpecification {

    def 'asdf is nil'() {
        expect:
        new Activity()

    }
}
