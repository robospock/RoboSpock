package pl.polidea.tddandroid.activity

import android.app.AlertDialog
import android.content.DialogInterface
import com.google.inject.Inject
import com.xtremelabs.robolectric.Robolectric
import com.xtremelabs.robolectric.shadows.ShadowAlertDialog
import com.xtremelabs.robolectric.shadows.ShadowDialog
import org.junit.runner.RunWith
import pl.polidea.robospock.RoboSputnik
import pl.polidea.robospock.RobolectricGuiceModules
import pl.polidea.robospock.UseShadows
import pl.polidea.tddandroid.database.DatabaseHelper
import pl.polidea.tddandroid.database.DatabaseObject
import pl.polidea.tddandroid.shadow.MyActivityManagerShadow
import spock.lang.Specification

@RobolectricGuiceModules
@RunWith(RoboSputnik)
@UseShadows(MyActivityManagerShadow)
class MainActivityTestGroovy extends Specification {
    @Inject DatabaseHelper databaseHelper;

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

    def "should dialog has good title and message"() {
        given:
        def mainActivity = new MainActivity()

        when:
        mainActivity.onCreate(null)
        mainActivity.button.performClick()
        def dialog = (ShadowAlertDialog) Robolectric.shadowOf(ShadowDialog.getLatestDialog());

        then:
        "title" == dialog.title
        "Dialog Content" == dialog.message
    }

    def "should dialog response to button actions"() {
        given:
        def mainActivity = new MainActivity()
        def listenerMock = Mock(DialogInterface.OnClickListener)
        mainActivity.dialogListener = listenerMock

        when:
        mainActivity.onCreate(null)
        mainActivity.button.performClick()
        def dialog = (ShadowAlertDialog) Robolectric.shadowOf(ShadowDialog.getLatestDialog());
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).performClick();

        then:
        3 * listenerMock.onClick(_, _)
    }

    def "should insert single object to database"() {
        given:
        def databaseObject = new DatabaseObject("title", 4, 0)
        def dao = databaseHelper.getDao(DatabaseObject)

        when:
        def insertSize = dao.create(databaseObject)

        then:
        1 == insertSize
    }

    def "should throw SQL Constraint exception when inserting object with existing primary key"() {
        given:
        def dao = databaseHelper.getDao(DatabaseObject)
        dao.create(new DatabaseObject("test", 4, 1));
        dao.create(new DatabaseObject("tset", 4, 2));
        dao.create(new DatabaseObject("testtset", 8, 3));
        def databaseObject = new DatabaseObject("title", 4, 1)

        when:
        dao.create(databaseObject)

        then:
        def exception = thrown(RuntimeException)
        exception.message =~ "SQLITE_CONSTRAINT"
    }

}
