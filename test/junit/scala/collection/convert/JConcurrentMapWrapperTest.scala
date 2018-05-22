package scala.collection.convert

import java.util.concurrent.{ConcurrentHashMap, CyclicBarrier}
import java.util.concurrent.atomic.AtomicInteger

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(classOf[JUnit4])
class JConcurrentMapWrapperTest {

  @Test
  def getOrElseUpdate(): Unit = {
    val underlying = new ConcurrentHashMap[Int, Int]()
    val wrapper = Wrappers.JConcurrentMapWrapper(underlying)

    assertEquals(10, wrapper.getOrElseUpdate(1, 10))
    assertEquals(10, wrapper.getOrElseUpdate(1, 10))
    assertEquals(20, wrapper.getOrElseUpdate(2, 20))
    assertEquals(20, wrapper.getOrElseUpdate(2, 20))
    assertEquals(2, wrapper.size)
  }

  @Test(expected = classOf[NullPointerException])
  def getOrElseUpdateWithNullValue(): Unit = {
    val underlying = new ConcurrentHashMap[Int, String]()
    val wrapper = Wrappers.JConcurrentMapWrapper(underlying)

    wrapper.getOrElseUpdate(1, null)
  }

  @Test
  def getOrElseUpdateShouldBeAtomicIfUnderlyingMapSupportsAtomicComputeIfAbsent(): Unit = {
    val maxIterations = 10000
    val threadCount = 4
    val count = new AtomicInteger()
    val barrier = new CyclicBarrier(threadCount)
    val underlying = new ConcurrentHashMap[Int, Int]()
    val wrapper = Wrappers.JConcurrentMapWrapper(underlying)

    val runnable: Runnable = () => {
      barrier.await()
      for (i <- 1 to maxIterations)
        wrapper.getOrElseUpdate(i, { count.incrementAndGet(); i})
    }

    val threads = (1 to threadCount).map { i =>
      val t = new Thread(runnable, s"thread-$i")
      t.start()
      t
    }

    threads.foreach(_.join())

    assertEquals(maxIterations, count.get())
    assertEquals(maxIterations, wrapper.size)
  }
}
