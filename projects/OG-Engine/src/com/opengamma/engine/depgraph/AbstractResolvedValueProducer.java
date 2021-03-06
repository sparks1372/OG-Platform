/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.depgraph.DependencyGraphBuilderPLAT1049.GraphBuildingContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/* package */abstract class AbstractResolvedValueProducer implements ResolvedValueProducer {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractResolvedValueProducer.class);
  private static final AtomicInteger s_nextObjectId = new AtomicInteger();

  // TODO: the locking here is not very efficient; rewrite if this causes a bottleneck

  private final class Callback implements ResolutionPump, Cancellable {

    private final int _objectId = s_nextObjectId.getAndIncrement();
    private final ResolvedValueCallback _callback;
    private ResolvedValue[] _results;
    private int _resultsPushed;
    private boolean _closed;

    public Callback(final ResolvedValueCallback callback) {
      _callback = callback;
    }

    @Override
    public void pump(final GraphBuildingContext context) {
      s_logger.debug("Pump called on {}", this);
      assert !_closed;
      ResolvedValue nextValue = null;
      boolean finished = false;
      ResolutionFailure failure = null;
      boolean needsOuterPump = false;
      synchronized (this) {
        if (_resultsPushed < _results.length) {
          nextValue = _results[_resultsPushed++];
        }
      }
      if (nextValue == null) {
        synchronized (AbstractResolvedValueProducer.this) {
          synchronized (this) {
            _results = AbstractResolvedValueProducer.this._results;
            if (_resultsPushed < _results.length) {
              nextValue = _results[_resultsPushed++];
            } else {
              if (_finished) {
                finished = true;
                failure = _failure;
              } else {
                needsOuterPump = _pumped.isEmpty();
                if (s_logger.isDebugEnabled()) {
                  if (needsOuterPump) {
                    s_logger.debug("Pumping outer object");
                  } else {
                    s_logger.debug("Adding to pump set");
                  }
                }
                _pumped.add(this);
              }
            }
          }
        }
      }
      if (nextValue != null) {
        s_logger.debug("Publishing value {}", nextValue);
        context.resolved(_callback, getValueRequirement(), nextValue, this);
      } else {
        if (needsOuterPump) {
          pumpImpl(context);
        } else {
          if (finished) {
            s_logger.debug("Finished {}", getValueRequirement());
            context.failed(_callback, getValueRequirement(), failure);
          }
        }
      }
    }

    @Override
    public void close(final GraphBuildingContext context) {
      s_logger.debug("Closing callback {}", this);
      synchronized (AbstractResolvedValueProducer.this) {
        assert !_closed;
        _closed = true;
      }
      release(context);
    }

    @Override
    public boolean cancel(final GraphBuildingContext context) {
      s_logger.debug("Cancelling callback {}", this);
      synchronized (AbstractResolvedValueProducer.this) {
        if (_pumped.remove(this)) {
          // Was in a pumped state; close and release the parent resolver
          _closed = true;
        } else {
          return false;
        }
      }
      release(context);
      return true;
    }

    @Override
    public String toString() {
      return "Callback" + _objectId + "[" + _callback + ", " + AbstractResolvedValueProducer.this.toString() + "]";
    }

  }

  private final ValueRequirement _valueRequirement;
  private final Set<Callback> _pumped = new HashSet<Callback>();
  private final int _objectId = s_nextObjectId.getAndIncrement();
  private final Set<ValueSpecification> _resolvedValues = new HashSet<ValueSpecification>();
  private int _refCount;
  private ResolvedValue[] _results;
  private boolean _finished;
  private ResolutionFailure _failure;

  public AbstractResolvedValueProducer(final ValueRequirement valueRequirement) {
    _valueRequirement = valueRequirement;
    _results = new ResolvedValue[0];
    _refCount = 1;
  }

  @Override
  public Cancellable addCallback(final GraphBuildingContext context, final ResolvedValueCallback valueCallback) {
    addRef();
    final Callback callback = new Callback(valueCallback);
    ResolvedValue firstResult = null;
    boolean finished = false;
    ResolutionFailure failure = null;
    synchronized (this) {
      s_logger.debug("Added callback {} to {}", valueCallback, this);
      callback._results = _results;
      if (_results.length > 0) {
        callback._resultsPushed = 1;
        firstResult = _results[0];
      } else {
        if (_finished) {
          finished = true;
          failure = _failure;
        } else {
          _pumped.add(callback);
        }
      }
    }
    if (firstResult != null) {
      s_logger.debug("Pushing first callback result {}", firstResult);
      context.resolved(valueCallback, getValueRequirement(), firstResult, callback);
    } else if (finished) {
      s_logger.debug("Pushing failure");
      context.failed(valueCallback, getValueRequirement(), failure);
      release(context);
      return null;
    }
    return callback;
  }

  protected boolean pushResult(final GraphBuildingContext context, final ResolvedValue value) {
    assert value != null;
    assert !_finished;
    assert getValueRequirement().isSatisfiedBy(value.getValueSpecification());
    Collection<Callback> pumped = null;
    final ResolvedValue[] newResults;
    synchronized (this) {
      if (!_resolvedValues.add(value.getValueSpecification())) {
        s_logger.debug("Rejecting {} already available from {}", value, this);
        return false;
      }
      final int l = _results.length;
      s_logger.debug("Result {} available from {}", value, this);
      newResults = new ResolvedValue[l + 1];
      System.arraycopy(_results, 0, newResults, 0, l);
      newResults[l] = value;
      _results = newResults;
      if (!_pumped.isEmpty()) {
        pumped = new ArrayList<Callback>(_pumped);
        _pumped.clear();
      }
    }
    if (pumped != null) {
      for (Callback callback : pumped) {
        ResolvedValue pumpValue;
        synchronized (callback) {
          pumpValue = newResults[callback._resultsPushed++];
        }
        s_logger.debug("Pushing result to {}", callback._callback);
        context.resolved(callback._callback, getValueRequirement(), pumpValue, callback);
      }
    }
    return true;
  }

  /**
   * Triggers either a future (or immediate) call to either one or more {@link #pushResult} or a single {@link #finished}.
   * 
   * @param context building context
   */
  protected abstract void pumpImpl(final GraphBuildingContext context);

  /**
   * Call when there are no more values that can be pushed. Any callbacks that have had pump called on them will
   * receive a failure notification. This must only be called once.
   */
  protected void finished(final GraphBuildingContext context) {
    s_logger.debug("Finished producing results at {}", this);
    assert !_finished;
    Collection<Callback> pumped = null;
    ResolutionFailure failure = null;
    synchronized (this) {
      _finished = true;
      if (!_pumped.isEmpty()) {
        failure = _failure;
        pumped = new ArrayList<Callback>(_pumped);
        _pumped.clear();
      }
    }
    if (pumped != null) {
      for (Callback callback : pumped) {
        s_logger.debug("Pushing failure to {}", callback._callback);
        context.failed(callback._callback, getValueRequirement(), failure);
        release(context);
      }
    } else {
      s_logger.debug("No pumped callbacks");
    }
  }

  protected void storeFailure(final ResolutionFailure failure) {
    if (failure != null) {
      synchronized (this) {
        if (_failure == null) {
          if (_results.length == 0) {
            _failure = (ResolutionFailure) failure.clone();
            return;
          }
          _failure = ResolutionFailure.resolvedValue(getValueRequirement(), _results[0]);
        }
        while (_failure.getResultCount() < _results.length) {
          _failure.resolvedValue(_results[_failure.getResultCount()]);
        }
        _failure.merge(failure);
      }
    }
  }

  protected ResolvedValue[] getResults() {
    assert _finished;
    return _results;
  }

  public ValueRequirement getValueRequirement() {
    return _valueRequirement;
  }

  protected int getObjectId() {
    return _objectId;
  }

  @Override
  public synchronized void addRef() {
    assert _refCount > 0;
    _refCount++;
  }

  @Override
  public synchronized int release(final GraphBuildingContext context) {
    assert _refCount > 0;
    s_logger.debug("Release called on {}, refCount={}", this, _refCount);
    return --_refCount;
  }

}
