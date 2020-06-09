package me.champeau.rigel

import me.champeau.rigel.fixtures.Fibo
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.Executors
import java.util.function.Supplier

class LazyTest extends Specification {
    @Unroll
    def "supplier code is executed once"() {
        def supplier = Mock(Supplier)

        when:
        def lazy = factory(supplier)

        then:
        0 * supplier._

        when:
        lazy.get()

        then:
        1 * supplier.get() >> 123

        when:
        lazy.get()

        then:
        0 * supplier.get()

        when:
        lazy.use {
            assert it == expected
        }

        then:
        noExceptionThrown()

        when:
        def val = lazy.apply {
            3 * it
        }

        then:
        0 * supplier.get()
        val == 3 * expected

        where:
        factory                                   || expected
        { s -> Lazy.unsafe(s) }                | 123
        { s -> Lazy.unsafe(s).map { 2 * it } } | 246
        { s -> Lazy.locking(s) }                   | 123
        { s -> Lazy.locking(s).map { 2 * it } }    | 246
    }

    def "locking lazy can handle concurrent threads"() {
        def supplier = Mock(Supplier)
        def lazy = Lazy.locking(supplier)
        def executors = Executors.newFixedThreadPool(20)

        when:
        50.times {
            executors.submit {
                lazy.get()
            }
        }

        then:
        1 * supplier.get()
    }

    def "mapped locking lazy can handle concurrent threads"() {
        def supplier = Mock(Supplier)
        def lazy = Lazy.locking(supplier).map { 2 * it }
        def executors = Executors.newFixedThreadPool(20)

        when:
        50.times {
            executors.submit {
                lazy.get()
            }
        }

        then:
        1 * supplier.get()
    }

    def "synchronized lazy can handle concurrent threads"() {
        def supplier = Mock(Supplier)
        def lazy = Lazy.synchronizing(supplier)
        def executors = Executors.newFixedThreadPool(20)

        when:
        50.times {
            executors.submit {
                lazy.get()
            }
        }

        then:
        1 * supplier.get()
    }

    def "mapped synchronizing lazy can handle concurrent threads"() {
        def supplier = Mock(Supplier)
        def lazy = Lazy.synchronizing(supplier).map { 2 * it }
        def executors = Executors.newFixedThreadPool(20)

        when:
        50.times {
            executors.submit {
                lazy.get()
            }
        }

        then:
        1 * supplier.get()
    }

    def "can defer initialization using Lazy"() {
        def fibo = new Fibo(24)

        expect:
        fibo.directUse() == "Result is 46368"

        and:
        fibo.mappedUse() == "Mapped is 46368"
    }
}
