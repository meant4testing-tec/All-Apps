package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.pow

class ExampleUnitTest {
  @Test
  fun testCommonLogarithm() {
    val result = log10(100.0)
    assertEquals(2.0, result, 1e-9)

    val result1000 = log10(1000.0)
    assertEquals(3.0, result1000, 1e-9)
  }

  @Test
  fun testNaturalLogarithm() {
    val result = ln(Math.E)
    assertEquals(1.0, result, 1e-9)

    val resultE3 = ln(Math.E.pow(3.0))
    assertEquals(3.0, resultE3, 1e-9)
  }

  @Test
  fun testBinaryLogarithm() {
    val result = log2(8.0)
    assertEquals(3.0, result, 1e-9)

    val result1024 = log2(1024.0)
    assertEquals(10.0, result1024, 1e-9)
  }

  @Test
  fun testCustomBaseLogarithm() {
    val base = 3.0
    val arg = 81.0
    val result = ln(arg) / ln(base)
    assertEquals(4.0, result, 1e-9)
  }

  @Test
  fun testAntilogarithm() {
    val base = 10.0
    val exponent = 2.0
    val result = base.pow(exponent)
    assertEquals(100.0, result, 1e-9)

    val base2 = 2.0
    val exponent3 = 3.0
    assertEquals(8.0, base2.pow(exponent3), 1e-9)
  }

  @Test
  fun testCharacteristicAndMantissa() {
    val x = 250.0
    val logVal = log10(x)
    val characteristic = floor(logVal).toLong()
    val mantissa = logVal - characteristic

    assertEquals(2L, characteristic)
    assertTrue(mantissa in 0.0..1.0)
    assertEquals(logVal, characteristic + mantissa, 1e-9)
  }
}
