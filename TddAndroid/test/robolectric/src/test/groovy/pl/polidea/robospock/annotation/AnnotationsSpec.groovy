package pl.polidea.robospock.annotation


import org.junit.runner.RunWith

import pl.polidea.robospock.RoboSputnik
//import pl.polidea.robospock.RobolectricGuiceModules
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
//@RobolectricGuiceModules([TestTaskExecutorModule,TestWebModule])
@RunWith(RoboSputnik)
class ShadowGuiceSputnikSpec extends AnnotationsSpec {
}

@UseShadows(MyActivityManagerShadow)
@RunWith(RoboSputnik)
//@RobolectricGuiceModules([TestTaskExecutorModule,TestWebModule])
class ShadowSputnikGuiceSpec extends AnnotationsSpec{
}

@RunWith(RoboSputnik)
@UseShadows(MyActivityManagerShadow)
//@RobolectricGuiceModules([TestTaskExecutorModule,TestWebModule])
class SputnikShadowGuiceSpec extends AnnotationsSpec{
}

@RunWith(RoboSputnik)
//@RobolectricGuiceModules([TestTaskExecutorModule,TestWebModule])
@UseShadows(MyActivityManagerShadow)
class SputnikGuiceShadowSpec extends AnnotationsSpec{
}

//@RobolectricGuiceModules([TestTaskExecutorModule,TestWebModule])
@UseShadows(MyActivityManagerShadow)
@RunWith(RoboSputnik)
class GuiceShadowSputnikSpec extends AnnotationsSpec{
}

//@RobolectricGuiceModules([TestTaskExecutorModule,TestWebModule])
@RunWith(RoboSputnik)
@UseShadows(MyActivityManagerShadow)
class GuiceSputnikShadowSpec extends AnnotationsSpec{
}