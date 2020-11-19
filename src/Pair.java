
public class Pair implements Comparable<Pair>{
	public String word;
	public Double val;
	
	public Pair(String word, Double val) {
		this.word = word;
		this.val = val;
	}

    public String getWord() {
      return word;
    }
    
    public Double getVal() {
      return val;
    }

    @Override
    public int compareTo(Pair pair) {
      return val.compareTo(pair.getVal());
    }
    
    @Override
    public String toString() {
    	return (word + "	" + val);
    }
}
