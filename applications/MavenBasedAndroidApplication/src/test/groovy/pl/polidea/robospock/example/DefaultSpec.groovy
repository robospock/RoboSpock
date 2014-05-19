package pl.polidea.robospock.example

import org.robolectric.Robolectric
import pl.polidea.robospock.RoboSpecification

class DefaultSpec extends RoboSpecification {
    def "should find text view and compare text value"() {
        given:
        def mainActivity = Robolectric.buildActivity(Main).create().get()

        when:
        def text = mainActivity.findViewById(R.id.text).text

        then:
        text == "Test App"
    }
}
