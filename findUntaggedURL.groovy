import javax.net.ssl.HttpsURLConnection
import java.net.*
import java.io.*

def userName = ""
def passWord = ""

if (this.args.length == 2) {
	userName = this.args[0]
	passWord = this.args[1]
} else {
    println "use: groovy findUntaggedURL <delicious_user_name> <delicious_password> "
    return -1
}

Authenticator.setDefault(new Authenticator() {
  PasswordAuthentication getPasswordAuthentication() {
    return new PasswordAuthentication(userName,passWord.toCharArray())
  }
});


def updatePost(userName, passWord, href, description) {
    try {
        def data =    URLEncoder.encode("url", "UTF-8")         + "=" + URLEncoder.encode(href, "UTF-8");  
        data += "&" + URLEncoder.encode("description", "UTF-8") + "=" + URLEncoder.encode(description, "UTF-8");  
        data += "&" + URLEncoder.encode("tags", "UTF-8")        + "=" + URLEncoder.encode("untagged", "UTF-8");  
        data += "&" + URLEncoder.encode("replace", "UTF-8")     + "=" + URLEncoder.encode("no", "UTF-8");  
      
        def url = new URL("https://${userName}:${passWord}@api.del.icio.us/v1/posts/add");  
        def conn = url.openConnection();  
        conn.setDoOutput(true);  
        def wr = new OutputStreamWriter(conn.getOutputStream());  
        wr.write(data);  
        wr.flush(); 
        def rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));  
        wr.close();  
        rd.close(); 
    } catch(Exception e) {
        e.printStackTrace()
    }

}

def deliciousPosts = {
    def postsList = []
    def url = new URL("https://${userName}:${passWord}@api.del.icio.us/v1/posts/all")
    def posts = new XmlParser().parseText(url.text)
    posts.each{ pAttr ->
        def description = pAttr.attribute("description")
        def href = pAttr.attribute("href")
        def tags = pAttr.attribute("tag")

        if ("".equals(tags)) {
            def post = [:]
            post.href = href
            post.description = description
            postsList.add(post)
        }
    }
    postsList
}


deliciousPosts().each {
    updatePost(userName, passWord, it.href, it.description) 
    println it.href
}
