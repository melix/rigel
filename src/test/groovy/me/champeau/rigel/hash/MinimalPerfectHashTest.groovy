package me.champeau.rigel.hash

import me.champeau.rigel.fixtures.Jumbles
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll
import static java.util.Optional.*

class MinimalPerfectHashTest extends Specification {
    @Subject
    @Shared
    Jumbles jumbles = Jumbles.of()

    @Unroll("Unscrambled #scrambled is #expected")
    void "should guess scrambled word"() {
        expect:
        jumbles.guess(scrambled) == expected

        where:
        scrambled | expected
        "ippyz"   | of("zippy")
        "zaaem"   | of("amaze")
        "rwdoc"   | of("crowd")
        "tlufan"  | of("flaunt")
        "prout"   | empty()
    }
}
