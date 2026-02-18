package co.ravn.ecommerce.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.web.util.HtmlUtils;

public class XssSanitizerUtil  {
      private XssSanitizerUtil() {}
  
  private static List<Pattern> xssInputPatterns = new ArrayList<>();

  static {
    xssInputPatterns.add(Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE));

    xssInputPatterns.add(Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));

    xssInputPatterns.add(Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));

    xssInputPatterns.add(Pattern.compile("</script>", Pattern.CASE_INSENSITIVE));

    xssInputPatterns.add(Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));

    xssInputPatterns.add(Pattern.compile("<input(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));

    xssInputPatterns.add(Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));

    xssInputPatterns.add(Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));

    xssInputPatterns.add(Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE));

    xssInputPatterns.add(Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE));

    xssInputPatterns.add(Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));
    
    xssInputPatterns.add(Pattern.compile("onfocus(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));
    
    xssInputPatterns.add(Pattern.compile("<form[^>]*>.*?</form>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));

  }

  public static String stripXSS(String value) {
    if(value != null) {
      for(Pattern xssInputPattern : xssInputPatterns) {
        xssInputPattern.matcher(value).replaceAll("");
      }
      
      value = HtmlUtils.htmlEscape(value);
    }
   return value;
  }
}
