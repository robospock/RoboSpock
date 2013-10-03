package pl.polidea.robospock

import org.robolectric.annotation.Config
import pl.polidea.robospock.activity.MainActivity
import pl.polidea.robospock.shadow.MyActivityManagerShadow

@Config(shadows = [MyActivityManagerShadow])
class DefaultSpec extends RoboSpecification {

    def "should inject view using RoboGuice"() {
        given:
        def mainActivity = new MainActivity()
        mainActivity.onCreate(null)

        when:
        def text = mainActivity.helloTv.text

        then:
        text == "Hello Szlif!"
    }

    def "should have 0MB memory class"() {
        given:
        def mainActivity = new MainActivity()
        mainActivity.onCreate(null)

        when:
        def text = mainActivity.memoryTv.text

        then:
        text == "I have 16 MB"
    }

}
