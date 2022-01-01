
public class Response {
    private int statusCode;
    private String reasonPhrase;
    private String raw_mime_header;
    private MimeHeader mh;

    public Response(String request) {
    	int index = request.indexOf(" ");
        statusCode = Integer.parseInt(request.substring(index));
        reasonPhrase =  request.substring(index+1,request.indexOf(" ", index+1));
        raw_mime_header = request.substring(request.indexOf(reasonPhrase)+1);
        mh = new MimeHeader(raw_mime_header);
    }

    public Response(int code, String reason, MimeHeader m) {
        statusCode = code;
        reasonPhrase = reason;
        mh = m;
        mh.put("Connection", "close");
    }
    
    @Override
    public String toString() {
        return "HTTP/1.1 " + statusCode + " " + reasonPhrase + "\r\n" + mh + "\r\n";
    }
}