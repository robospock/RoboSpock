package com.android.tests.basic

import org.robolectric.annotation.Config
import pl.polidea.robospock.RoboSpecification
import com.android.tests.basic.Main

@Config(manifest = "../basic/src/main/AndroidManifest.xml")
class DefaultSpec extends RoboSpecification {

  def "should inject view using RoboGuice"() {
    given:
    def mainActivity = new Main()
    mainActivity.onCreate(null)

    when:
    def text = mainActivity.textView.text

    then:
    text == "Test App - Basic"
  }
}
