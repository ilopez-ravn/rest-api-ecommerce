package co.ravn.ecommerce.Filters;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import co.ravn.ecommerce.Utils.XssSanitizerUtil;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class XSSRequestWrapper extends HttpServletRequestWrapper {
    
  public XSSRequestWrapper(HttpServletRequest request) {
    super(request);
  }
 
  @Override
  public String[] getParameterValues(String parameter) {
    String[] values = super.getParameterValues(parameter);
    if (values != null) {
      for (int i = 0; i < values.length; i++) {                 
        values[i] = XssSanitizerUtil.stripXSS(values[i]);
      }
    }
    return values;
  }

  @Override
  public String getParameter(String parameter) {
    String value = super.getParameter(parameter);
    if(value != null) {
      value = XssSanitizerUtil.stripXSS(value);
    }
    return value;
  }

  @Override
  public String getHeader(String name) {
    String value = super.getHeader(name);
    return (value != null) ? XssSanitizerUtil.stripXSS(value) : null;
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    ServletInputStream inputStream = super.getInputStream();
    String requestBody = new String(inputStream.readAllBytes());
    
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonNode = mapper.readTree(requestBody);
    
    sanitizeJsonNode(jsonNode);
    
    String sanitizeBody = mapper.writeValueAsString(jsonNode);

    return new ServletInputStream() {
      
      private final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
        sanitizeBody.getBytes()
      );

      @Override
      public int read() throws IOException {
        return byteArrayInputStream.read();
      }

      @Override
      public void setReadListener(ReadListener arg0) {
        
      }

      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public boolean isFinished() {
        return byteArrayInputStream.available() == 0;
      }
    };
  }
  
  private void sanitizeJsonNode(JsonNode node) {
    if(node.isObject()) {
      ObjectNode objectNode = (ObjectNode) node;
      objectNode.fields().forEachRemaining(entry -> {
        JsonNode valueNode = entry.getValue();
        if(valueNode.isTextual()) {
          objectNode.put(entry.getKey(), XssSanitizerUtil.stripXSS(valueNode.textValue()));
        } else if(valueNode.isObject() || valueNode.isArray()) {
          sanitizeJsonNode(valueNode);
        }
      });
    } else if(node.isArray()) {
      ArrayNode arrayNode = (ArrayNode) node;
      for(int i=0 ; i< arrayNode.size(); i++) {
        JsonNode jsonNode = arrayNode.get(i);
        if(jsonNode.isObject()) {
          sanitizeJsonNode(jsonNode);
        } else if(jsonNode.isTextual()) {
            arrayNode.set(i, XssSanitizerUtil.stripXSS(jsonNode.textValue()));
          }
      }
    }
  }
}
