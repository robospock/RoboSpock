package pl.polidea.tddandroid.activity

import com.google.inject.AbstractModule
import com.google.inject.Inject
import pl.polidea.robospock.RoboSpecification
import pl.polidea.robospock.RobolectricGuiceModules
import pl.polidea.tddandroid.module.TestTaskExecutorModule
import pl.polidea.tddandroid.web.WebInterface
import com.google.inject.Binder

//@RobolectricGuiceModules([TestTaskExecutorModule])
class TaskActivityTestGroovy extends RoboSpecification {

    @Inject WebInterface webInterface


    def "setup"() {
        modules {
            install new TestTaskExecutorModule()
            bind(WebInterface).toInstance(Mock(WebInterface))
        }
    }

    def "should load text from asyc task"() {
        given:
        def taskActivity = new TaskActivity()

        when:
        taskActivity.onCreate(null)

        then:
        "WebText" == taskActivity.asyncText.text
    }


    def "test webinterface"(){
        given:
        webInterface.execute(_) >> "Hi"

        when:
        def text = webInterface.execute("")

        then:
        "Hi" == text

    }


}

