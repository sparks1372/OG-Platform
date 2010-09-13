/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGatherer;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;
import com.opengamma.engine.view.calcnode.JobResultReceiver;
import com.opengamma.engine.view.calcnode.stats.FunctionCost;
import com.opengamma.util.tuple.Pair;

/**
 * This DependencyGraphExecutor executes the given dependency graph
 * on a number of calculation nodes.
 */
public class MultipleNodeExecutor implements DependencyGraphExecutor<Object> {

  private final SingleComputationCycle _cycle;
  private final int _minJobItems;
  private final int _maxJobItems;
  private final long _minJobCost;
  private final long _maxJobCost;
  private final int _maxConcurrency;
  private final FunctionCost _functionCost;

  protected MultipleNodeExecutor(final SingleComputationCycle cycle, final int minimumJobItems, final int maximumJobItems, final long minimumJobCost, final long maximumJobCost,
      final int maximumConcurrency, final FunctionCost functionCost) {
    // Don't check for null as the factory does this, plus for testing we don't have a cycle and override the methods that use it
    _cycle = cycle;
    _minJobItems = minimumJobItems;
    _maxJobItems = maximumJobItems;
    _minJobCost = minimumJobCost;
    _maxJobCost = maximumJobCost;
    _maxConcurrency = maximumConcurrency;
    _functionCost = functionCost;
  }

  protected SingleComputationCycle getCycle() {
    return _cycle;
  }

  protected CalculationJobSpecification createJobSpecification(final DependencyGraph graph) {
    return new CalculationJobSpecification(getCycle().getViewName(), graph.getCalcConfName(), getCycle().getValuationTime().toEpochMillisLong(), JobIdSource.getId());
  }

  protected void addJobToViewProcessorQuery(final CalculationJobSpecification jobSpec, final DependencyGraph graph) {
    getCycle().getProcessingContext().getViewProcessorQueryReceiver().addJob(jobSpec, graph);
  }

  protected void dispatchJob(final CalculationJob job, final JobResultReceiver jobResultReceiver) {
    getCycle().getProcessingContext().getComputationJobDispatcher().dispatchJob(job, jobResultReceiver);
  }

  protected void markExecuted(final DependencyNode node) {
    getCycle().markExecuted(node);
  }

  protected void markFailed(final DependencyNode node) {
    getCycle().markFailed(node);
  }

  protected RootGraphFragment executeImpl(final DependencyGraph graph, final GraphExecutorStatisticsGatherer statistics) {
    final GraphFragmentContext context = new GraphFragmentContext(this, graph);
    // writeGraphForTestingPurposes(graph);
    if (graph.getSize() <= getMinJobItems()) {
      // If the graph is too small, run it as-is
      final RootGraphFragment fragment = new RootGraphFragment(context, statistics, graph.getExecutionOrder());
      statistics.graphProcessed(graph.getCalcConfName(), 1, graph.getSize(), fragment.getJobInvocationCost());
      context.allocateFragmentMap(1);
      fragment.executeImpl();
      return fragment;
    }
    final Set<GraphFragment> allFragments = new HashSet<GraphFragment>((graph.getSize() * 4) / 3);
    final RootGraphFragment logicalRoot = new RootGraphFragment(context, statistics);
    for (GraphFragment root : graphToFragments(context, graph, allFragments)) {
      root.getDependencies().add(logicalRoot);
      logicalRoot.getInputs().add(root);
    }
    int failCount = 0;
    do {
      if (mergeSharedInputs(logicalRoot, allFragments)) {
        failCount = 0;
      } else {
        if (++failCount >= 2) {
          break;
        }
      }
      if (mergeSingleDependencies(allFragments)) {
        failCount = 0;
      } else {
        if (++failCount >= 2) {
          break;
        }
      }
    } while (true);
    findTailFragments(allFragments);
    context.allocateFragmentMap(allFragments.size());
    // Set block counts on non-leaf nodes & leave only the leaves in the set
    logicalRoot.initBlockCount();
    final Iterator<GraphFragment> fragmentIterator = allFragments.iterator();
    final int count = allFragments.size();
    int totalSize = 0;
    long totalInvocationCost = 0;
    while (fragmentIterator.hasNext()) {
      final GraphFragment fragment = fragmentIterator.next();
      totalSize += fragment.getJobItems();
      totalInvocationCost += fragment.getJobInvocationCost();
      if (!fragment.getInputs().isEmpty()) {
        fragment.initBlockCount();
        fragmentIterator.remove();
      }
    }
    statistics.graphProcessed(graph.getCalcConfName(), count, (double) totalSize / (double) count, (double) totalInvocationCost / (double) count);
    //printFragment(logicalRoot);
    // Execute anything left (leaf nodes)
    for (GraphFragment fragment : allFragments) {
      fragment.execute();
    }
    return logicalRoot;
  }

