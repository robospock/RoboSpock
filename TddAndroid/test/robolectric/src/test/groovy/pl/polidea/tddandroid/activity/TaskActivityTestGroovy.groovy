package pl.polidea.tddandroid.activity

import com.google.inject.Inject
import com.xtremelabs.robolectric.Robolectric
import com.xtremelabs.robolectric.shadows.ShadowBitmapFactory
import pl.polidea.robospock.RoboSpecification
import pl.polidea.tddandroid.module.TestTaskExecutorModule
import pl.polidea.tddandroid.web.WebInterface

class TaskActivityTestGroovy extends RoboSpecification {

    @Inject WebInterface webInterface
    def File

    def "setup"() {
        modules {
            install TestTaskExecutorModule
            bind WebInterface, Mock(WebInterface)
        }
        File = new File(Robolectric.application.getCacheDir().getPath() + "/image")
    }

    def "should load text from asyc task"() {
        given:
        def taskActivity = new TaskActivity()

        when:
        taskActivity.onCreate(null)

        then:
        "WebText" == taskActivity.asyncText.text
    }


    def "should display text from web"() {
        given:
        webInterface.execute("http://dev.polidea.pl/ext/szlif677557/text") >> "Hi! I'm text from ext :)"
        def taskActivity = new TaskActivity()


        when:
        taskActivity.onCreate(null)

        then:
        "Hi! I'm text from ext :)" == taskActivity.webTv.text
    }

    def "should display image downloaded from web"() {
        given:
        webInterface.downloadFile("http://www.polidea.pl/CorporateIdentity/logo_100x60.png", file.path) >> file
        ShadowBitmapFactory.provideWidthAndHeightHints(file.path, 200, 300)
        def taskActivity = new TaskActivity()

        when:
        taskActivity.onCreate(null)
        taskActivity.loadBtn.performClick()


        then:
        taskActivity.webIv.drawable

    }


}

