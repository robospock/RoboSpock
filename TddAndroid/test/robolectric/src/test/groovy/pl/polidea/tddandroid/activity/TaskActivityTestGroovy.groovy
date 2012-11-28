package pl.polidea.tddandroid.activity

import org.junit.runner.RunWith
import pl.polidea.robospock.RoboSputnik
import spock.lang.Specification
import pl.polidea.robospock.RobolectricGuiceModules
import pl.polidea.robospock.UseShadows
import pl.polidea.tddandroid.module.TestTaskExecutorModule
import pl.polidea.tddandroid.module.TestWebModule
import com.google.inject.AbstractModule
import pl.polidea.tddandroid.web.WebInterface

@RunWith(RoboSputnik)
@RobolectricGuiceModules([TestTaskExecutorModule, TestWebModule])
@UseShadows
class TaskActivityTestGroovy extends Specification {

    def "should load text from asyc task"(){
        given:
        def taskActivity = new TaskActivity()

        when:
        taskActivity.onCreate(null)

        then:
        "WebText" == taskActivity.asyncText.text
    }

    def "should display text from given url"(){
        given:
        def taskActivity = new TaskActivity()

        when:
        taskActivity.onCreate(null)

        then:
        "Hi I'm text from ext :)" == taskActivity.webTv.text
    }
}