  @Override
  public Future<Object> execute(final DependencyGraph graph, final GraphExecutorStatisticsGatherer statistics) {
    return executeImpl(graph, statistics).getFuture();
  }

  public int getMinJobItems() {
    return _minJobItems;
  }

  public int getMaxJobItems() {
    return _maxJobItems;
  }

  public long getMinJobCost() {
    return _minJobCost;
  }

  public long getMaxJobCost() {
    return _maxJobCost;
  }

  public int getMaxConcurrency() {
    return _maxConcurrency;
  }

  public FunctionCost getFunctionCost() {
    return _functionCost;
  }

  private Collection<GraphFragment> graphToFragments(final GraphFragmentContext context, final DependencyGraph graph, final Set<GraphFragment> allFragments) {
    final Map<DependencyNode, GraphFragment> node2fragment = new HashMap<DependencyNode, GraphFragment>();
    final Collection<DependencyNode> rootNodes = graph.getRootNodes();
    final Collection<GraphFragment> rootFragments = new ArrayList<GraphFragment>(rootNodes.size());
    graphToFragments(context, graph, rootFragments, node2fragment, rootNodes);
    allFragments.addAll(node2fragment.values());
    return rootFragments;
  }

  private void graphToFragments(final GraphFragmentContext context, final DependencyGraph graph, final Collection<GraphFragment> output, final Map<DependencyNode, GraphFragment> node2fragment,
      final Collection<DependencyNode> nodes) {
    // TODO Andrew 2010-09-02 -- Can we do this by iterating the graph nodes instead of walking the tree?
    for (DependencyNode node : nodes) {
      if (!graph.containsNode(node)) {
        continue;
      }
      GraphFragment fragment = node2fragment.get(node);
      if (fragment == null) {
        fragment = new GraphFragment(context, node);
        node2fragment.put(node, fragment);
        final Collection<DependencyNode> inputNodes = node.getInputNodes();
        if (!inputNodes.isEmpty()) {
          graphToFragments(context, graph, fragment.getInputs(), node2fragment, inputNodes);
          for (GraphFragment input : fragment.getInputs()) {
            input.getDependencies().add(fragment);
          }
        }
      }
      output.add(fragment);
    }
  }

