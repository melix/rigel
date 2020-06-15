package me.champeau.rigel.hash

import me.champeau.rigel.fixtures.Jumbles
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

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
        "ippyz"   | "zippy"
        "zaaem"   | "amaze"
        "rwdoc"   | "crowd"
        "tlufan"  | "flaunt"
    }
}
