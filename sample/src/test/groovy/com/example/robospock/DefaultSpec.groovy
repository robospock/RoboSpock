package com.example.robospock

import com.example.robospock.activity.MainActivity
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import com.example.robospock.shadow.MyActivityManagerShadow
import util.SampleSpecification

@Config(shadows = [MyActivityManagerShadow])
class DefaultSpec extends SampleSpecification {

    def "should find text view and compare text value"() {
        given:
        def mainActivity = Robolectric.buildActivity(MainActivity).create().get()

        when:
        def text = mainActivity.helloTv.text

        then:
        text == "Test App - Basic"
    }
}