  /**
   * Finds pairs of nodes with the same input set (i.e. that would execute concurrently) that are below the minimum job size
   * and merge them together.
   */
  private boolean mergeSharedInputs(final GraphFragment logicalRoot, final Set<GraphFragment> allFragments) {
    final Map<Set<GraphFragment>, GraphFragment> possibleCandidates = new HashMap<Set<GraphFragment>, GraphFragment>();
    // REVIEW 2010-08-27 Andrew -- Should we only create validCandidates when we're ready to use it?
    final Map<GraphFragment, GraphFragment> validCandidates = new HashMap<GraphFragment, GraphFragment>();
    boolean result = false;
    do {
      // Scan all fragments for possible merges
      for (GraphFragment fragment : allFragments) {
        if (fragment.getInputs().isEmpty()) {
          // No inputs to consider
          continue;
        }
        if ((fragment.getJobCost() >= getMinJobCost()) && (fragment.getJobItems() >= getMinJobItems())) {
          // We already meet the minimum requirement for the graph
          continue;
        }
        final GraphFragment mergeCandidate = possibleCandidates.get(fragment.getInputs());
        if (mergeCandidate != null) {
          if ((mergeCandidate.getJobCost() + fragment.getJobCost() <= getMaxJobCost()) && (mergeCandidate.getJobItems() + fragment.getJobItems() <= getMaxJobItems())) {
            // Defer the merge because we're iterating through the dependent's inputs at the moment
            validCandidates.put(fragment, mergeCandidate);
            // Stop using the merge candidate
            possibleCandidates.remove(fragment.getInputs());
            continue;
          }
          if (fragment.getJobCost() >= mergeCandidate.getJobCost()) {
            // We are a worse possible candidate as we're already more expensive
            continue;
          }
        }
        possibleCandidates.put(fragment.getInputs(), fragment);
      }
      if (validCandidates.isEmpty()) {
        return result;
      }
      for (Map.Entry<GraphFragment, GraphFragment> merge : validCandidates.entrySet()) {
        final GraphFragment fragment = merge.getKey();
        final GraphFragment mergeCandidate = merge.getValue();
        mergeCandidate.appendFragment(fragment);
        // Merge candidate already has the correct inputs by definition
        for (GraphFragment dependency : fragment.getDependencies()) {
          dependency.getInputs().remove(fragment);
          if (mergeCandidate.getDependencies().add(dependency)) {
            dependency.getInputs().add(mergeCandidate);
          }
        }
        for (GraphFragment input : fragment.getInputs()) {
          input.getDependencies().remove(fragment);
        }
        allFragments.remove(fragment);
      }
      // If deep nodes have merged with "root" nodes then we need to kill the roots
      final Iterator<GraphFragment> fragmentIterator = logicalRoot.getInputs().iterator();
      while (fragmentIterator.hasNext()) {
        final GraphFragment fragment = fragmentIterator.next();
        if (fragment.getDependencies().size() > 1) {
          fragment.getDependencies().remove(logicalRoot);
          fragmentIterator.remove();
        }
      }
      validCandidates.clear();
      possibleCandidates.clear();
      result = true;
    } while (true);
  }

  /**
   * If a fragment has only one dependency, and both it and its dependent are below the
   * maximum job size they are merged.
   */
  private boolean mergeSingleDependencies(final Set<GraphFragment> allFragments) {
    int changes = 0;
    final Iterator<GraphFragment> fragmentIterator = allFragments.iterator();
    while (fragmentIterator.hasNext()) {
      final GraphFragment fragment = fragmentIterator.next();
      if (fragment.getDependencies().size() != 1) {
        continue;
      }
      final GraphFragment dependency = fragment.getDependencies().iterator().next();
      if (dependency.getNodes().isEmpty()) {
        // Ignore the roots
        continue;
      }
      if ((fragment.getJobItems() + dependency.getJobItems() > getMaxJobItems()) || (fragment.getJobCost() + dependency.getJobCost() > getMaxJobCost())) {
        // Can't merge
        continue;
      }
      // Merge fragment with it's dependency and slice it out of the graph
      dependency.prependFragment(fragment);
      fragmentIterator.remove();
      dependency.getInputs().remove(fragment);
      for (GraphFragment input : fragment.getInputs()) {
        dependency.getInputs().add(input);
        input.getDependencies().remove(fragment);
        input.getDependencies().add(dependency);
      }
      changes++;
    }
    return changes > 0;
  }

