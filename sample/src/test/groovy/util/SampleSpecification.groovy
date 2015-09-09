package util

import com.example.robospock.BuildConfig
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import spock.lang.Specification

@RunWith(SampleSputnik)
@Config(constants = BuildConfig)
class SampleSpecification extends Specification {

}