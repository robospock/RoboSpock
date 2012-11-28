package pl.polidea.tddandroid.activity

import android.app.AlertDialog
import com.xtremelabs.robolectric.Robolectric
import com.xtremelabs.robolectric.shadows.ShadowAlertDialog
import com.xtremelabs.robolectric.shadows.ShadowDialog
import org.junit.runner.RunWith
import pl.polidea.robospock.RoboSputnik
import pl.polidea.robospock.RobolectricGuiceModules
import pl.polidea.robospock.UseShadows
import pl.polidea.tddandroid.shadow.MyActivityManagerShadow
import spock.lang.Specification

@RunWith(RoboSputnik)
@RobolectricGuiceModules
@UseShadows(MyActivityManagerShadow)
class MainActivityTestGroovy extends Specification {

    def "should display hello text"() {
        given:
        def mainActivity = new MainActivity()

        when:
        mainActivity.onCreate(null)

        then:
        "Hello Szlif!" == mainActivity.helloTv.text
    }

    def "should change text when button is clicked"() {
        given:
        def mainActivity = new MainActivity()

        when:
        mainActivity.onCreate(null) // TODO: question - 'when' has two actions ? is it good?
        mainActivity.button.performClick()

        then:
        "Clicked !" == mainActivity.buttonTv.text
    }

    def "should memory TextView display memory class"() {
        given:
        def mainActivity = new MainActivity()

        when:
        mainActivity.onCreate(null)

        then:
        "I have 16 MB" == mainActivity.memoryTv.text
    }

    def "should displayed dialog's button has good text"() {
        given:
        def mainActivity = new MainActivity()

        when:
        mainActivity.onCreate(null)
        mainActivity.button.performClick()
        def dialog = (ShadowAlertDialog) Robolectric.shadowOf(ShadowDialog.getLatestDialog());

        then:
        dialog.getButton(number).text == value

        where:
        number                      | value
        AlertDialog.BUTTON_POSITIVE | "Ok"
        AlertDialog.BUTTON_NEGATIVE | "Cancel"
        AlertDialog.BUTTON_NEUTRAL  | "Dismiss"
    }
}
