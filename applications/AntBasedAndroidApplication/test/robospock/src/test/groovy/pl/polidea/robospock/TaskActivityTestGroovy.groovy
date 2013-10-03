package pl.polidea.robospock

import com.google.inject.Inject
import org.robolectric.Robolectric
import org.robolectric.shadows.ShadowBitmapFactory
import pl.polidea.robospock.activity.TaskActivity
import pl.polidea.robospock.module.TestTaskExecutorModule
import pl.polidea.robospock.web.WebInterface

class TaskActivityTestGroovy extends RoboSpecification {

    @Inject WebInterface webInterface
    def File

    def "setup"() {
        inject {
            install TestTaskExecutorModule
            bindInstance WebInterface, Mock(WebInterface)
        }

        File = new File(Robolectric.application.getCacheDir().getPath() + "/image")
    }

    def "should load text from asyc task"() {
        given:
        def taskActivity = new TaskActivity()

        when:
        taskActivity.onCreate(null)

        then:
        taskActivity.asyncText.text == "WebText"
    }


    def "should display text from web"() {
        given:
        webInterface.execute("http://dev.polidea.pl/ext/szlif677557/text") >> "Hi! I'm text from ext :)"
        def taskActivity = new TaskActivity()

        when:
        taskActivity.onCreate(null)

        then:
        taskActivity.webTv.text == "Hi! I'm text from ext :)"
    }

    def "should display image downloaded from web"() {
        given:
        webInterface.downloadFile("http://www.polidea.pl/CorporateIdentity/logo_100x60.png", file.path) >> file
        ShadowBitmapFactory.provideWidthAndHeightHints(file.path, 200, 300)

        and: 'set up activity'
        def taskActivity = new TaskActivity()
        taskActivity.onCreate(null)

        when:
        taskActivity.loadBtn.performClick()

        then:
        taskActivity.webIv.drawable
    }


}

