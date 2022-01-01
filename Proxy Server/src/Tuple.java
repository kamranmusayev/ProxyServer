


public class Tuple{
	
    private String last_modified;
    private byte[] cached_resource;

    
	
	public Tuple(String last_modified ,byte[] resource ){
		this.last_modified = last_modified;
		cached_resource = resource;
		
	}
	
	public String getLast_modified() {
		return last_modified;
	}

	public void setLast_modified(String last_modified) {
		this.last_modified = last_modified;
	}


	public byte[] getCached_resource() {
		return cached_resource;
	}

	public void setCached_resource(byte[] cached_resource) {
		this.cached_resource = cached_resource;
	}

	@Override
	public String toString() {
		return "Last Modified: "+getLast_modified();
	}
	
	
	
}