  /**
   * If a fragment has only a single input, it can be a tail to the fragment generating that input. A fragment with multiple inputs can
   * be a tail to all of them iff they are tails to a common fragment (i.e. all will end up at the same node).
   */
  private void findTailFragments(final Set<GraphFragment> allFragments) {
    // Estimate start times based on fragment costs and dependencies
    final NavigableMap<Long, Pair<List<GraphFragment>, List<GraphFragment>>> concurrencyEvent = new TreeMap<Long, Pair<List<GraphFragment>, List<GraphFragment>>>();
    final int cacheKey = allFragments.size(); // Any changes to the graph reduce this, so we use it to cache the start time
    for (GraphFragment fragment : allFragments) {
      Pair<List<GraphFragment>, List<GraphFragment>> event = concurrencyEvent.get(fragment.getStartTime(cacheKey));
      if (event == null) {
        event = Pair.of((List<GraphFragment>) new LinkedList<GraphFragment>(), null);
        concurrencyEvent.put(fragment.getStartTime(cacheKey), event);
      } else {
        if (event.getFirst() == null) {
          event = Pair.of((List<GraphFragment>) new LinkedList<GraphFragment>(), event.getSecond());
          concurrencyEvent.put(fragment.getStartTime(cacheKey), event);
        }
      }
      event.getFirst().add(fragment);
      event = concurrencyEvent.get(fragment.getStartTime(cacheKey) + fragment.getJobCost());
      if (event == null) {
        event = Pair.of(null, (List<GraphFragment>) new LinkedList<GraphFragment>());
        concurrencyEvent.put(fragment.getStartTime(cacheKey) + fragment.getJobCost(), event);
      } else {
        if (event.getSecond() == null) {
          event = Pair.of(event.getFirst(), (List<GraphFragment>) new LinkedList<GraphFragment>());
          concurrencyEvent.put(fragment.getStartTime(cacheKey) + fragment.getJobCost(), event);
        }
      }
      event.getSecond().add(fragment);
    }
    // Walk the execution plan, coloring the graph with potential invocation sites
    final Map<Integer, AtomicInteger> executing = new HashMap<Integer, AtomicInteger>();
    int nextColour = 0;
    for (Map.Entry<Long, Pair<List<GraphFragment>, List<GraphFragment>>> eventEntry : concurrencyEvent.entrySet()) {
      final Pair<List<GraphFragment>, List<GraphFragment>> event = eventEntry.getValue();
      if (event.getSecond() != null) {
        for (GraphFragment finishing : event.getSecond()) {
          // Decrement the concurrency count for the graph color
          executing.get(finishing.getColour()).decrementAndGet();
        }
      }
      if (event.getFirst() != null) {
        for (GraphFragment starting : event.getFirst()) {
          if (starting.getInputs().isEmpty()) {
            // No inputs, so we're a leaf node = new graph color
            nextColour++;
            starting.setColour(nextColour);
            executing.put(nextColour, new AtomicInteger(1));
          } else if (starting.getInputs().size() == 1) {
            // Single input, become the tail with the same graph color if below the concurrency limit
            final GraphFragment tailOf = starting.getInputs().iterator().next();
            final AtomicInteger concurrency = executing.get(tailOf.getColour());
            if (concurrency.get() >= getMaxConcurrency()) {
              // Concurrency limit exceeded so start a new color
              nextColour++;
              starting.setColour(nextColour);
              executing.put(nextColour, new AtomicInteger(1));
            } else {
              // Below concurrency limit so use same color and add as tail
              tailOf.addTail(starting);
              starting.setColour(tailOf.getColour());
              concurrency.incrementAndGet();
            }
          } else {
            final Iterator<GraphFragment> inputIterator = starting.getInputs().iterator();
            int nodeColour = inputIterator.next().getColour();
            while (inputIterator.hasNext()) {
              final GraphFragment input = inputIterator.next();
              if (input.getColour() != nodeColour) {
                // Inputs are from different colored graph fragments = new graph color
                nextColour++;
                starting.setColour(nextColour);
                executing.put(nextColour, new AtomicInteger(1));
                nodeColour = -1;
                break;
              }
            }
            if (nodeColour > 0) {
              // Inputs are all from the same colored graph fragments = become tail with the same color if below concurrency limit
              final AtomicInteger concurrency = executing.get(nodeColour);
              if (concurrency.get() >= getMaxConcurrency()) {
                // Concurrency limit exceeded so start a new color
                nextColour++;
                starting.setColour(nextColour);
                executing.put(nextColour, new AtomicInteger(1));
              } else {
                // Below concurrency limit so use same color and add as tails
                starting.setColour(nodeColour);
                concurrency.incrementAndGet();
                for (GraphFragment input : starting.getInputs()) {
                  input.addTail(starting);
                }
              }
            }
          }
        }
      }
    }
  }

  public void printFragment(final GraphFragment root) {
    printFragment("", Collections.singleton(root), new HashSet<GraphFragment>());
  }

  private void printFragment(final String indent, final Collection<GraphFragment> fragments, final Set<GraphFragment> printed) {
    if (indent.length() > 16) {
      return;
    }
    for (GraphFragment fragment : fragments) {
      /*
       * if (!printed.add(fragment)) {
       * System.out.println(indent + " Fragments " + fragment.fragmentList());
       * continue;
       * }
       */
      System.out.println(indent + " " + fragment);
      if (!fragment.getInputs().isEmpty()) {
        printFragment(indent + "  ", fragment.getInputs(), printed);
      }
    }
  }

}
