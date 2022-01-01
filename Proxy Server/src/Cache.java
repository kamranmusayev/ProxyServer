
import java.util.HashMap;


public class Cache {
	static final int Cachable_Object_Limit = 512;
	private static HashMap<String, Tuple> Cached_Objects = new HashMap<String, Tuple>();
	

	
	public synchronized static Tuple getCached_Objects(String URL) throws Exception {
		return Cached_Objects.get(URL);
	}
	
	public synchronized static boolean AddtoCache(String URL, String last_modified, byte[] resource) {
		if(Cached_Objects.size() < Cachable_Object_Limit)
			Cached_Objects.put(URL, new Tuple(last_modified,resource));
		else 
			return false;
		
		return true;
	}
	public synchronized static boolean check_cache(String URL) {
		if(Cached_Objects.containsKey(URL)) {
			return true;
		}
		
		return false;
		
	}
	public synchronized static boolean Delete_object(String URL) {
		if(Cached_Objects.remove(URL) != null) return true;
		else return false;
		
	}
	
}