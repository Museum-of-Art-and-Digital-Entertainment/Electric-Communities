
package ec.examples.lm2000;

import java.util.Vector;

public class Rating {
  public Restaurant restaurant;
  public int score;

  public Rating(Restaurant r, int s) {
    restaurant = r;
    score = s;
  }
}
