/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;

/**
 * Test IdentifierSearch.
 */
public class IdentifierSearchTest {

  private final Identifier _id11 = Identifier.of("D1", "V1");
  private final Identifier _id21 = Identifier.of("D2", "V1");
  private final Identifier _id12 = Identifier.of("D1", "V2");

  //-------------------------------------------------------------------------
  @Test
  public void test_constructor_noargs() {
    IdentifierSearch test = new IdentifierSearch();
    assertEquals(0, test.size());
  }

  //-------------------------------------------------------------------------
  @Test(expected = IllegalArgumentException.class)
  public void test_constructor_Identifier_null() {
    new IdentifierSearch((Identifier) null);
  }

  @Test
  public void test_constructor_Identifier() {
    IdentifierSearch test = new IdentifierSearch(_id11);
    assertEquals(1, test.size());
    assertEquals(Sets.newHashSet(_id11), test.getIdentifiers());
    assertEquals(IdentifierSearchType.ANY, test.getSearchType());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_constructor_varargs_noIdentifiers() {
    Identifier[] args = new Identifier[0];
    IdentifierSearch test = new IdentifierSearch(args);
    assertEquals(0, test.size());
  }

  @Test
  public void test_constructor_varargs_oneIdentifier() {
    Identifier[] args = new Identifier[] {_id11};
    IdentifierSearch test = new IdentifierSearch(args);
    assertEquals(1, test.size());
    assertEquals(Sets.newHashSet(_id11), test.getIdentifiers());
  }

  @Test
  public void test_constructor_varargs_twoIdentifiers() {
    Identifier[] args = new Identifier[] {_id11, _id12};
    IdentifierSearch test = new IdentifierSearch(args);
    assertEquals(2, test.size());
    assertEquals(Sets.newHashSet(_id11, _id12), test.getIdentifiers());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_varargs_null() {
    Identifier[] args = null;
    new IdentifierSearch(args);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_varargs_noNulls() {
    Identifier[] args = new Identifier[] {_id11, null, _id12};
    new IdentifierSearch(args);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_constructor_Iterable_empty() {
    IdentifierSearch test = new IdentifierSearch(new ArrayList<Identifier>());
    assertEquals(0, test.size());
  }

  @Test
  public void test_constructor_Iterable_two() {
    IdentifierSearch test = new IdentifierSearch(Arrays.asList(_id11, _id12));
    assertEquals(2, test.size());
    assertEquals(Sets.newHashSet(_id11, _id12), test.getIdentifiers());
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_constructor_Iterable_null() {
    new IdentifierSearch((Iterable<Identifier>) null);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_Iterable_noNulls() {
    new IdentifierSearch(Arrays.asList(_id11, null, _id12));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_singleIdentifierDifferentConstructors() {
    assertTrue(new IdentifierSearch(_id11).equals(new IdentifierSearch(Collections.singleton(_id11))));
  }

  @Test
  public void test_singleVersusMultipleIdentifier() {
    assertFalse(new IdentifierSearch(_id11).equals(new IdentifierSearch(_id11, _id12)));
    assertFalse(new IdentifierSearch(_id11, _id12).equals(new IdentifierSearch(_id11)));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_addIdentifier() {
    IdentifierSearch test = new IdentifierSearch(Identifier.of("A", "B"));
    assertEquals(1, test.size());
    test.addIdentifier(Identifier.of("A", "C"));
    assertEquals(2, test.size());
    assertTrue(test.getIdentifiers().contains(Identifier.of("A", "B")));
    assertTrue(test.getIdentifiers().contains(Identifier.of("A", "C")));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_addIdentifier_null() {
    IdentifierSearch test = new IdentifierSearch(Identifier.of("A", "B"));
    test.addIdentifier(null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_removeIdentifier_match() {
    IdentifierSearch test = new IdentifierSearch(Identifier.of("A", "B"));
    assertEquals(1, test.size());
    test.removeIdentifier(Identifier.of("A", "B"));
    assertEquals(0, test.size());
  }

  @Test
  public void test_removeIdentifier_noMatch() {
    IdentifierSearch test = new IdentifierSearch(Identifier.of("A", "B"));
    assertEquals(1, test.size());
    test.removeIdentifier(Identifier.of("A", "C"));
    assertEquals(1, test.size());
    assertTrue(test.getIdentifiers().contains(Identifier.of("A", "B")));
  }

  @Test
  public void test_removeIdentifier_null() {
    IdentifierSearch test = new IdentifierSearch(Identifier.of("A", "B"));
    assertEquals(1, test.size());
    test.removeIdentifier(null);
    assertEquals(1, test.size());
    assertTrue(test.getIdentifiers().contains(Identifier.of("A", "B")));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_setSearchType() {
    IdentifierSearch test = new IdentifierSearch(Identifier.of("A", "B"));
    assertEquals(IdentifierSearchType.ANY, test.getSearchType());
    test.setSearchType(IdentifierSearchType.EXACT);
    assertEquals(IdentifierSearchType.EXACT, test.getSearchType());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_setSearchType_null() {
    IdentifierSearch test = new IdentifierSearch(Identifier.of("A", "B"));
    test.setSearchType(null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_size() {
    assertEquals(0, new IdentifierSearch().size());
    assertEquals(1, new IdentifierSearch(_id11).size());
    assertEquals(2, new IdentifierSearch(_id11, _id12).size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_iterator() {
    Set<Identifier> expected = new HashSet<Identifier>();
    expected.add(_id11);
    expected.add(_id12);
    Iterable<Identifier> base = new IdentifierSearch(_id11, _id12);
    Iterator<Identifier> test = base.iterator();
    assertEquals(true, test.hasNext());
    assertEquals(true, expected.remove(test.next()));
    assertEquals(true, test.hasNext());
    assertEquals(true, expected.remove(test.next()));
    assertEquals(false, test.hasNext());
    assertEquals(0, expected.size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_matches_EXACT() {
    IdentifierSearch test = new IdentifierSearch(_id11, _id12);
    test.setSearchType(IdentifierSearchType.EXACT);
    assertEquals(true, test.matches(new IdentifierSearch(_id11, _id12)));
    assertEquals(false, test.matches(new IdentifierSearch(_id11, _id12, _id21)));
    assertEquals(false, test.matches(new IdentifierSearch(_id11)));
    assertEquals(false, test.matches(new IdentifierSearch(_id12)));
    assertEquals(false, test.matches(new IdentifierSearch(_id21)));
    assertEquals(false, test.matches(new IdentifierSearch()));
  }

  @Test
  public void test_matches_ALL() {
    IdentifierSearch test = new IdentifierSearch(_id11, _id12);
    test.setSearchType(IdentifierSearchType.ALL);
    assertEquals(true, test.matches(new IdentifierSearch(_id11, _id12)));
    assertEquals(true, test.matches(new IdentifierSearch(_id11, _id12, _id21)));
    assertEquals(false, test.matches(new IdentifierSearch(_id11)));
    assertEquals(false, test.matches(new IdentifierSearch(_id12)));
    assertEquals(false, test.matches(new IdentifierSearch(_id21)));
    assertEquals(false, test.matches(new IdentifierSearch()));
  }

  @Test
  public void test_matches_ANY() {
    IdentifierSearch test = new IdentifierSearch(_id11, _id12);
    test.setSearchType(IdentifierSearchType.ANY);
    assertEquals(true, test.matches(new IdentifierSearch(_id11, _id12)));
    assertEquals(true, test.matches(new IdentifierSearch(_id11, _id12, _id21)));
    assertEquals(true, test.matches(new IdentifierSearch(_id11)));
    assertEquals(true, test.matches(new IdentifierSearch(_id12)));
    assertEquals(false, test.matches(new IdentifierSearch(_id21)));
    assertEquals(false, test.matches(new IdentifierSearch()));
  }

  @Test
  public void test_matches_NONE() {
    IdentifierSearch test = new IdentifierSearch(_id11, _id12);
    test.setSearchType(IdentifierSearchType.NONE);
    assertEquals(false, test.matches(new IdentifierSearch(_id11, _id12)));
    assertEquals(false, test.matches(new IdentifierSearch(_id11, _id12, _id21)));
    assertEquals(false, test.matches(new IdentifierSearch(_id11)));
    assertEquals(false, test.matches(new IdentifierSearch(_id12)));
    assertEquals(true, test.matches(new IdentifierSearch(_id21)));
    assertEquals(true, test.matches(new IdentifierSearch()));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_containsAll1() {
    IdentifierSearch test = new IdentifierSearch(_id11);
    assertEquals(false, test.containsAll(new IdentifierSearch(_id11, _id12)));
    assertEquals(true, test.containsAll(new IdentifierSearch(_id11)));
    assertEquals(false, test.containsAll(new IdentifierSearch(_id12)));
    assertEquals(false, test.containsAll(new IdentifierSearch(_id21)));
    assertEquals(true, test.containsAll(new IdentifierSearch()));
  }

  @Test
  public void test_containsAll2() {
    IdentifierSearch test = new IdentifierSearch(_id11, _id12);
    assertEquals(true, test.containsAll(new IdentifierSearch(_id11, _id12)));
    assertEquals(true, test.containsAll(new IdentifierSearch(_id11)));
    assertEquals(true, test.containsAll(new IdentifierSearch(_id12)));
    assertEquals(false, test.containsAll(new IdentifierSearch(_id21)));
    assertEquals(true, test.containsAll(new IdentifierSearch()));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_containsAll_null() {
    IdentifierSearch test = new IdentifierSearch(_id11, _id12);
    test.containsAll(null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_containsAny() {
    IdentifierSearch test = new IdentifierSearch(_id11, _id12);
    assertEquals(true, test.containsAny(new IdentifierSearch(_id11, _id12)));
    assertEquals(true, test.containsAny(new IdentifierSearch(_id11)));
    assertEquals(true, test.containsAny(new IdentifierSearch(_id12)));
    assertEquals(false, test.containsAny(new IdentifierSearch(_id21)));
    assertEquals(false, test.containsAny(new IdentifierSearch()));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_containsAny_null() {
    IdentifierSearch test = new IdentifierSearch(_id11, _id12);
    test.containsAny(null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_contains() {
    IdentifierSearch test = new IdentifierSearch(_id11, _id12);
    assertEquals(true, test.contains(_id11));
    assertEquals(true, test.contains(_id11));
    assertEquals(false, test.contains(_id21));
  }

  @Test
  public void test_contains_null() {
    IdentifierSearch test = new IdentifierSearch(_id11, _id12);
    assertEquals(false, test.contains(null));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_canMatch_EXACT() {
    IdentifierSearch test = new IdentifierSearch();
    test.setSearchType(IdentifierSearchType.EXACT);
    assertEquals(false, IdentifierSearch.canMatch(test));
    test.addIdentifier(_id11);
    assertEquals(true, IdentifierSearch.canMatch(test));
  }

  @Test
  public void test_canMatch_ALL() {
    IdentifierSearch test = new IdentifierSearch();
    test.setSearchType(IdentifierSearchType.ALL);
    assertEquals(false, IdentifierSearch.canMatch(test));
    test.addIdentifier(_id11);
    assertEquals(true, IdentifierSearch.canMatch(test));
  }

  @Test
  public void test_canMatch_ANY() {
    IdentifierSearch test = new IdentifierSearch();
    test.setSearchType(IdentifierSearchType.ANY);
    assertEquals(false, IdentifierSearch.canMatch(test));
    test.addIdentifier(_id11);
    assertEquals(true, IdentifierSearch.canMatch(test));
  }

  @Test
  public void test_canMatch_NONE() {
    IdentifierSearch test = new IdentifierSearch();
    test.setSearchType(IdentifierSearchType.NONE);
    assertEquals(true, IdentifierSearch.canMatch(test));
    test.addIdentifier(_id11);
    assertEquals(true, IdentifierSearch.canMatch(test));
  }

  @Test
  public void test_canMatch_null() {
    assertEquals(true, IdentifierSearch.canMatch(null));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_equals_same_empty() {
    IdentifierSearch a1 = new IdentifierSearch();
    IdentifierSearch a2 = new IdentifierSearch();
    
    assertEquals(true, a1.equals(a1));
    assertEquals(true, a1.equals(a2));
    assertEquals(true, a2.equals(a1));
    assertEquals(true, a2.equals(a2));
  }

  @Test
  public void test_equals_same_nonEmpty() {
    IdentifierSearch a1 = new IdentifierSearch(_id11, _id12);
    IdentifierSearch a2 = new IdentifierSearch(_id11, _id12);
    
    assertEquals(true, a1.equals(a1));
    assertEquals(true, a1.equals(a2));
    assertEquals(true, a2.equals(a1));
    assertEquals(true, a2.equals(a2));
  }

  @Test
  public void test_equals_different() {
    IdentifierSearch a = new IdentifierSearch();
    IdentifierSearch b = new IdentifierSearch(_id11, _id12);
    
    assertEquals(true, a.equals(a));
    assertEquals(false, a.equals(b));
    assertEquals(false, b.equals(a));
    assertEquals(true, b.equals(b));
    
    assertEquals(false, b.equals("Rubbish"));
    assertEquals(false, b.equals(null));
  }

  @Test
  public void test_hashCode() {
    IdentifierSearch a = new IdentifierSearch(_id11, _id12);
    IdentifierSearch b = new IdentifierSearch(_id11, _id12);
    
    assertEquals(a.hashCode(), b.hashCode());
  }

  @Test
  public void test_toString_empty() {
    IdentifierSearch test = new IdentifierSearch();
    assertEquals("Search[]", test.toString());
  }

  @Test
  public void test_toString_nonEmpty() {
    IdentifierSearch test = new IdentifierSearch(_id11, _id12);
    assertEquals("Search[" + _id11.toString() + ", " + _id12.toString() + "]", test.toString());
  }

}