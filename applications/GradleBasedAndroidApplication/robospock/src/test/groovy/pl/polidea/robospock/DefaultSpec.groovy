package pl.polidea.robospock

import org.robolectric.annotation.Config
import com.android.tests.basic.Main

@Config(manifest = "../basic/src/main/AndroidManifest.xml")
class DefaultSpec extends RoboSpecification {

  def "should inject view using RoboGuice"() {
    given:
    def mainActivity = new Main()
    mainActivity.onCreate(null)

    when:
    def text = mainActivity.findViewById(com.android.tests.basic.R.id.text).text

    then:
    text == "Test App - Basic"
  }
}
