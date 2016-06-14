  package l3s.curves; 
    
  /**
  * ValidationInformation for Precision Recall Curves
  */
  public class ValidationInformationPrecRec implements Comparable<ValidationInformationPrecRec>
  {
    private String classLabel ; 
    double distance ; 
    private String id=null;
    String features=null;
    
    
    public String getFeatures() {
		return features;
	}

	public void setFeatures(String features) {
		this.features = features;
	}

	public String getClassLabel()
    {
      return classLabel ; 
    }    
    
    public double getDistance()
    {
      return distance ; 
    }
    
    public ValidationInformationPrecRec( String id,String aClassLabel , double aDistance ) 
    {
      classLabel = aClassLabel ; 
      distance = aDistance ; 
      this.id=id;
    }
    public String getId(){return id;}
    public ValidationInformationPrecRec( String aClassLabel , double aDistance ) 
    {
      classLabel = aClassLabel ; 
      distance = aDistance ; 
    }
    public int compareTo(ValidationInformationPrecRec inf) 
    {
      if (this.distance < inf.distance ) 
      {
        return 1 ; 
      }
      else if (this.distance > inf.distance ) 
      {
        return -1 ; 
      }
      else 
      {
        return 0 ; 
      }
    }
    
    public String toString()
    {
      return "(" + classLabel + "," + distance + ")" ; 
    }
    
    public static void main(String[] args)
    {
      System.out.println("test"); 
    }
    
  }
  
  
  
