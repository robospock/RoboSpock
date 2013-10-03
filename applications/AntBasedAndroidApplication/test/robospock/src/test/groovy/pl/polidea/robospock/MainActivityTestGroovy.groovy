package pl.polidea.robospock

import android.app.AlertDialog
import android.content.DialogInterface
import com.google.inject.Inject
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowDialog
import pl.polidea.robospock.activity.MainActivity
import pl.polidea.robospock.database.DatabaseHelper
import pl.polidea.robospock.database.DatabaseObject
import pl.polidea.robospock.shadow.MyActivityManagerShadow

import java.sql.SQLException

@Config(shadows = [MyActivityManagerShadow])
class MainActivityTestGroovy extends RoboSpecification {
    @Inject DatabaseHelper databaseHelper;

    def "setup"() {
        inject()
    }

    def "should display hello text"() {
        given:
        def mainActivity = new MainActivity()

        when:
        mainActivity.onCreate(null)

        def tv = Robolectric.shadowOf(mainActivity.helloTv)

        tv.right = 6
        tv.left = 1
        then:
        tv.text == "Hello Szlif!"
        tv.getWidth() == 5
    }

    def "should change text when button is clicked"() {
        given: 'set up activity'
        def mainActivity = new MainActivity()
        mainActivity.onCreate(null)

        when:
        mainActivity.button.performClick()

        then:
        mainActivity.buttonTv.text == "Clicked !"
    }

    def "memory TextView should display memory class"() {
        given:
        def mainActivity = new MainActivity()

        when:
        mainActivity.onCreate(null)

        then:
        mainActivity.memoryTv.text == "I have 16 MB"
    }

    def "should displayed dialog's button has good text"() {
        given:
        def mainActivity = new MainActivity()
        mainActivity.onCreate(null)

        when:
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
        mainActivity.onCreate(null)

        when:
        mainActivity.button.performClick()
        def dialog = (ShadowAlertDialog) Robolectric.shadowOf(ShadowDialog.getLatestDialog());

        then:
        dialog.title == "title"
        dialog.message == "Dialog Content"
    }

    def "dialog buttons should response on click actions"() {
        given:
        def mainActivity = new MainActivity()

        and: 'create listener mock and set to Activity'
        def listenerMock = Mock(DialogInterface.OnClickListener)
        mainActivity.dialogListener = listenerMock

        and:
        mainActivity.onCreate(null)

        when: 'click dialog button'
        mainActivity.button.performClick()
        def dialog = (ShadowAlertDialog) Robolectric.shadowOf(ShadowDialog.getLatestDialog());

        and: 'click all dialog buttons'
        [
                AlertDialog.BUTTON_POSITIVE,
                AlertDialog.BUTTON_NEGATIVE,
                AlertDialog.BUTTON_NEUTRAL
        ].each { dialog.getButton(it).performClick() }

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
        insertSize == 1
    }

    def "should throw SQL Constraint exception when inserting object with existing primary key"() {
        given:
        def dao = databaseHelper.getDao(DatabaseObject)

        and: 'stored object'
        def dbObject = new DatabaseObject("test", 4, 1)
        dao.create(dbObject)

        when: 'duplication'
        dao.create(dbObject)

        then:
        def exception = thrown(RuntimeException)
        exception.message =~ "SQLITE_CONSTRAINT"
        exception.cause.class == SQLException
    }

}
