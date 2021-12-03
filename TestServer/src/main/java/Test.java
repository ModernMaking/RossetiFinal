import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.lang3.StringEscapeUtils;
import sun.misc.IOUtils;

import org.json.JSONObject;

public class Test {

    private static Ontology o = new Ontology();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/test", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        o.addObject("1");
    }

    private static String createResponse(String requestMethod) {
        String resp = "<svg viewbox='-10 -10 20 20' height='100vh'><text text-" +
                "anchor='middle' stroke='red' stroke-width='0.05' font-size='4'>%s</text></svg>";
        if ("GET".equals(requestMethod))
            return String.format(resp, "GET");
        if ("POST".equals(requestMethod))
            return String.format(resp, "POST");
        return "unknown method";
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            float speed = 0;
            String message = "";
            float vertical_speed = 0;


            //String text = problem.getFullText();
            //System.out.println(text);

            String method = t.getRequestMethod();

            //System.out.println(t.getRequestBody().toString());
            byte [] b = new byte[1000];
            InputStream is = t.getRequestBody();

            int ch;
            StringBuilder sb = new StringBuilder();
            while((ch = is.read()) != -1)
                sb.append((char)ch);
            String inputString = sb.toString();
            //System.out.println("String: "+inputString);
            if (sb.length()>0) {

                HashMap<String, String> param_value = new HashMap<String, String>();
                String[] params = inputString.split("&");
                for (String param : params) {
                    String[] key_value = param.split("=");
                    param_value.put(key_value[0], key_value[1]);
                }

                String pressed = param_value.get("pressed");
                float trackSin = Float.parseFloat(param_value.get("tracksin"));
                String up = param_value.get("up");
                float heigth = Float.parseFloat(param_value.get("heigth"));
                //System.out.println("Pressed: "+pressed);
                ObjState state = o.getSpeed("1", (pressed.equals("true")), trackSin, up.equals("true"), heigth);
                speed = state.speed;
                vertical_speed = state.vertical_speed;
                message = state.message;
            }


            String response = "This is the response";

            OutputStream os = t.getResponseBody();
            OutputStreamWriter ow = new OutputStreamWriter(os, "Cp1251");
            String respStr = new String(response.getBytes("ISO-8859-1"), "Cp1251");
            JSONObject jo = new JSONObject();
            jo.put("text", StringEscapeUtils.escapeJava(response));
            jo.put("speed", speed);
            jo.put("verticalspeed", vertical_speed);
            jo.put("message", StringEscapeUtils.escapeJava(message));
            String jsonStr = jo.toString();
            t.sendResponseHeaders(200, jsonStr.length());
            ow.write(jsonStr);
            ow.flush();
            ow.close();
            //os.write(response.getBytes());
            //os.close();
        }

        private String requestToJena(HashMap<String, String> param_value)
        {
            return "Correct";
        }

    }
}