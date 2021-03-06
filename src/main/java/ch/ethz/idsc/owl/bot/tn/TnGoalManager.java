// code by jph
package ch.ethz.idsc.owl.bot.tn;

import java.io.Serializable;
import java.util.List;

import ch.ethz.idsc.owl.data.Lists;
import ch.ethz.idsc.owl.glc.adapter.CatchyTrajectoryRegionQuery;
import ch.ethz.idsc.owl.glc.adapter.GoalAdapter;
import ch.ethz.idsc.owl.glc.core.CostFunction;
import ch.ethz.idsc.owl.glc.core.GlcNode;
import ch.ethz.idsc.owl.glc.core.GoalInterface;
import ch.ethz.idsc.owl.math.flow.Flow;
import ch.ethz.idsc.owl.math.region.Region;
import ch.ethz.idsc.owl.math.state.StateTime;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.TensorMetric;
import ch.ethz.idsc.tensor.red.Norm;
import ch.ethz.idsc.tensor.sca.Ramp;

/** goal region is spherical
 * 
 * objective is minimum path length */
class TnGoalManager implements Region<Tensor>, CostFunction, Serializable {
  private final TensorMetric tensorMetric;
  private final Tensor center;
  private final Scalar radius;

  public TnGoalManager(TensorMetric tensorMetric, Tensor center, Scalar radius) {
    this.tensorMetric = tensorMetric;
    this.center = center;
    this.radius = radius;
  }

  @Override // from CostIncrementFunction
  public Scalar costIncrement(GlcNode node, List<StateTime> trajectory, Flow flow) {
    StateTime from = node.stateTime();
    return Norm._2.between(from.state(), Lists.getLast(trajectory).state());
  }

  @Override // from HeuristicFunction
  public Scalar minCostToGoal(Tensor x) {
    return Ramp.of(tensorMetric.distance(x, center).subtract(radius));
  }

  @Override // from Region
  public boolean isMember(Tensor tensor) {
    return Scalars.isZero(minCostToGoal(tensor));
  }

  public GoalInterface getGoalInterface() {
    return new GoalAdapter(CatchyTrajectoryRegionQuery.timeInvariant(this), this);
  }
}
