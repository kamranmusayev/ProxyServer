import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

@SuppressWarnings("serial")
public class MimeHeader extends HashMap<String,String>{
	
	public MimeHeader() {
	}
	
	public MimeHeader(String data) {
		StringTokenizer st = new StringTokenizer(data, "\r\n");
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			try {
				int a = s.indexOf(":");
				String key = s.substring(0 , a);
				String value = s.substring(a+2);
				put(key, value);
			}catch(StringIndexOutOfBoundsException itr) {
				String key = "data";
				String value = s;
				put(key, value);
			}catch(Exception itr) {
				itr.printStackTrace();
			}
			
		}
	}
	
	@Override
	public String toString() {
		String str = "";
		Iterator<String> itr = keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();
			if(key != "data"){
				String val = get(key);
				str += key + ": " + val + "\r\n";
			}
		}
		return str;
	}
}
