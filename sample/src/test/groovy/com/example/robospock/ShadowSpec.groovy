package com.example.robospock

import com.example.robospock.activity.MainActivity
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import com.example.robospock.shadow.MyActivityManagerShadow
import util.SampleSpecification

@Config(shadows = [MyActivityManagerShadow])
class ShadowSpec extends SampleSpecification {

    def "shouldCompile"() {
        given:
        def mainActivity = Robolectric.buildActivity(MainActivity).create().get()

        when:
        def text = mainActivity.memoryTv.text

        then:
        text == "I have 16 MB"
    }
}