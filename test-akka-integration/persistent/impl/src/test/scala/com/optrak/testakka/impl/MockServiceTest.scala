package com.optrak.testakka.impl

class MockServiceTest extends PersistentServiceBase {

  override lazy val client = new MockPersistentService

}
