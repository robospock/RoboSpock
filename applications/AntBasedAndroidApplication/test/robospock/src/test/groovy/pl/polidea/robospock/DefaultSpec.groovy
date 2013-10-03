package pl.polidea.robospock

import com.google.inject.ConfigurationException
import pl.polidea.robospock.activity.MainActivity
import pl.polidea.robospock.activity.TaskActivity

class DefaultSpec extends RoboSpecification {

    def "should inject view using RoboGuice"() {
        given:
        def mainActivity = new MainActivity()
        mainActivity.onCreate(null)

        when:
        def text = mainActivity.helloTv.text

        then:
        text == "Hello Szlif!"
    }

    def "should have 0MB memory class"() {
        given:
        def mainActivity = new MainActivity()
        mainActivity.onCreate(null)

        when:
        def text = mainActivity.memoryTv.text

        then:
        text == "I have 0 MB"
    }

    def "should instance null Async Task Executor"() {
        given:
        def taskActivity = new TaskActivity()

        when:
        taskActivity.onCreate(null)

        then:
        thrown(ConfigurationException)
    }

}
