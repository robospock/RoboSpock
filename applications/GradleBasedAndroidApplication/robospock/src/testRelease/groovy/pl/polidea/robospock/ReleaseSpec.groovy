package pl.polidea.robospock

import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import com.android.tests.basic.Main

@Config(manifest = Config.DEFAULT)
class ReleaseSpec extends RoboSpecification {

  def "should find text view and compare text value"() {
    given:
    def mainActivityBuilder = Robolectric.buildActivity(Main)
    mainActivityBuilder.create()

    when:
    def text = mainActivityBuilder.get().findViewById(com.android.tests.basic.R.id.text).text

    then:
    text == "Test App - Release"
  }
}
