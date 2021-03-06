package me.champeau.rigel.lazy

import me.champeau.rigel.fixtures.Fibo
import me.champeau.rigel.lazy.Lazy
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
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
        factory                                || expected
        { s -> Lazy.unsafe().of(s) } | 123
        { s -> Lazy.unsafe().of(s).map { 2 * it } }  | 246
        { s -> Lazy.locking().of(s) }                | 123
        { s -> Lazy.locking().of(s).map { 2 * it } } | 246
    }

    @Unroll
    def "lazy can handle concurrent threads (#factoryName)"() {
        def supplier = Mock(Supplier)
        def lazy = factory.of(supplier)
        def executors = Executors.newFixedThreadPool(20)

        when:
        50.times {
            executors.submit {
                assert lazy.get() == 'hello'
            }
        }
        executors.shutdown()
        executors.awaitTermination(1, TimeUnit.MINUTES)

        then:
        1 * supplier.get() >> 'hello'

        where:
        factoryName     | factory
        'locking'       | Lazy.locking()
        'synchronized'  | Lazy.synchronizing()
    }

    @Unroll
    def "locking lazy can handle concurrent threads (#factoryName)"() {
        def supplier = Mock(Supplier)
        def lazy = factory.of(supplier).map { 2 * it }
        def executors = Executors.newFixedThreadPool(20)

        when:
        50.times {
            executors.submit {
                lazy.get()
            }
        }
        executors.shutdown()
        executors.awaitTermination(1, TimeUnit.MINUTES)

        then:
        1 * supplier.get()

        where:
        factoryName     | factory
        'locking'       | Lazy.locking()
        'synchronized'  | Lazy.synchronizing()
    }

    def "can defer initialization using Lazy"() {
        def fibo = new Fibo(24)

        expect:
        fibo.directUse() == "Result is 46368"

        and:
        fibo.mappedUse() == "Mapped is 46368"
    }
}
