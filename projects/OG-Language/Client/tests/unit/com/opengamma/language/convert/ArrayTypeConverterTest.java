/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Tests the {@link ArrayTypeConverter} class.
 */
public class ArrayTypeConverterTest extends AbstractConverterTest {

  private final ArrayTypeConverter _converter = new ArrayTypeConverter();

  @Test
  public void testCanConvertTo() {
    assertEquals(true, _converter.canConvertTo(JavaTypeInfo.builder(String.class).arrayOf().get()));
    assertEquals(true, _converter.canConvertTo(JavaTypeInfo.builder(String.class).arrayOf().get()));
    assertEquals(false, _converter.canConvertTo(JavaTypeInfo.builder(String.class).get()));
    assertEquals(true, _converter.canConvertTo(JavaTypeInfo.builder(Integer.class).arrayOf().get()));
    assertEquals(true, _converter.canConvertTo(JavaTypeInfo.builder(int[].class).get()));
  }

  @Test
  public void testGetConversionsTo() {
    assertConversionCount(1, _converter, JavaTypeInfo.builder(String.class).arrayOf().get());
    assertConversionCount(1, _converter, JavaTypeInfo.builder(Integer.class).arrayOf().get());
    assertConversionCount(1, _converter, JavaTypeInfo.builder(Boolean.class).arrayOf().get());
  }

  @Test
  public void testConversion_1D() {
    final String[] a = new String[] {"42", "0", "100" };
    final Integer[] b = new Integer[] {42, 0, 100 };
    assertValidConversion(_converter, a, JavaTypeInfo.builder(Integer.class).arrayOf().get(), b);
    assertValidConversion(_converter, b, JavaTypeInfo.builder(String.class).arrayOf().get(), a);
  }

  @Test
  public void testConversion_2D() {
    final String[][] a = new String[][] {new String[] {"1", "2" }, new String[] {"3", "4" } };
    final Integer[][] b = new Integer[][] {new Integer[] {1, 2 }, new Integer[] {3, 4 } };
    assertValidConversion(_converter, a, JavaTypeInfo.builder(Integer.class).arrayOf().arrayOf().get(), b);
    assertValidConversion(_converter, b, JavaTypeInfo.builder(String.class).arrayOf().arrayOf().get(), a);
  }

  @Test
  public void testPrimitive() {
    final int[] a = new int[] {42, 0, 100 };
    final double[] b = new double[] {42, 0, 100 };
    assertValidConversion(_converter, a, JavaTypeInfo.builder(double[].class).get(), b);
    assertValidConversion(_converter, b, JavaTypeInfo.builder(int[].class).get(), a);
  }

}
