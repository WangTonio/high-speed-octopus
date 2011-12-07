package com.hscc.hellogooglemap;

import java.util.ArrayList;
import java.util.Stack;
import android.location.Location;
import android.util.Log;

public class reduceGPS {
	  /**
	   * Decimates the given locations for a given zoom level. This uses a
	   * Douglas-Peucker decimation algorithm.
	   *
	   * @param tolerance in meters
	   * @param locations input
	   * @param decimated output
	   */
	public static final double TO_RADIANS = Math.PI / 180.0;

	  public void decimate(double tolerance, ArrayList<Location> locations,
	      ArrayList<Location> decimated) {
	    final int n = locations.size();
	    if (n < 1) {
	      return;
	    }
	    int idx;
	    int maxIdx = 0;
	    Stack<int[]> stack = new Stack<int[]>();
	    double[] dists = new double[n];
	    dists[0] = 1;
	    dists[n - 1] = 1;
	    double maxDist;
	    double dist = 0.0;
	    int[] current;

	    if (n > 2) {
	      int[] stackVal = new int[] {0, (n - 1)};
	      stack.push(stackVal);
	      while (stack.size() > 0) {
	        current = stack.pop();
	        maxDist = 0;
	        for (idx = current[0] + 1; idx < current[1]; ++idx) {
	          dist = distance(
	              locations.get(idx),
	              locations.get(current[0]),
	              locations.get(current[1]));
	          if (dist > maxDist) {
	            maxDist = dist;
	            maxIdx = idx;
	          }
	        }
	        if (maxDist > tolerance) {
	          dists[maxIdx] = maxDist;
	          int[] stackValCurMax = {current[0], maxIdx};
	          stack.push(stackValCurMax);
	          int[] stackValMaxCur = {maxIdx, current[1]};
	          stack.push(stackValMaxCur);
	        }
	      }
	    }

	    int i = 0;
	    idx = 0;
	    decimated.clear();
	    for (Location l : locations) {
	      if (dists[idx] != 0) {
	        decimated.add(l);
	        i++;
	      }
	      idx++;
	    }
	    Log.d("W", "Decimating " + n + " points to " + i
	        + " w/ tolerance = " + tolerance);
	  }

	public static double distance(
      final Location c0, final Location c1, final Location c2) {
    if (c1.equals(c2)) {
      return c2.distanceTo(c0);
    }

    final double s0lat = c0.getLatitude() * TO_RADIANS;
    final double s0lng = c0.getLongitude() * TO_RADIANS;
    final double s1lat = c1.getLatitude() * TO_RADIANS;
    final double s1lng = c1.getLongitude() * TO_RADIANS;
    final double s2lat = c2.getLatitude() * TO_RADIANS;
    final double s2lng = c2.getLongitude() * TO_RADIANS;

    double s2s1lat = s2lat - s1lat;
    double s2s1lng = s2lng - s1lng;
    final double u =
        ((s0lat - s1lat) * s2s1lat + (s0lng - s1lng) * s2s1lng)
            / (s2s1lat * s2s1lat + s2s1lng * s2s1lng);
    if (u <= 0) {
      return c0.distanceTo(c1);
    }
    if (u >= 1) {
      return c0.distanceTo(c2);
    }
    Location sa = new Location("");
    sa.setLatitude(c0.getLatitude() - c1.getLatitude());
    sa.setLongitude(c0.getLongitude() - c1.getLongitude());
    Location sb = new Location("");
    sb.setLatitude(u * (c2.getLatitude() - c1.getLatitude()));
    sb.setLongitude(u * (c2.getLongitude() - c1.getLongitude()));
    return sa.distanceTo(sb);
  }


}
