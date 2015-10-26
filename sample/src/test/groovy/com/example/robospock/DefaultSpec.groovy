package com.example.robospock

import com.example.robospock.activity.MainActivity
import com.example.robospock.shadow.MyActivityManagerShadow
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robospock.GradleRoboSpecification

@Config(shadows = [MyActivityManagerShadow], constants = BuildConfig)
class DefaultSpec extends GradleRoboSpecification {

    def "should find text view and compare text value"() {
        given:
        def mainActivity = Robolectric.buildActivity(MainActivity).create().get()

        when:
        def text = mainActivity.helloTv.text

        then:
        text == "Test App - Basic"
    }
}
