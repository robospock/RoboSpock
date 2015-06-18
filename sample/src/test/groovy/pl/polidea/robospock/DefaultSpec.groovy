package pl.polidea.robospock

import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import pl.polidea.robospock.activity.MainActivity
import pl.polidea.robospock.shadow.MyActivityManagerShadow
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
