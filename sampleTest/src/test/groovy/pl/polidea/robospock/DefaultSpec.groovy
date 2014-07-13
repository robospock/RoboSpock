package pl.polidea.robospock

import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robospock.sampleapp.R
import pl.polidea.robospock.activity.MainActivity
import pl.polidea.robospock.shadow.MyActivityManagerShadow

@Config(shadows = [MyActivityManagerShadow])
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
