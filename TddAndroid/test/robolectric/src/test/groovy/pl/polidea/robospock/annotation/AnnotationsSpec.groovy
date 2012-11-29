package pl.polidea.robospock.annotation


import org.junit.runner.RunWith

import pl.polidea.robospock.RoboSputnik
import pl.polidea.robospock.UseShadows
import pl.polidea.tddandroid.activity.MainActivity;
import pl.polidea.tddandroid.module.TestTaskExecutorModule
import pl.polidea.tddandroid.module.TestWebModule
import pl.polidea.tddandroid.shadow.MyActivityManagerShadow
import spock.lang.Ignore;
import spock.lang.Specification;

@Ignore
abstract class AnnotationsSpec extends Specification{


    def "shouldCompile"(){
        given:
        def mainActivity = new MainActivity()
        mainActivity.onCreate(null);

        when:
        def text = mainActivity.memoryTv.text

        then:
        text == "I have 16 MB"
    }
}

@UseShadows(MyActivityManagerShadow)
@RunWith(RoboSputnik)
class ShadowSputnikSpec extends AnnotationsSpec {
}

@RunWith(RoboSputnik)
@UseShadows(MyActivityManagerShadow)
class SputnikShadowSpec extends AnnotationsSpec{
}
