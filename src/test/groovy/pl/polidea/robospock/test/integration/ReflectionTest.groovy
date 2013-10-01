package pl.polidea.robospock.test.integration

import spock.lang.Specification

class ReflectionTest extends Specification {

    def asdf

    def 'extending'() {
        given:
        Class c = ReflectionTest

        when:
        def fields = c.getDeclaredFields()

        then:
        fields == []
    }

}
