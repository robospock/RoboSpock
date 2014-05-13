package org.robospock.android

import org.robolectric.Robolectric
import pl.polidea.robospock.RoboSpecification

class MainActivitySpec extends RoboSpecification {

    def "should magic button change the text"(){
        given:
        def activity = Robolectric.buildActivity(MainActivity).create().get()

        when:
        activity.magicBtn.performClick()

        then:
        activity.magicTxt.getText() == "Magic!"
    }

    def "should second click clear the text"(){
        given:
        def activity = Robolectric.buildActivity(MainActivity).create().get()

        when:
        activity.magicBtn.performClick()
        and:
        activity.magicBtn.performClick()

        then:
        !activity.magicTxt.getText()
    }
}