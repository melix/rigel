package me.champeau.rigel.hash

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import me.champeau.rigel.fixtures.Jumbles
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static java.util.Optional.empty
import static java.util.Optional.of

class MinimalPerfectHashTest extends Specification {
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

    void "can build a minimal perfect hasher for strings"() {
        def mphbuilder = new MPHBuilder(3, this.&stringHasher)
        def words = [
                'hello',
                'world',
                'how',
                'are',
                'you',
                '?'
        ]
        when:
        words.each { mphbuilder.add(it) }

        then:
        def hashFunc = mphbuilder.build()
        hashFunc.size() == 6
        def index = words.collectEntries {
            [hashFunc.applyAsInt(it), it]
        }
        index.keySet() == (0..<6) as Set
        index.values() as Set == words as Set

        when: "adding elements to builder"
        mphbuilder.add("heavier")
        mphbuilder.add("elements")
        hashFunc = mphbuilder.build()
        index = [*words, 'heavier', 'elements'].collectEntries {
            def idx = hashFunc.applyAsInt(it)
            println "$it -> $idx"
            [idx, it]
        }

        then:
        hashFunc.size() == 8
        index.keySet() == (0..<8) as Set
        index.values() as Set == [*words, 'heavier', 'elements'] as Set

    }

    void "can build a minimal perfect hasher for captains"() {
        def mphbuilder = new MPHBuilder(5, this.&captainHasher)
        def enterpriseCaptains = [
                new Captain("Jonathan", "Archer"),
                new Captain("Robert", "April"),
                new Captain("William", "Decker"),
                new Captain("Christopher", "Pike"),
                new Captain("James", "Kirk"),
                new Captain("Spock", "M. Vulcan"),
                new Captain("John", "Harriman"),
                new Captain("Rachel", "Garrett"),
                new Captain("Jean-Luc", "Picard"),
                new Captain("William", "Riker"),
                new Captain("Edward", "Jellico")
        ]

        when:
        enterpriseCaptains.each { mphbuilder.add(it) }

        then:
        def hashFunc = mphbuilder.build()
        hashFunc.size() == 11
        def index = enterpriseCaptains.collectEntries {
            [hashFunc.applyAsInt(it), it]
        }
        index.keySet() == (0..<11) as Set
        index.values() as Set == enterpriseCaptains as Set

    }

    void "reasonable error message when can't build hasher"() {
        def mphbuilder = new MPHBuilder(2, this.&stringHasher)
        100.times {
            mphbuilder.add("Hello $it".toString())
        }

        when:
        mphbuilder.build()

        then:
        def e = thrown(IllegalStateException)
        e.message == "Can't build minimal perfect hash function. Try increasing the number of initial buckets."
    }

    @CompileStatic
    private static int stringHasher(String str, int seed) {
        Random rnd = new Random(seed)
        int i = 0;
        for (char c : str.toCharArray()) {
            i = 37 * i + c + rnd.nextInt()
        }
        return i
    }

    @CompileStatic
    private static int captainHasher(Captain p, int seed) {
        Random rnd = new Random(seed)
        return 37 * p.firstName.hashCode() + p.lastName.hashCode() + rnd.nextInt()
    }

    @Canonical
    class Captain {
        String firstName
        String lastName
    }
}
