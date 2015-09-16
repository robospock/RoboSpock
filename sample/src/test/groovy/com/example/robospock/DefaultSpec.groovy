package com.example.robospock

import com.example.robospock.activity.MainActivity
import org.robolectric.Robolectric
import pl.polidea.robospock.RoboSpecification

class DefaultSpec extends RoboSpecification {
    def "should find text view and compare text value"() {
        given:
        def mainActivity = Robolectric.buildActivity(MainActivity).create().get()

        when:
        def text = mainActivity.helloTv.text

        then:
        text == "Test App - Basic"
    }
}
