package pl.polidea.robospock.test.integration

import pl.polidea.robospock.RoboSpecification

class OmitGrooovyInternalsSpecification extends RoboSpecification {

    def "should load original Groovy Object class"() {
        given:
        def originalGroovyObjectClass = ClassLoader.getSystemClassLoader().loadClass(GroovyObject.name)

        when:
        def groovyObjectClass = GroovyObject

        then:
        groovyObjectClass == originalGroovyObjectClass
    }
}
