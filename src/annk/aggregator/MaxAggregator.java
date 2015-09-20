package annk.aggregator;

import java.util.Collections;
import java.util.List;

public class MaxAggregator implements IAggregator {

	@Override
	public double getAggregateValue(List<Double> values) {
		return Collections.max(values);
	}
	
}
