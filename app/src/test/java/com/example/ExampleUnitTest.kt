package com.example

import com.example.data.MessageSender
import com.example.data.SipraMessage
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testSipraMessageCreation() {
    val message = SipraMessage(
      sender = MessageSender.SIPRA,
      text = "All systems nominal. Lyra online."
    )
    assertEquals(MessageSender.SIPRA, message.sender)
    assertEquals("All systems nominal. Lyra online.", message.text)
    assertFalse(message.isError)
    assertTrue(message.id.isNotEmpty())
  }
}
