import org.junit.Assert
import org.junit.Test

class TestLambdas {
  @Test
  fun contains() {
    Assert.assertTrue(
      "The result should be true if the collection contains an even number",
      containsEven(listOf(1, 2, 3, 126, 555)),
    )
  }

  @Test
  fun notContains() {
    Assert.assertFalse(
      "The result should be false if the collection doesn't contain an even number",
      containsEven(listOf(43, 33)),
    )
  }
}

//sampleStart
/*
Pass a lambda to any function to check if the collection contains an even number. The function any
gets a predicate as an argument and returns true if there is at least one element satisfying the predicate.
*/
fun containsEven(collection: Collection<Int>): Boolean = collection.any { TODO() }
//sampleEnd
