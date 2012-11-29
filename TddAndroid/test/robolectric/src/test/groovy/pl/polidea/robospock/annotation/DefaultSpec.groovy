package pl.polidea.robospock.annotation


import com.google.inject.ConfigurationException
import org.junit.runner.RunWith
import pl.polidea.robospock.RoboSputnik
import pl.polidea.robospock.UseShadows
import pl.polidea.tddandroid.activity.MainActivity
import pl.polidea.tddandroid.activity.TaskActivity
import spock.lang.Specification

@RunWith(RoboSputnik)
@UseShadows  // just bind default shadow classes
class DefaultSpec extends Specification {

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
